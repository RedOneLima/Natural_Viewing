/*
  a TriList
  holds a list of
  Triangle instances,
  and is able to do
  OpenGL stuff to
  produce a VAO to
  draw its triangles
*/

import java.util.ArrayList;

//import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryUtil;

public class TriList {

  private ArrayList<Triangle> list;
  private int vao;  // handle for vertex array object prepared

  private int positionHandle, colorHandle;
  private FloatBuffer positionBuffer, colorBuffer;

  private boolean haveResources;  // note whether have resources to release

  public TriList() {
    list = new ArrayList<Triangle>();
    haveResources = false; // avoid trying to release resources on a new TriList
  }

  public void add( Triangle t ) {
    list.add( t );
  }
  
  public int size() {
    return list.size();
  }

  public Triangle get( int index ) {
    return list.get(index);
  }

  // set up all the OpenGL stuff to
  // draw the triangles
  // (call this whenever the list or the triangles in it
  //  has changed)
  public void prepare() {

    releaseResources();

    haveResources = true;  // will have resources shortly

    // create vertex buffer objects and their handles one at a time
    positionHandle = GL15.glGenBuffers();
    colorHandle = GL15.glGenBuffers();
//    System.out.println("have position handle " + positionHandle +
//                       " and color handle " + colorHandle );

    // set up buffers to hold all the data:
    // (9 values per triangle)
    FloatBuffer positionBuffer = MemoryUtil.memAllocFloat( list.size() * 9 );
                     // =  Util.createFloatBuffer( list.size() * 9 );
    FloatBuffer colorBuffer = MemoryUtil.memAllocFloat( list.size() * 9 );
                     // = Util.createFloatBuffer( list.size() * 9 );

    // copy data from list into buffers

    positionBuffer.rewind();
    colorBuffer.rewind();

    for( int k=0; k<list.size(); k++ ) {
      list.get( k ).copyData( positionBuffer, colorBuffer );
    }

    positionBuffer.rewind();
    colorBuffer.rewind();

    // debug output:  see what values are in the buffers
//     Util.showBuffer("position buffer: ", positionBuffer );  positionBuffer.rewind();
//     Util.showBuffer("color buffer: ", colorBuffer );  colorBuffer.rewind();

    // now connect the buffers
    GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, positionHandle );
             Util.error("after bind positionHandle");
    GL15.glBufferData( GL15.GL_ARRAY_BUFFER, 
                                     positionBuffer, GL15.GL_STATIC_DRAW );
             Util.error("after set position data");
    GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, colorHandle );
             Util.error("after bind colorHandle");
    GL15.glBufferData( GL15.GL_ARRAY_BUFFER, 
                                     colorBuffer, GL15.GL_STATIC_DRAW );
             Util.error("after set color data");

    // set up vertex array object

      // using convenience form that produces one vertex array handle
      vao = GL30.glGenVertexArrays();
           Util.error("after generate single vertex array");
      GL30.glBindVertexArray( vao );
           Util.error("after bind the vao");
//      System.out.println("vao is " + vao );

      // enable the vertex array attributes
      GL20.glEnableVertexAttribArray(0);  // position
             Util.error("after enable attrib 0");
      GL20.glEnableVertexAttribArray(1);  // color
             Util.error("after enable attrib 1");
  
      // map index 0 to the position buffer
      GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, positionHandle );
             Util.error("after bind position buffer");
      GL20.glVertexAttribPointer( 0, 3, GL11.GL_FLOAT, false, 0, 0 );
             Util.error("after do position vertex attrib pointer");

      // map index 1 to the color buffer
      GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, colorHandle );
             Util.error("after bind color buffer");
      GL20.glVertexAttribPointer( 1, 3, GL11.GL_FLOAT, false, 0, 0 );
             Util.error("after do color vertex attrib pointer");

  }// prepare

  // return resources used by a trilist
  public void releaseResources() {
    if( haveResources ) {// release the resources
      GL15.glDeleteBuffers( positionHandle );
      GL15.glDeleteBuffers( colorHandle );
      GL30.glDeleteVertexArrays( vao );

      MemoryUtil.memFree( positionBuffer );
      MemoryUtil.memFree( colorBuffer );

      haveResources = false;  // never had or have released
    }

  }// releaseResources

  public void draw()
  {
    // activate vao
    GL30.glBindVertexArray( vao );
           Util.error("after bind vao");

    // draw the triangles
    GL11.glDrawArrays( GL11.GL_TRIANGLES, 0, list.size()*3 );
           Util.error("after draw arrays");

  }// draw

  public TriList transform( Triple e, Triple a, Triple b, Triple c) {

    // do this work once for all the triangles:

    Triple eMinusA = e.minus( a );
     double len2ea = eMinusA.dot( eMinusA );
    Triple bMinusA = b.minus( a );
     double len2ba = bMinusA.dot( bMinusA );
    Triple cMinusA = c.minus( a );
     double len2ca = cMinusA.dot( cMinusA );

    System.out.println("compute all 9 relevant dot products:\n" +
       eMinusA.dot( eMinusA ) + " " + eMinusA.dot( bMinusA ) + " " + eMinusA.dot( cMinusA ) + "\n" +
       bMinusA.dot( eMinusA ) + " " + bMinusA.dot( bMinusA ) + " " + bMinusA.dot( cMinusA ) + "\n" +
       cMinusA.dot( eMinusA ) + " " + cMinusA.dot( bMinusA ) + " " + cMinusA.dot( cMinusA ) + "\n" );

    // for each triangle, produce transformed triangle

    TriList tempList = new TriList();

    for( int k=0; k<list.size(); k++ ) {
      Triangle t = list.get(k);
      tempList.add( new Triangle( 

                   new Vertex( Triple.transform( t.a.pos,
                    e,eMinusA,bMinusA,cMinusA,
                    len2ea,len2ba,len2ca), t.a.color ),

                   new Vertex( Triple.transform( t.b.pos,
                    e,eMinusA,bMinusA,cMinusA,
                    len2ea,len2ba,len2ca), t.b.color ),

                   new Vertex( Triple.transform( t.c.pos,
                    e,eMinusA,bMinusA,cMinusA,
                    len2ea,len2ba,len2ca), t.c.color )
                )
              );

    }// loop to transform all tris

    return tempList;

  }// transform

  public static void main(String[] args) {

    Triple e = new Triple( 50, 50, 100 );
    Triple a = new Triple( 50, 50, 99 );
    Triple b = new Triple( 51, 50, 99 );
    Triple c = new Triple( 50, 51, 99 );


    Triple eMinusA = e.minus( a );
     double len2ea = eMinusA.dot( eMinusA );
    Triple bMinusA = b.minus( a );
     double len2ba = bMinusA.dot( bMinusA );
    Triple cMinusA = c.minus( a );
     double len2ca = cMinusA.dot( cMinusA );

    System.out.println("compute all 9 relevant dot products:\n" +
       eMinusA.dot( eMinusA ) + " " + eMinusA.dot( bMinusA ) + " " + eMinusA.dot( cMinusA ) + "\n" +
       bMinusA.dot( eMinusA ) + " " + bMinusA.dot( bMinusA ) + " " + bMinusA.dot( cMinusA ) + "\n" +
       cMinusA.dot( eMinusA ) + " " + cMinusA.dot( bMinusA ) + " " + cMinusA.dot( cMinusA ) + "\n" );

    
    Triple p = new Triple( 45, 45, 0 );

    System.out.println("transformed p: " + Triple.transform( p, e,
                                                    eMinusA, bMinusA, cMinusA,
                                                    len2ea, len2ba, len2ca ) );
  }

}
