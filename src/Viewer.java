/*  
   given some hard-coded viewing setup
   choices, compute the rest of the
   viewing setup and view a scene whose
   data comes from a data file
*/

import java.io.File;
import java.util.Scanner;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

 import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
 import static org.lwjgl.system.MemoryUtil.*;

public class Viewer extends Basic
{
  public static void main(String[] args)
  {
    if( args.length != 1 ) {
      System.out.println("Usage:  j Viewer world1");
      System.exit(1);
    }
    Viewer app = new Viewer( "View a Scene", 500, 500, 30, args[0] );
    app.start();
  }// main

  // instance variables 

  private FloatBuffer backColor;
  private Shader v1, f1;
  private int hp1;

  private int vao;  // handle to the vertex array object

  private TriList sceneTris, transformedTris;

  // construct basic application with given title, pixel width and height
  // of drawing area, and frames per second
  public Viewer( String appTitle, int pw, int ph, int fps, String fileName )
  {
    super( appTitle, pw, ph, (long) ((1.0/fps)*1000000000) );

    // create viewing info in a purely hard-code way:

/* test 1: check!
    Triple e = new Triple( 50, 50, 100 );
    Triple a = new Triple( 50, 50, 99 );
    Triple b = new Triple( 51, 50, 99 );
    Triple c = new Triple( 50, 51, 99 );
*/
/*  test 2
    Triple e = new Triple( 50, 50, 11 );
    Triple a = new Triple( 50, 50, 10 );
    Triple b = new Triple( 51, 50, 10 );
    Triple c = new Triple( 50, 51, 10 );
*/
/*
    Triple e = new Triple( 25, 25, 0 );
    Triple a = new Triple( 26, 26, 0 );
    Triple b = new Triple( 27, 25, 0 );
    Triple c = new Triple( 26, 26, 1 );
*/
    Triple e = new Triple( 50, 50, -11 );
    Triple a = new Triple( 50, 50, -10 );
    Triple b = new Triple( 51, 50, -10 );
    Triple c = new Triple( 50, 51, -10 );



    // get triangle data from file
    Scanner input;
    try {
      input = new Scanner( new File( fileName ) );
      
      // read number of triangles
      int num = input.nextInt();  input.nextLine();

      sceneTris = new TriList();

      for( int k=0; k<num; k++ ) {
        Triangle tri = new Triangle( input );
System.out.println("got triangle " + k + ": " + tri );
        sceneTris.add( tri );
      }
    }
    catch( Exception exc ) {
      System.out.println("problem loading data file");
      exc.printStackTrace();
      System.exit(1);
    }

    // transform scene triangles
    transformedTris = sceneTris.transform( e, a, b, c );

    System.out.println("transformed scene triangles: " );
    for( int k=0; k<transformedTris.size(); k++ ) {
      System.out.println( transformedTris.get(k) );
    }

  }

  protected void init()
  {
    String vertexShaderCode =
"#version 330 core\n"+
"layout (location = 0 ) in vec3 vertexPosition;\n"+
"layout (location = 1 ) in vec3 vertexColor;\n"+
"out vec3 color;\n"+
"void main(void)\n"+
"{\n"+
"  color = vertexColor;\n"+
"  gl_Position = vec4( vertexPosition, 1.0);\n"+
"}\n";

    System.out.println("Vertex shader:\n" + vertexShaderCode + "\n\n" );

    v1 = new Shader( "vertex", vertexShaderCode );

    String fragmentShaderCode =
"#version 330 core\n"+
"in vec3 color;\n"+
"layout (location = 0 ) out vec4 fragColor;\n"+
"void main(void)\n"+
"{\n"+
"  fragColor = vec4(color, 1.0 );\n"+
"}\n";

    System.out.println("Fragment shader:\n" + fragmentShaderCode + "\n\n" );

    f1 = new Shader( "fragment", fragmentShaderCode );

    hp1 = GL20.glCreateProgram();
         Util.error("after create program");
         System.out.println("program handle is " + hp1 );

    GL20.glAttachShader( hp1, v1.getHandle() );
         Util.error("after attach vertex shader to program");

    GL20.glAttachShader( hp1, f1.getHandle() );
         Util.error("after attach fragment shader to program");

    GL20.glLinkProgram( hp1 );
         Util.error("after link program" );

    GL20.glUseProgram( hp1 );
         Util.error("after use program");

    GL11.glEnable( GL11.GL_DEPTH_TEST );
    GL11.glClearDepth( -1.0f );
    GL11.glDepthFunc( GL11.GL_GREATER );

    GL11.glClearColor( 1.0f, 1.0f, 1.0f, 0.0f );

  }// init

  protected void processInputs()
  {
    // process all waiting input events
    while( InputInfo.size() > 0 )
    {
      InputInfo info = InputInfo.get();

      if( info.kind == 'k' && (info.action == GLFW_PRESS || 
                               info.action == GLFW_REPEAT) )
      {
        int code = info.code;

      }// input event is a key

      else if ( info.kind == 'm' )
      {// mouse moved
      //  System.out.println( info );
      }

      else if( info.kind == 'b' )
      {// button action
       //  System.out.println( info );
      }

    }// loop to process all input events

  }

  protected void update()
  {
  }

  protected void display()
  {
    System.out.println( "Step number:  " + getStepNumber() );

    GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );

    transformedTris.prepare();
    transformedTris.draw();

  }

}// Viewer
