import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Scanner;
import javax.swing.*;


public class Sartor_appCOSC111{
    @FunctionalInterface
    interface Renderer { //Understand.
        void render(int[][] ancestries, int[] pixels, int width, int height, float[][] pixels2);
    }
    public static BufferedImage draw(int width, int height, int[][] ancestries, Renderer renderer) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] colors = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        float[][] colors2 = new float[height*width][4]; //Make class.
        renderer.render(ancestries, colors, width, height, colors2);
        return image;
    }
    public static void rotate(double[] direction,double angle){
        double swap=direction[0];
        direction[0]=direction[0]*Math.cos(angle)-direction[1]*Math.sin(angle);
        direction[1]=direction[1]*Math.cos(angle)+swap*Math.sin(angle);
    }

    public static int[] toBaseN(int i, int n, int d){
        int[] o = new int[d];
        for (int j=0;j<d;j++){
            o[j]=(i%((int) Math.pow(n,d-j)))/((int) Math.pow(n,d-j-1));
        }
        return o;
    }
    public static double angleOf(double x, double y){
        return Math.atan(y/x)*Math.signum(x);
    }
    
    
    public static void forward(double[] posxy, double[] vector){
        posxy[0]+=vector[0];
        posxy[1]+=vector[1];
    }
    public static void setColor(float brightness, float[][] pixels, float[] rgb, int index, float hue){
        ColorUtils.sampleRainbow(hue, rgb);
        pixels[index][0]+=rgb[0]*brightness;
        pixels[index][1]+=rgb[1]*brightness;
        pixels[index][2]+=rgb[2]*brightness;
        pixels[index][3]+=brightness;
    }
    public static void renderRecursive(int iter, String s, boolean spin, double grow, double b, float hue, int height, int width, float[][] pixels, double startx, double starty, float hueSize, short bright, float hEnd, float[] rgbL, double[][][][] vectors, int spinCount, int bCount){
        if(iter==0){
            setColor((float) Math.sqrt((float)Math.pow(2f,bright)), pixels, rgbL, (int)starty*width+(int)startx, hue/16384/hEnd);
        }
        else{
            for (int k=0; k<s.length(); k++){
                char c = s.charAt(k);
                switch (c){
                    case '+' -> {spinCount+=(spin ? 1 : -1);}
                    case '-' -> {spinCount-=(spin ? 1 : -1);}
                    case 'A' -> {
                        renderRecursive(iter-1, s, spin, grow, b, hue, height, width, pixels, startx, starty, hueSize*(float) grow, bright, hEnd, rgbL, vectors, spinCount, bCount+(spin ? 1 : -1));
                        startx+=vectors[iter][bCount][spinCount][0];
                        starty+=vectors[iter][bCount][spinCount][1];}
                    case 'a' -> {
                        startx+=vectors[iter][bCount][spinCount][0];
                        starty+=vectors[iter][bCount][spinCount][1];}
                    case '|' -> {spin=!spin;}
                    case '^' -> {bright+=1;}
                    case 'v' -> {bright-=1;}
                    case '>' -> {hue+=hueSize;}
                    case '<' -> {hue-=hueSize;}
                }
            }
        }
    }
    public static void renderRecursive(int iter, String s, boolean spin, double grow, double b, float hue, int height, int width, float[][] pixels, double startx, double starty, float hueSize, short bright, float hEnd, float[] rgbL, double[][][][] vectors, int spinCount, int bCount, double [] maxminend
    ){
        if(iter==0){
        }
        else{
            for (int k=0; k<s.length(); k++){
                char c = s.charAt(k);
                switch (c){
                    case '+' -> {spinCount+=(spin ? 1 : -1);}
                    case '-' -> {spinCount-=(spin ? 1 : -1);}
                    case 'A' -> {
                        renderRecursive(iter-1, s, spin, grow, b, hue, height, width, pixels, startx, starty, hueSize*(float) grow, bright, hEnd, rgbL, vectors, spinCount, bCount+(spin ? 1 : -1), maxminend);
                        startx+=vectors[iter][bCount][spinCount][0];
                        starty+=vectors[iter][bCount][spinCount][1];
                        maxminend[0]=Math.max(maxminend[0], startx);
                        maxminend[1]=Math.max(maxminend[1], starty);
                        maxminend[2]=Math.min(maxminend[2], startx);
                        maxminend[3]=Math.min(maxminend[3], starty);
                        maxminend[4]=startx;
                        maxminend[5]=starty;}
                    case 'a' -> {
                        startx+=vectors[iter][bCount][spinCount][0];
                        starty+=vectors[iter][bCount][spinCount][1];
                        maxminend[4]=startx;
                        maxminend[5]=starty;}
                    case '|' -> {
                        spin=!spin;
                        maxminend[6]=(spin ? 1 : -1);}
                    case '^' -> {bright+=1;}
                    case 'v' -> {bright-=1;}
                    case '>' -> {hue+=hueSize;}
                    case '<' -> {hue-=hueSize;}
                }
            }
        }
    }
    public static void main(String[] args){
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screen.width;
        int height = screen.height-80; //To fit the taskbar.
        Scanner kb = new Scanner(System.in);
        // 4 A+A--A+A - /
        // 4 A+A- - /
        // 3 A+A-A - /
        // 3 A+|A|-A - /
        // 3 |A|+A-|A| - /
        double spinInput = 1/kb.nextDouble(); //The angle of the turns is a fraction of a circle, so we take the input as the inverse of that fraction.
        String A = kb.next(); //Input sequence of turns and whatnot.
        int multi=0;
        for (int i=0; i<A.length(); i++){ //Counts the number of segments making up the path.
            if (A.charAt(i)=='A'){
                multi++;
            }
        }
        final int mult = multi;
        System.out.println("mult "+mult);
        int iter = 0;
        
        String iterIn = kb.next(); //User inputs their maximum allowable number of factors of two in number of details.

        if (iterIn.equals("-")||iterIn.equals("/")){ //Type '/' or '-' to take the default value, 20.
            for (int i=1; Math.pow(mult,i)<=Math.pow(2,20); i++){
                iter=i;
            }
            System.out.println("iter "+iter);
        }
        else{
            for (int i=1; Math.pow(mult,i)<=Math.pow(2,Integer.parseInt(iterIn)); i++){ //Number of iterations should be such that there are fewer segments in the final path than 2^input.
                iter=i;
            }
            System.out.println("iter "+iter);
        }
        double spinl = Math.TAU*spinInput; //Converts angle to radians.
        System.out.println("spin "+spinl);
        double sAngle = 0;
        String angleIn = kb.next(); //User inputs desired rotation of image, in units of the angle they defined.
        if (!(angleIn.equals("-")||angleIn.equals("/"))){
            sAngle=Double.parseDouble(angleIn)*spinl;
            System.out.println("angle "+sAngle);
        }
        kb.close();
        double sAngle2 = sAngle;
        final int iter2 = iter;
        
        Renderer colorer = (arr, pixels, widthR, heightR, pixels2) ->{ //TODO: understand.
            int maxCCount=0;
            double spin = spinl;
            //double[] vector = {Math.cos(sAngle2)*size/8,Math.sin(sAngle2)*size/8};
            float hue = 0f;
            float hueSize = 16384f;
            float[] rgbL = new float[3];
            int spinCount = 0;
            for(int i=0; i<A.length(); i++){ //Counts number of turns in input path.
                if(A.charAt(i)=='+'||A.charAt(i)=='-'){
                    spinCount++;
                }
            }
            int itersmaller=1;
            double[][][][] vectorss = new double[itersmaller+1][2*itersmaller+1][spinCount*2*itersmaller+1][2]; //Lookup table for vectors. //TODO: turn into a method or class.
            for(int h=0; h<=itersmaller; h++){
                for(int i=0; i<2*itersmaller+1; i++){
                    for(int j=0; j<spinCount*2*itersmaller+1; j++){
                        double vAngle = -0*itersmaller-spinCount*spin*itersmaller+i*0+j*spin;
                        vectorss[h][i][j][0]=(Math.cos(sAngle2+0+vAngle))*Math.pow(1,itersmaller-h);
                        vectorss[h][i][j][1]=(Math.sin(sAngle2+0+vAngle))*Math.pow(1,itersmaller-h);
                    }
                }
            }
            double startx=0;
            double starty=0;
            double[] maxmin = {startx,starty,startx,starty,startx,starty, 1}; //Output vector for test.
            renderRecursive(itersmaller, A, (Math.signum(spin)==Math.signum(1)), 1, 0, hue, heightR, widthR, pixels2, startx, starty, hueSize, (short)0, 1, rgbL, vectorss, spinCount*itersmaller, itersmaller, maxmin); //Test.
            System.out.println(maxmin[4]);
            System.out.println(maxmin[5]);
            double scorrect=1/(Math.pow(Math.pow(maxmin[4],2)+Math.pow(maxmin[5],2),(0.5))); //Inverse of distance between start and end of input path.
            double bcorrect=sAngle2-angleOf(maxmin[4],maxmin[5])*(1+Math.signum(maxmin[6]))/2; //Amount path needs to be rotated to form horizontal line.
            System.out.println("scorrect "+scorrect);
            System.out.println("bcorrect "+bcorrect);
            double[] maxmin2 = {startx,starty,startx,starty,startx,starty, 1}; //Reset output vector.
            maxmin=maxmin2;
            int itersmall=iter2-3;
            double[][][][] vectorsu = new double[itersmall+1][2*itersmall+1][spinCount*2*itersmall+1][2]; //Lookup table for vectors. //TODO: turn into a method or class.
            for(int h=0; h<=itersmall; h++){
                for(int i=0; i<2*itersmall+1; i++){
                    for(int j=0; j<spinCount*2*itersmall+1; j++){
                        double vAngle = -bcorrect*itersmall-spinCount*spin*itersmall+i*bcorrect+j*spin;
                        vectorsu[h][i][j][0]=(Math.cos(sAngle2+bcorrect+vAngle))*Math.pow(scorrect,itersmall-h);
                        vectorsu[h][i][j][1]=(Math.sin(sAngle2+bcorrect+vAngle))*Math.pow(scorrect,itersmall-h);
                    }
                }
            }
            renderRecursive(itersmall, A, (Math.signum(spin)==Math.signum(1)), scorrect, bcorrect, hue, heightR, widthR, pixels2, startx, starty, hueSize, (short)0, 1, rgbL, vectorsu, spinCount*itersmall, itersmall, maxmin);
            System.out.println(maxmin[0]);
            System.out.println(maxmin[1]);
            System.out.println(maxmin[2]);
            System.out.println(maxmin[3]);
            
            double size1=-8+Math.min(heightR/(maxmin[1]-maxmin[3]),widthR/(maxmin[0]-maxmin[2]));
            System.out.println("size1 "+size1);
            double size=Math.pow(scorrect,(int)(Math.log(/*gcorrect**/size1)/Math.log(scorrect)));
            System.out.println("size "+size); //Planned distance between start and end of final path?
            startx=Math.floor((widthR-maxmin[0]*size-maxmin[2]*size)/2)+0.5;
            starty=Math.floor((heightR-maxmin[1]*size-maxmin[3]*size)/2)+0.5;
            float hcorrect=(float) ((1/scorrect)/(1/scorrect-1)); //How much the smaller components of the path are less able to affect hues.
            double[][][][] vectors = new double[iter2+1][2*iter2+1][spinCount*2*iter2+1][2]; //Lookup table for vectors. //TODO: turn into a method or class.
            for(int h=0; h<=iter2; h++){
                for(int i=0; i<2*iter2+1; i++){
                    for(int j=0; j<spinCount*2*iter2+1; j++){
                        double vAngle = -bcorrect*iter2-spinCount*spin*iter2+i*bcorrect+j*spin;
                        vectors[h][i][j][0]=(Math.cos(sAngle2+bcorrect+vAngle))*size*Math.pow(scorrect,iter2-h);
                        vectors[h][i][j][1]=(Math.sin(sAngle2+bcorrect+vAngle))*size*Math.pow(scorrect,iter2-h);
                    }
                }
            }
            System.out.println(vectors[iter2][iter2][spinCount*iter2][0]);
            System.out.println(vectors[iter2][iter2][spinCount*iter2][1]);
            renderRecursive(iter2, A, (Math.signum(spin)==Math.signum(1)), scorrect, bcorrect, hue, heightR, widthR, pixels2, startx, starty, hueSize, (short)0, hcorrect, rgbL, vectors, spinCount*iter2, iter2);
            int cCount;
            for (int i=0; i<widthR*heightR; i++){
                cCount=(int) pixels2[i][3];
                if (cCount>maxCCount){maxCCount=cCount;}
            }
            System.out.println("maxCCount "+maxCCount);
            //System.out.println("minHue "+minHue);
            //System.out.println("maxHue "+maxHue);
            short[] t;
            for (int i=0; i<widthR*heightR; i++){
                float[] cSum={pixels2[i][0],pixels2[i][1],pixels2[i][2]};
                if ((pixels2[i][3])!=0){
                    //float[] rgbR=new float[] {cSum[0]/maxCCount,cSum[1]/maxCCount,cSum[2]/maxCCount};
                    t = new short[]{(short) (255*ColorUtils.linearToSrgb(cSum[0]/maxCCount)),(short) (255*ColorUtils.linearToSrgb(cSum[1]/maxCCount)),(short) (255*ColorUtils.linearToSrgb(cSum[2]/maxCCount)) };  //trueColor(rgbR, (float) cCount/maxCCount);
                    pixels[i]=(t[0]<<16 | t[1]<<8 | t[2]);
                    //System.out.println("cCount"+cCount);
                }
            }
        };
        int[][] ancestries={{0}};
        BufferedImage image = draw(width, height, ancestries, colorer);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width, height);
            }
        };
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}