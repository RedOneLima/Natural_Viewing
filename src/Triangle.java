/* a triangle holds
  position and color
  data for three vertices
  of a simple (just color)
  triangle
*/

import java.util.Scanner;
import java.nio.FloatBuffer;

public class Triangle {

  public Vertex a, b, c;  // three vertices of the triangle

  // construct a triangle from input source
  public Triangle( Scanner input ) {
    a = new Vertex( input );
    b = new Vertex( input );
    c = new Vertex( input );
  }

  // construct a triangle from given Vertex's
  public Triangle( Vertex one, Vertex two, Vertex three ) {
    a = one;
    b = two;
    c = three;
  }

  // copy this triangle's data into the buffers
  // one vertex at a time
  public void copyData( FloatBuffer posBuffer, FloatBuffer colBuffer ) {
    a.copyData( posBuffer, colBuffer );
    b.copyData( posBuffer, colBuffer );
    c.copyData( posBuffer, colBuffer );
  }

  public String toString() {
    return "<" + a + " " + b + " " + c + ">";
  }

}
