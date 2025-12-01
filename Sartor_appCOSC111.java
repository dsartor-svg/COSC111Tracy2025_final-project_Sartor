import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Scanner;
import javax.swing.*;


public class Sartor_appCOSC111{
    @FunctionalInterface
    interface Renderer {
        void render(int[][] ancestries, int[] pixels, int width, int height, float[][] pixels2);
    }
    public static BufferedImage draw(int width, int height, int[][] ancestries, Renderer renderer) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] colors = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        float[][] colors2 = new float[height*width][4];
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
    public static void renderRecursive(double[] vector2, int iter, String s, boolean spin, double grow, double b, float hue, int height, int width, float[][] pixels, double startx, double starty, float hueSize, short bright, float hEnd, float[] rgbL, double[][][][] vectors, int spinCount, int bCount){
        if(iter==0){
            ColorUtils.sampleRainbow(hue/16384/hEnd, rgbL);
            float bright2 = (float) Math.sqrt((float)Math.pow(2f,bright));
            int index = (int)starty*width+(int)startx;
            pixels[index][0]+=rgbL[0]*bright2;
            pixels[index][1]+=rgbL[1]*bright2;
            pixels[index][2]+=rgbL[2]*bright2;
            pixels[index][3]+=bright2;
        }
        else{
            for (int k=0; k<s.length(); k++){
                char c = s.charAt(k);
                switch (c){
                    case '+' -> {spinCount+=(spin ? 1 : -1);}
                    case '-' -> {spinCount-=(spin ? 1 : -1);}
                    case 'A' -> {
                        renderRecursive(vector2, iter-1, s, spin, grow, b, hue, height, width, pixels, startx, starty, hueSize*(float) grow, bright, hEnd, rgbL, vectors, spinCount, bCount+(spin ? 1 : -1));
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
    
    public static double[] subPixel(double[] vector2, int iter, String s, double spinO, double grow, double b, int[][] arr, int seed){
        double lx = 0;
        double ly = 0;
        double maxlx = 0;
        double minlx = 0;
        double maxly = 0;
        double minly = 0;
        int mult = 0;
        short bright = 0;
        float hue = 0f;
        float hueSize = 16384f;
        double[] vector = {vector2[0],vector2[1]};
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i)=='A') {
                mult++;
            }
        }
        double[] lxy = {lx,ly};
        double spin=spinO;
        boolean fWards = true;
        double[][] savef = new double[arr[seed].length+1][4];
        for (int j=0; j<arr[seed].length; j++){
            rotate(vector,b*Math.signum(spin));
            int index=-1;//(fwards ? -1 : s.length())
            for (int k=0; k<=arr[seed][j]; k++){
                index+=s.substring(index+1).indexOf('A')+1;
            }
            if (arr[seed][j]>=mult){
                index=s.length();
            }
            for (int k=0; k<index; k++){
                switch (s.charAt(k)){
                    case '+' -> {rotate(vector,spin);
                        break;}
                    case '-' -> {rotate(vector,-spin);
                        break;}
                    case 'A' -> {forward(lxy,vector);
                        lx=lxy[0];
                        ly=lxy[1];
                        break;}
                    case 'a' -> {forward(lxy,vector);
                        lx=lxy[0];
                        ly=lxy[1];
                        break;}
                    case '|' -> {spin*=-1;
                        break;}
                    case '[' -> {savef[j+1][0]=lx;
                        savef[j][1]=ly;
                        savef[j][2]=angleOf(vector[0],vector[1]);
                        savef[j][3]=spin;
                        break;}
                        
                    case ']' -> {lx=savef[j][0];
                        ly=savef[j][1];
                        rotate(vector,-angleOf(vector[0],vector[1]));
                        rotate(vector,savef[j][2]);
                        spin=savef[j][3];
                        break;}
                    case '*' -> {fWards=!fWards;
                        break;}
                    case '^' -> {bright+=1;
                        break;}
                    case 'v' -> {bright-=1;
                        break;}
                    case '>' -> {hue+=hueSize;
                        break;}
                    case '<' -> {hue-=hueSize;
                        break;}
                }
                maxlx=Math.max(maxlx,lx);
                minlx=Math.min(minlx,lx);
                maxly=Math.max(maxly,ly);
                minly=Math.min(minly,ly);
            }
            vector[0]*=grow;
            vector[1]*=grow;
            hueSize*=grow;
        }
        double[] lpos = {lx,ly,maxlx,minlx,maxly,minly,spin,bright,hue/16384f};
        return lpos;
    }
    public static void subPixel2(double[] vector2, int iter, String s, double spinO, double grow, double b, int[][] arr, int seed, int height, int width, float[][] pixels, double startx, double starty){
        double[] pos = subPixel(vector2, iter, s, spinO, grow, b, arr, seed);
        float hue = (float) pos[8]/((1f/(float) grow)/(1f/(float) grow-1f));
        float[] rgbL = new float[3];
        ColorUtils.sampleRainbow(hue, rgbL);
        float bright = (float) Math.pow(2f,pos[7]);
        double posx = pos[0]+startx;
        double posy = pos[1]+starty;
        pixels[(int)posy*width+(int)posx][0]+=rgbL[0]*bright;
        pixels[(int)posy*width+(int)posx][1]+=rgbL[1]*bright;
        pixels[(int)posy*width+(int)posx][2]+=rgbL[2]*bright;
        pixels[(int)posy*width+(int)posx][3]+=bright;
        
    }
    public static void main(String[] args){
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screen.width;
        int height = screen.height-80;
        Scanner kb = new Scanner(System.in);
        // 4 A+A--A+A - -
        // 4 A+A- - -
        // 3 A+A-A - -
        // 3 A+|A|-A - -
        // 3 |A|+A-|A| - -
        double spinInput = 1/kb.nextDouble();
        String A = kb.next();
        int multi=0;
        for (int i=0; i<A.length(); i++){
            if (A.charAt(i)=='A'){
                multi++;
            }
        }
        final int mult = multi;
        System.out.println("mult "+mult);
        int iter = 5; //Placeholder.
        
        String iterIn = kb.next();

        if (iterIn.equals("-")){
            for (int i=1; Math.pow(mult,i)<=Math.pow(2,20); i++){
                iter=i;
            }
            System.out.println("iter "+iter);
        }
        else{
            for (int i=1; Math.pow(mult,i)<=Math.pow(2,Integer.parseInt(iterIn)); i++){
                iter=i;
            }
            System.out.println("iter "+iter);
        }
        double spinl = Math.TAU*spinInput;
        double sAngle = 0;
        String angleIn = kb.next();
        if (!angleIn.equals("-")){
            sAngle=Double.parseDouble(angleIn)*spinl;
            System.out.println("angle "+sAngle);
        }
        double[] vectorl = {Math.cos(sAngle),Math.sin(sAngle)};
        double lsx = 0;
        double lsy = 0;
        double maxlx = 0;
        double minlx = 0;
        double maxly = 0;
        double minly = 0;
        double lx;
        double ly;
        double scorrect;
        double bcorrect;
        double gcorrect;
        
        double maxlsx=0;
        double minlsx=0;
        double maxlsy=0;
        double minlsy=0;
        int[][] placeholder = {{mult}};
        double[] pos=subPixel(vectorl, 1, A, spinl, (double) 1, 0, placeholder, 0); 
        lx = pos[0];
        ly = pos[1];
        for (int a=0; a<pos.length; a++){
            System.out.println("pos["+a+"] "+pos[a]);
        }
        scorrect=1/(Math.pow(Math.pow(lx,2)+Math.pow(ly,2),(0.5)));
        bcorrect=sAngle-angleOf(lx,ly)*(1+Math.signum(pos[6]))/2;
        //ecorrect=-angleOf(matrix[0],matrix[2])-bcorrect;
        System.out.println("lx "+lx);
        System.out.println("ly "+ly);
        System.out.println("scorrect "+scorrect);
        System.out.println("bcorrect "+bcorrect);
        //System.out.println(ecorrect);
        lx=0;
        ly=0;
        double lsx2=lsx;
        double lsy2=lsy;
        vectorl[0]=Math.cos(sAngle);
        vectorl[1]=Math.sin(sAngle);
        int iterSmall = Math.max(3,iter-(int) (Math.log(1024)/Math.log(mult)));
        System.out.println("iterSmall "+iterSmall);
        int[][] ancestriesSmall = new int[(int) Math.pow(mult,iterSmall)][iterSmall];
        for (int i=0; i<ancestriesSmall.length; i++){
            ancestriesSmall[i]=toBaseN(i,mult,iterSmall);
        }
        for (int i=0; i<ancestriesSmall.length; i++){
            vectorl[0]=Math.cos(sAngle);
            vectorl[1]=Math.sin(sAngle);
            pos=subPixel(vectorl, iterSmall, A, spinl, scorrect, bcorrect, ancestriesSmall, i);
            maxlx = Math.max(maxlx,pos[2]);
            minlx = Math.min(minlx,pos[3]);
            maxly = Math.max(maxly,pos[4]);
            minly = Math.min(minly,pos[5]);
            lx= pos[0];
            ly= pos[1];
        }
        vectorl[0]=Math.cos(sAngle);
        vectorl[1]=Math.sin(sAngle);
        System.out.println(lx);
        System.out.println(ly);
        System.out.println(maxlx);
        System.out.println(minlx);
        System.out.println(maxly);
        System.out.println(minly);
        int iterSmaller = iterSmall-1;
        int[][] ancestriesSmaller = new int[(int) Math.pow(mult,iterSmaller)][iterSmaller];
        for (int i=0; i<ancestriesSmaller.length; i++){
            ancestriesSmaller[i]=toBaseN(i,mult,iterSmaller);
        }
        for (int i=0; i<ancestriesSmaller.length; i++){
            vectorl[0]=Math.cos(sAngle);
            vectorl[1]=Math.sin(sAngle);
            pos=subPixel(vectorl, iterSmaller, A, spinl, scorrect, bcorrect, ancestriesSmaller, i);
            maxlsx = Math.max(maxlsx,pos[2]);
            minlsx = Math.min(minlsx,pos[3]);
            maxlsy = Math.max(maxlsy,pos[4]);
            minlsy = Math.min(minlsy,pos[5]);
            lsx2= pos[0];
            lsy2= pos[1];
        }
        vectorl[0]=Math.cos(sAngle);
        vectorl[1]=Math.sin(sAngle);
        System.out.println(lsx2);
        System.out.println(lsy2);
        System.out.println(maxlsx);
        System.out.println(minlsx);
        System.out.println(maxlsy);
        System.out.println(minlsy);
        gcorrect = Math.pow(Math.min((maxlsx-minlsx)/(maxlx-minlx), (maxlsy-minlsy)/(maxly-minly)),1);
        System.out.println("gcorrect "+gcorrect);
        double maxlx2 = maxlx;
        double minlx2 = minlx;
        double maxly2 = maxly;
        double minly2 = minly;
        kb.close();
        double sAngle2 = sAngle;
        
        int[][] ancestries = new int[1][iter];
        /*for (int i=0; i<ancestries.length; i++){
            ancestries[i]=toBaseN(i,mult,iter);
        }*/
        final int iter2 = iter;
        
        Renderer colorer = (arr, pixels, widthR, heightR, pixels2) ->{
            int maxCCount=0;
            double spin = spinl;
            double size1 = -8+Math.min(heightR/(maxly2-minly2),widthR/(maxlx2-minlx2));
            System.out.println("size1 "+size1);
            double size=Math.pow(scorrect,(int)(Math.log(gcorrect*size1)/Math.log(scorrect)));//Math.round(Math.min(heightR/(maxly2-minly2),widthR/(maxlx2-minlx2))*gcorrect-20);
            System.out.println("size "+size);
            double startx=Math.floor((widthR-maxlx2*size-minlx2*size)/2)+0.5;
            double starty=Math.floor((heightR-maxly2*size-minly2*size)/2)+0.5;
            //double[] vector = {Math.cos(sAngle2)*size/8,Math.sin(sAngle2)*size/8};
            double[] vector = {Math.cos(sAngle2+bcorrect)*size,Math.sin(sAngle2+bcorrect)*size};
            float hue = 0f;
            float hueSize = 16384f;
            float hcorrect=(float) ((1/scorrect)/(1/scorrect-1));
            float[] rgbL = new float[3];
            int spinCount = 0;
            for(int i=0; i<A.length(); i++){
                if(A.charAt(i)=='+'||A.charAt(i)=='-'){
                    spinCount++;
                }
            }
            double[][][][] vectors = new double[iter2+1][2*iter2+1][spinCount*2*iter2+1][2];
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
            renderRecursive(vector, iter2, A, (Math.signum(spin)==Math.signum(1)), scorrect, bcorrect, hue, heightR, widthR, pixels2, startx, starty, hueSize, (short)0, hcorrect, rgbL, vectors, spinCount*iter2, iter2);
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