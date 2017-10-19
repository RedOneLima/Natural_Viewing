/*  
   view a scene whose
   data comes from a data file
   using interactive view setup
   creation
*/

import java.io.File;
import java.util.Scanner;

import org.lwjgl.opengl.*;

import org.lwjgl.glfw.*;
import org.lwjgl.glfw.GLFW;

public class Viewer2 extends Basic
{
  public static void main(String[] args)
  {
//    if( args.length != 1 ) {
//      System.out.println("Usage:  j Viewer2 world1");
//      System.exit(1);
//    }
    Viewer2 app = new Viewer2( "View a Scene", 500, 500, 30, "world1" );
    app.start();
  }// main

  // instance variables 

  private Shader v1, f1;
  private int hp1;

  private int vao;  // handle to the vertex array object

  private TriList sceneTris, transformedTris;

  // viewing setup parameters
  private Triple eye;
  private double azimuth, altitude, distance;

  // construct basic application with given title, pixel width and height
  // of drawing area, and frames per second
  public Viewer2( String appTitle, int pw, int ph, int fps, String fileName )
  {
    super( appTitle, pw, ph, (long) ((1.0/fps)*1000000000) );

    // initial viewing setup
    eye = new Triple( 50, 0, 0 );
    azimuth = 90;  
    altitude = 0;
    distance = 2;    

    // get model triangle data from file
    Scanner input;
    try {
      input = new Scanner( new File( fileName ) );
      
      // read number of triangles
      int num = input.nextInt();  input.nextLine();

      sceneTris = new TriList();

      for( int k=0; k<num; k++ ) {
        Triangle tri = new Triangle( input );
        // System.out.println("got triangle " + k + ": " + tri );
        sceneTris.add( tri );
      }
    }
    catch( Exception exc ) {
      System.out.println("problem loading data file");
      exc.printStackTrace();
      System.exit(1);
    }

    updateView();

  }// constructor

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

    // set up depth testing so that closer to 1 is in front,
    // closer to -1 is behind
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

      if( info.kind == 'k' && (info.action == GLFW.GLFW_PRESS || 
                               info.action == GLFW.GLFW_REPEAT) )
      {
        int code = info.code;
        int mods = info.mods;  // shift is bit 0, ctrl is bit 1,
                               // option is bit 2

        if( code == GLFW.GLFW_KEY_X && mods == 0 ) {// x
          eye = eye.plus( -1, 0, 0 );
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_X && mods == 1 ) {// X
          eye = eye.plus( 1, 0, 0 );
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_Y && mods == 0 ) {// y
          eye = eye.plus( 0, -1, 0 );
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_Y && mods == 1 ) {// Y
          eye = eye.plus( 0, 1, 0 );
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_Z && mods == 0 ) {// z
          eye = eye.plus( 0, 0, -1 );
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_Z && mods == 1 ) {// Z
          eye = eye.plus( 0, 0, 1 );
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_LEFT && mods == 0 ) {
          azimuth += 5;
          if( azimuth >= 360 ) azimuth -= 360;
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_RIGHT && mods == 0 ) {
          azimuth -= 5;
          if( azimuth < 0 ) azimuth += 360;
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_U && mods == 0 ) {// u
          altitude += 1;
          if( altitude > 89 ) altitude = 89;
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_D && mods == 0 ) {// d
          altitude -= 1;
          if( altitude < -89 ) altitude = -89;
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_F && mods == 0 ) {// f (farther)
          distance += 0.1;
          updateView();
        }
        else if( code == GLFW.GLFW_KEY_N && mods == 0 ) {// n (nearer)
          distance -= 0.1;
          if( distance < 0.1 ) distance = 0.1;
          updateView();
        }

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
//    System.out.println( "Step number:  " + getStepNumber() );

    GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );

    transformedTris.prepare();
    transformedTris.draw();

  }

  // using eye, azimuth, altitude, distance
  // generate e, a, b, c and create transformedTris
  // from modelTris
  private void updateView() {

    System.out.println("--------------------------------\n" +
                        "Eye: " + eye + " azi: " + azimuth + " alt: " +
                         altitude + " d: " + distance );

    Triple e, a, b, c;

    e = eye;  // just notation

    double azi = Math.toRadians(azimuth);
    double alt = Math.toRadians(altitude);
    double cosAzi = Math.cos( azi ), sinAzi = Math.sin( azi );
    double cosAlt = Math.cos( alt ), sinAlt = Math.sin( alt );
    
    // length is distance
    Triple aMinusE = new Triple( cosAlt*distance*cosAzi,
                           cosAlt*distance*sinAzi,
                           distance*sinAlt );
    a = e.plus(aMinusE);
System.out.println("A: " + a );
    Triple eMinusA = e.minus(a);
System.out.println("E-A: " + eMinusA );

    Triple bMinusA = Triple.zAxis.cross( eMinusA );
    bMinusA = bMinusA.normalized(); // make b-a have length 1
    b = a.plus(bMinusA);

    Triple cMinusA = eMinusA.cross( bMinusA );
    cMinusA = cMinusA.normalized();  // make c-a have length 1
    c = a.plus(cMinusA);
    
    System.out.println("dot products:\n" + 
      eMinusA.dot(eMinusA) + " " + eMinusA.dot(bMinusA) + " " + eMinusA.dot(cMinusA) + "\n" +
      bMinusA.dot(eMinusA) + " " + bMinusA.dot(bMinusA) + " " + bMinusA.dot(cMinusA) + "\n" +
      cMinusA.dot(eMinusA) + " " + cMinusA.dot(bMinusA) + " " + cMinusA.dot(cMinusA) + "\n" );
      
    // if transformedTris is pointing to a TriList instance,
    // make sure it has released its resources before going away
    if( transformedTris != null )
      transformedTris.releaseResources();

    transformedTris = sceneTris.transform( e, a, b, c );

    /* // debug output
    System.out.println("transformed scene triangles: " );
    for( int k=0; k<transformedTris.size(); k++ ) {
      System.out.println( transformedTris.get(k) );
    }
    */

  }// updateView

}// Viewer2
