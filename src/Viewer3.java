/*  
   view a scene whose
   data comes from a data file
   using interactive view setup
   creation
   and doing view transformations
   in the GPU using uniform variables
*/

import java.util.Random;

import org.lwjgl.opengl.*;

import org.lwjgl.glfw.GLFW;

public class Viewer3 extends Basic
{
    private Shader vertexShaderObject, fragmentShaderObject;
    private int programHandle;
    private TriList sceneTris;
    //private int vertexArrayObject;  -->not used

    // viewing setup parameters
    private Triple eye;
    private double azimuth, altitude, distance, speed=0;

    // CPU variables to feed the uniform variables
    private Triple eyePoint, eMinusA, bMinusA, cMinusA, aMinusE;
    //private FloatBuffer eBuff, eaBuff, baBuff, caBuff;  -->not used

    // locations of the uniform variables:
    private int eLoc, eaLoc, baLoc, caLoc;

    public static void main(String[] args)
    {
        Viewer3 app = new Viewer3( "Exercise 5", 1700, 1000, 30, "world1" );
        app.start();
    }

    public Viewer3( String appTitle, int pw, int ph, int fps, String fileName )
    {// construct basic application with given title, pixel width and height of drawing area, and frames per second
        super( appTitle, pw, ph, (long) ((1.0/fps)*1000000000) );

        // initial viewing setup
        eye = new Triple( -40, -26, 118 );
        azimuth = 40;
        altitude = -20;
        distance = 2;
        sceneTris = new TriList();
        //TODO this is where the calls to build the scene should go
        /* To create a triangle in TriList:
                Triple -> Vertex -> Triangle -> TriList
        */
        Random random = new Random();
        drawEnviroment();
            Triple randomColor;
            double locationY = 5;
            double locationX = 0;
            for(int i = 0; i<20; i++) {
                if(i%5==0){
                    locationX += 20;
                    locationY = 5;
                }
                randomColor = new Triple(random.nextDouble(), random.nextDouble(), (random.nextDouble()));
                building(10, 10, random.nextInt(25) + 15, locationX+50, locationY+50, randomColor);
                locationY += 20;


            }
        updateView();


    }

    protected void init()
    {
    //--------------------------------  Shader Code  -------------------------------------------------------
    //------------------------------------------------------------------------------------------------------

    String vertexShaderCode =
        "#version 330 core\n"+
        "layout (location = 0 ) in vec3 vertexPosition;\n"+
        "layout (location = 1 ) in vec3 vertexColor;\n"+
        "uniform vec3 eyePoint;\n"+
        "uniform vec3 eMinusA;\n"+
        "uniform vec3 bMinusA;\n"+
        "uniform vec3 cMinusA;\n"+
        "out vec3 color;\n"+
        "void main(void)\n"+
        "{\n"+
        "  color = vertexColor;\n"+
        "  vec3 p = vertexPosition;\n" +
        "  vec3 pMinusE = p-eyePoint;\n" +
        "  float lambda = - (dot(eMinusA,eMinusA) / dot(eMinusA,pMinusE) );\n" +
        "  float beta = lambda * ( dot(bMinusA,pMinusE) / dot(bMinusA,bMinusA) );\n" +
        "  float gamma = lambda * ( dot(cMinusA,pMinusE) / dot(cMinusA,cMinusA) );\n" +
        "  gl_Position = vec4(beta,gamma,2*lambda-1,1.0);\n"+
        "}\n";

    System.out.println("Vertex shader:\n" + vertexShaderCode + "\n\n" );

    vertexShaderObject = new Shader( "vertex", vertexShaderCode );

    String fragmentShaderCode =
        "#version 330 core\n"+
        "in vec3 color;\n"+
        "layout (location = 0 ) out vec4 fragColor;\n"+
        "void main(void)\n"+
        "{\n"+
        "  fragColor = vec4(color, 1.0 );\n"+
        "}\n";

    System.out.println("Fragment shader:\n" + fragmentShaderCode + "\n\n" );

    fragmentShaderObject = new Shader( "fragment", fragmentShaderCode );


    //-----------------------------------------  End Shader Code  ----------------------------------------
    //----------------------------------------------------------------------------------------------------

    //*****************************************glCreateProgram*************************************************************

    /*glCreateProgram creates an empty program object and returns a non-zero value by which it can be referenced.
      A program object is an object to which shader objects can be attached.
      This provides a mechanism to specify the shader objects that will be linked to create a program.
      It also provides a means for checking the compatibility of the shaders that will be used to create a program
    */
    programHandle = GL20.glCreateProgram();
        //Checks for openGL errors...prints 'Got OpenGL Error at '#####''
         Util.error("after create program");
         System.out.println("program handle is " + programHandle);

    //*****************************************glAttachShader**************************************************************

     /*In order to create a complete shader program, there must be a way to specify the list of things that will be linked together.
       Program objects provide this mechanism. Shaders that are to be linked together in a program object must first
       be attached to that program object.
       glAttachShader attaches the shader object specified by shader to the program object specified by program.
       This indicates that shader will be included in link operations that will be performed on program.
     */
    GL20.glAttachShader(programHandle, vertexShaderObject.getHandle() );
         Util.error("after attach vertex shader to program");

    GL20.glAttachShader(programHandle, fragmentShaderObject.getHandle() );
         Util.error("after attach fragment shader to program");

    //*****************************************glLinkProgram***************************************************************

     /*Links the program object specified by program.
       If any shader objects of type GL_VERTEX_SHADER/GL_FRAGMENT_SHADER are attached to program,
          they will be used to create an executable that will run on the programmable vertex/fragment processor.
     */
    GL20.glLinkProgram(programHandle);
         Util.error("after link program" );

    //*****************************************glUseProgram****************************************************************

     /*Installs the program object specified by program as part of current rendering state.
       One or more executables are created in a program object by successfully attaching shader objects to it
            with glAttachShader, successfully compiling the shader objects with glCompileShader, and successfully
            linking the program object with glLinkProgram.
     */
    GL20.glUseProgram(programHandle);
         Util.error("after use program");
    //*****************************************glGetUniformLocation********************************************************

    /*Returns an integer that represents the location of a specific uniform variable within a program object.
      Name must be a String that contains no white space.
      Name must be an active uniform variable name in program that is not a structure, an array of structures,
          or a subcomponent of a vector or a matrix.
    */
    eLoc = GL20.glGetUniformLocation(programHandle, "eyePoint" );
          Util.error("get loc of eyePoint");
    eaLoc = GL20.glGetUniformLocation(programHandle, "eMinusA" );
    baLoc = GL20.glGetUniformLocation(programHandle, "bMinusA" );
    caLoc = GL20.glGetUniformLocation(programHandle, "cMinusA" );
        System.out.println("Found locations: " + eLoc + " " + eaLoc + " " + baLoc + " " + caLoc );

    sendUniformData();

    // set up depth testing so that closer to 1 is in front,
    // closer to -1 is behind
    GL11.glEnable( GL11.GL_DEPTH_TEST ); //Do depth comparisons and update the depth buffer.
    GL11.glClearDepth( -1.0f );    //Specifies the depth value used when the depth buffer is cleared.

    //Specifies the function used to compare each incoming pixel depth value with the depth value present in the depth buffer.
    //The comparison is performed only if depth testing is enabled.
    GL11.glDepthFunc( GL11.GL_GREATER );//Passes if the incoming depth value is greater than the stored depth value.

    // Specify the red, green, blue, and alpha values used when the color buffers are cleared. The initial values are all 0.
    GL11.glClearColor( 1.0f, 1.0f, 1.0f, 0.0f );

    // set up GPU with sceneTris once and for all:
    // (can do because no triangle ever changes)
    sceneTris.prepare();

    }

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
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_X && mods == 1 ) {// X
          eye = eye.plus( 1, 0, 0 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_Y && mods == 0 ) {// y
          eye = eye.plus( 0, -1, 0 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_Y && mods == 1 ) {// Y
          eye = eye.plus( 0, 1, 0 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_Z && mods == 0 ) {// z
            eye = eye.plus(0, 0, -1);
            updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_Z && mods == 1 ) {// Z
          eye = eye.plus( 0, 0, 1 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_LEFT && mods == 0 ) {
          azimuth += 5;
          if( azimuth >= 360 ) azimuth -= 360;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_RIGHT && mods == 0 ) {
          azimuth -= 5;
          if( azimuth < 0 ) azimuth += 360;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_U && mods == 0 ) {// u
          altitude += 5;
          if( altitude > 89 ) altitude = 89;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_D && mods == 0 ) {// d
          altitude -= 5;
          if( altitude < -89 ) altitude = -89;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_F && mods == 0 ) {// f (farther)
          distance += 0.1;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_N && mods == 0 ) {// n (nearer)
          distance -= 0.1;
          if( distance < 0.1 ) distance = 0.1;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_UP){
            speed += 1;
            if(speed > 10) speed= 10;
            System.out.println("Speed: "+speed);
        }
        else if( code == GLFW.GLFW_KEY_DOWN){
            speed -= 1;
            if(speed < -10) speed = -10;
            System.out.println("Speed: "+speed);
        }
        else if (code == GLFW.GLFW_KEY_R){
            eye = new Triple( -40, -26, 118 );
            azimuth = 40;
            altitude = -20;
            distance = 2;
            speed = 0;
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

    protected void update(){
        eye = eye.plus(aMinusE.mult(speed*.1));
        updateViewAndSend();

    }

    protected void display()
    {
        //Bitwise OR of masks that indicate the buffers to be cleared
        GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );
                            //                               ^Indicates the depth buffer
                            //   ^ Indicates the buffers currently enabled for color writing
        sceneTris.draw(); //draws the triangles in the list of triangles
    }

    private void updateView()
    { // using eye, azimuth, altitude, distance, generate eyePoint, a, b, c
    System.out.println("--------------------------------\n" +
                        "Eye: " + eye + " azi: " + azimuth + " alt: " +
                         altitude + " d: " + distance );

    Triple a, b, c;

    eyePoint = eye;  // just notation
    double azi = Math.toRadians(azimuth);
    double alt = Math.toRadians(altitude);
    double cosAzi = Math.cos(azi), sinAzi = Math.sin(azi);
    double cosAlt = Math.cos(alt), sinAlt = Math.sin(alt);

    // length is distance
    aMinusE = new Triple(cosAlt * distance * cosAzi,
            cosAlt * distance * sinAzi,
            distance * sinAlt);
    a = eyePoint.plus(aMinusE);
    System.out.println("A: " + a);
    eMinusA = eyePoint.minus(a);
    System.out.println("E-A: " + eMinusA);

    bMinusA = Triple.zAxis.cross(eMinusA);
    bMinusA = bMinusA.normalized(); // make b-a have length 1
    b = a.plus(bMinusA);

    cMinusA = eMinusA.cross(bMinusA);
    cMinusA = cMinusA.normalized();  // make c-a have length 1
    c = a.plus(cMinusA);

    }

    private void sendUniformData()
    {
    System.out.println("about to send data to uniforms" );

    // send viewing info to GPU:
    // (send: E, E-A, B-A, C-A)
    GL20.glUniform3fv( eLoc, eyePoint.toBuffer() );
    GL20.glUniform3fv( eaLoc, eMinusA.toBuffer() );
    GL20.glUniform3fv( baLoc, bMinusA.toBuffer() );
    GL20.glUniform3fv( caLoc, cMinusA.toBuffer() );
    System.out.println("finished sending data to uniforms");

    }

    private void updateViewAndSend()
    {
    updateView();
    sendUniformData();
    }

    private void drawEnviroment()
    {
       sceneTris.add(new Triangle(
                new Vertex(new Triple(50,50,0.1), new Triple(.588,.588,.588)),
                new Vertex(new Triple(150,50,0.1),new Triple(.588,.588,.588)),
                new Vertex(new Triple(150,150,0.1),new Triple(.588,.588,.588))));
        sceneTris.add(new Triangle(
                new Vertex(new Triple(50,50,0.1), new Triple(.588,.588,.588)),
                new Vertex(new Triple(50,150,0.1), new Triple(.588,.588,.588)),
                new Vertex(new Triple(150,150,0.1), new Triple(.588,.588,.588))
        ));
        sceneTris.add(new Triangle(
                new Vertex(new Triple(0,0,0), new Triple(.19608,.67843,.19608)),
                new Vertex(new Triple(300,0,0),new Triple(.19608,.67843,.19608)),
                new Vertex(new Triple(300,300,0),new Triple(.19608,.67843,.19608))));
        sceneTris.add(new Triangle(
                new Vertex(new Triple(0,0,0), new Triple(.19608,.67843,.19608)),
                new Vertex(new Triple(0,300,0), new Triple(.19608,.67843,.19608)),
                new Vertex(new Triple(300,300,0), new Triple(.19608,.67843,.19608))
        ));
    }

    private void building(double width,double length,double height,double locationX, double locationY, Triple color)
    {
        /*  width  || to x axis
            length || to y axis
            height || to z axis
        */

        //Top
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX-width/2,locationY+length/2, height), color),
                        new Vertex(new Triple(locationX+width/2,locationY+length/2, height),color),
                        new Vertex(new Triple(locationX-width/2,locationY-length/2, height),color)
                    )
        );
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX+width/2,locationY+length/2, height), color),
                        new Vertex(new Triple(locationX+width/2,locationY-length/2, height),color),
                        new Vertex(new Triple(locationX-width/2,locationY-length/2, height),color)
                )
        );

        //wall 1
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX-width/2,locationY+length/2, height), color),
                        new Vertex(new Triple(locationX-width/2,locationY+length/2, 0),color),
                        new Vertex(new Triple(locationX+width/2,locationY+length/2, 0),color)
                )
        );
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX-width/2,locationY+length/2, height), color),
                        new Vertex(new Triple(locationX+width/2,locationY+length/2, height),color),
                        new Vertex(new Triple(locationX+width/2,locationY+length/2, 0),color)
                )
        );

        //wall 2
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX-width/2,locationY+length/2, height), color),
                        new Vertex(new Triple(locationX-width/2,locationY-length/2, height),color),
                        new Vertex(new Triple(locationX-width/2,locationY+length/2, 0),color)
                )
        );
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX-width/2,locationY-length/2, height), color),
                        new Vertex(new Triple(locationX-width/2,locationY+length/2, 0),color),
                        new Vertex(new Triple(locationX-width/2,locationY-length/2, 0),color)
                )
        );

        //wall 3
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX-width/2,locationY-length/2, height), color),
                        new Vertex(new Triple(locationX+width/2,locationY-length/2, height),color),
                        new Vertex(new Triple(locationX+width/2,locationY-length/2, 0),color)
                )
        );
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX-width/2,locationY-length/2, 0), color),
                        new Vertex(new Triple(locationX+width/2,locationY-length/2, 0),color),
                        new Vertex(new Triple(locationX-width/2,locationY-length/2, height),color)
                )
        );
        //wall 4
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX+width/2,locationY+length/2, height), color),
                        new Vertex(new Triple(locationX+width/2,locationY-length/2, height),color),
                        new Vertex(new Triple(locationX+width/2,locationY-length/2, 0),color)
                )
        );
        sceneTris.add(new Triangle(
                        new Vertex(new Triple(locationX+width/2,locationY+length/2, 0), color),
                        new Vertex(new Triple(locationX+width/2,locationY-length/2, 0),color),
                        new Vertex(new Triple(locationX+width/2,locationY+length/2, height),color)
                )
        );
    }

}// Viewer3
