/* 
 data for
 one point with color
*/

import java.util.Scanner;
import java.nio.FloatBuffer;

public class Vertex {

  public Triple pos;
  public Triple color;

  public Vertex( Scanner input ) {
    pos = new Triple( input );  input.nextLine();
    color = new Triple( input );  input.nextLine();
  }

  public Vertex( Triple p, Triple c ) {
    pos = p;
    color = c;
  }

  // copy this vertex's data into the buffers
  public void copyData( FloatBuffer posBuffer, FloatBuffer colBuffer ) {
    posBuffer.put( (float) pos.x ); 
    posBuffer.put( (float) pos.y ); 
    posBuffer.put( (float) pos.z );
    colBuffer.put( (float) color.x ); 
    colBuffer.put( (float) color.y ); 
    colBuffer.put( (float) color.z );
  }

  public String toString() {
    return "{" + pos + " " + color + "}";
  }

}
