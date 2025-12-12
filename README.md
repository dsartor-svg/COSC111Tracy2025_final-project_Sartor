Program creates images from special input strings. Program is intended for producing beauty, for exercising recursive and spatial reasoning, and for incentivizing either by tying it to the other.
Example inputs:

4 `-|A|+^^A>Avv+|A|-` 26 /
3 `A+^^>|A|>vv-A` 26 /
4 `A+>A-` 26 /

The first number is the angle of rotation.
The string is a specification of a path, along with coloring instructions.
The second number is the number of iteration.
The third number is the rotation of the final image.


Take path defined by input. Rotate path to start and end at the same height, with the end to the right of the start. Consider the line defined by such start and end. Now we've defined a correspondence between a line segment and a path composed of line segments. Take the path, replace each line segment with a copy of the path, rotated and scaled according to such correspondence. Repeat until you've done it iter many times.
As an example:
4 `A-A` 26 /
4 means the relevant angle is a quarter of a circle, i.e. 90 degrees.
With that angle, A-A means the path is something like:
`_`
` |`
Rotate so that it starts and ends at the same height. Scaled up by root(2) so ASCII can represent the 45 degree rotation, something like:
`/\`
Imagine the line segment connecting beginning and ending.
We can do the same in reverse to get a path from a line segment. Take the path, and do such to the line segments making it up. Something like:
`__`
`||`
According to the third piece of the input, this must be done 24 more times.
The last piece of the input specifies how much to rotate the product before display. Since it's a '/', we use the default rotation, which is none.
The starting path has its beginning and ending root(2) distance apart (where each line segment is 1 unit long), so we scale the final path such that the distance in pixels from final start to final end is the largest power of root(2) that still allows the whole path to fit on screen.

ToDos:
Explain flip operator ('|'). 
Explain hue, brightness.
Finish ToDo list.

o