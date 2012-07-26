/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;


public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private Triangle mTriangle;
    private volatile Square mSquare; // Declare as synchronized to avoid setShader clobbering it whilst drawing

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    
    // Declare as volatile because we are updating it from another thread
    public volatile float mAngle;
    public boolean drawTriangle;
    
    public static final Shader[] mShaders = new Shader[8];
    public static int currentShader = 1;
    
    public static final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +

            "attribute vec4 vPosition;" +
            "void main() {" +
            // the matrix must be included as a modifier of gl_Position
            "  gl_Position = vPosition * uMVPMatrix;" +
            "}";
    
    private final String fragmentShaderCodeBasic =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";
    
    private final String fragmentShaderCodeGrey =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vec4(0.6,0.6,0.6,0.6);" +
            "}";
        

    private final String fragmentShaderCodeFun = 
   "	precision mediump float; "+

"    uniform float time;" +
"uniform vec4 vColor;" +
//"    uniform vec2 mouse; "+
//"    uniform vec2 resolution;  "+

"void main (void) { "+

"        vec2 position = gl_FragCoord.xy / vec2(1280.0,800.0); "+ // resolution.xy; "+

"        float t = mod(time/1.0,1.); " +
"        float t4 = mod(time/4.0,1.); " + 

"        float c1,c2,c3;" +
"        c1 = t/2.0; "+
"        c2 = mod(position.y,0.1111)/0.1111; "+
"        c3 = mod(position.x,0.0625)/0.0625; "+

"        float a = sin(39.14*distance(position.xy,vec2(t4,t4))); "+ 

"        if( mod(time,2.0) <= 1.0) { "+
"                gl_FragColor = vec4(c1,c2,a,a); "+
"        } else { "+
"                gl_FragColor = vec4(c2,c1,c3,a); "+
"        } "+
"}";

    private final String fragmentShaderCodeLeds =
"precision mediump float; "+

"uniform float time;" +
"uniform vec4 vColor;" +

"void main (void) { " +
"    vec2 v = gl_FragCoord.xy / vec2(1280.0,800.0); " +
"    float w, x, z = 0.0; " +
"    vec2 u; " +
"    vec3 c; " +
"    v *= 10.0; " +
"    u = floor(v) * 0.1 + vec2(20.0, 11.0); " +
"    u = u * u; " +
"    x = fract(u.x * u.y * 9.1 + time); " +
"    x *= (1.0 - length(fract(v) - vec2(0.5, 0.5)) * (2.0 + x)); " +
"    c = vec3(v * x, x); " +
"    gl_FragColor = vec4(c,1.0); " +
"}";

      
    public void setVariableShader(int i) {
    	currentShader++ ;
    	if(currentShader > 3) { 
    		currentShader = 0 ;
    	};
  	}
    
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // Enable Alpha/Blending (see also setEGLConfigChooser on the GLSurfaceView
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        
        mShaders[0] = new Shader(vertexShaderCode, fragmentShaderCodeBasic);
        mShaders[1] = new Shader(vertexShaderCode, fragmentShaderCodeFun);
        mShaders[2] = new Shader(vertexShaderCode, fragmentShaderCodeGrey);
        mShaders[3] = new Shader(vertexShaderCode, fragmentShaderCodeLeds);
        mTriangle = new Triangle();
        mSquare   = new Square();
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        // Draw square
        
        long time = SystemClock.uptimeMillis() % 16000L;
        float seconds = 0.001f * ((int) time);
        mSquare.draw(mMVPMatrix,seconds);
        
        // Create a rotation for the triangle
//        long time = SystemClock.uptimeMillis() % 4000L;
//        float angle = 0.090f * ((int) time);
        
        if(drawTriangle) {
        	Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

        	// Combine the rotation matrix with the projection and camera view
        	Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

        	// Draw triangle
        	mTriangle.draw(mMVPMatrix);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);
        
        //Log.d("MyGLRenderer:onSurfaceChanged", "W:" + width + " H:" + height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}

class Shader {
    public int mProgram;
    private int fShaderId;
    private int vShaderId;

    private final static String TAG = "Shader";

    public Shader (String vertexShaderCode, String fragmentShaderCode) {
        // prepare shaders and OpenGL program
        vShaderId = loadShader(GLES20.GL_VERTEX_SHADER,
                                                   vertexShaderCode);
        MyGLRenderer.checkGlError("loadShader vertex");
        fShaderId = loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                     fragmentShaderCode);
        MyGLRenderer.checkGlError("loadShader fragment");

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vShaderId);   // add the vertex shader to program
        MyGLRenderer.checkGlError("glAttachShader vertex");
        GLES20.glAttachShader(mProgram, fShaderId); // add the fragment shader to program
        MyGLRenderer.checkGlError("glAttachShader fragment");
        Log.e(TAG,"Attached Shader: " + fShaderId);
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
        	Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(mProgram));
            GLES20.glDeleteProgram(mProgram);
        }
    }
    
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + type + ":");
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }
}


class Triangle {  


    private final FloatBuffer vertexBuffer;
    private final String TAG = "MyGLRenderer::Triangle";
    private Shader s;
    private int mPositionHandle; // These should really be private with getters...
    private int mTimeHandle;
    private int mMVPMatrixHandle;
    private int mColorHandle;
    
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = { // in counterclockwise order:
         0.0f,  0.622008459f, 0.0f,   // top
        -0.5f, -0.311004243f, 0.0f,   // bottom left
         0.5f, -0.311004243f, 0.0f    // bottom right
    };
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 1.0f, 0.0f, 0.0f, 0.25f };

    public Triangle() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
                
    }

    public void draw(float[] mvpMatrix) {
    	
    	s = MyGLRenderer.mShaders[0];
    	int mProgram = s.mProgram;
    	
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        MyGLRenderer.checkGlError("glGetUniformLocation vPosition");

        // get handle to fragment shader's vColor member
        mTimeHandle = GLES20.glGetUniformLocation(mProgram, "time");
        MyGLRenderer.checkGlError("glGetUniformLocation time");
        
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation uMVPMatrix");
        
        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        MyGLRenderer.checkGlError("glGetUniformLocation mColor");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}



class Square {

	//    private final String fragmentShaderCode =
//       "precision mediump float;" +
//        "uniform vec4 vColor;" +
//        "void main() {" +
//        "  vec4 a = vColor;" +
//        "  gl_FragColor = vec4(sin(gl_FragCoord.xy), 0.0, 0.0); " +
//        "}";


    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final String TAG = "MyGLRenderer::Square";

    private Shader s;
    private int mPositionHandle; // These should really be private with getters...
    private int mTimeHandle;
    private int mMVPMatrixHandle;
    private int mColorHandle;
    
    
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = { -2.0f,  2.0f, 0.0f,   // top left
                                    -2.0f, -2.0f, 0.0f,   // bottom left
                                     2.0f, -2.0f, 0.0f,   // bottom right
                                     2.0f,  2.0f, 0.0f }; // top right

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    public Square() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        
    }

    public void draw(float[] mvpMatrix, float time) {
                	
        //Log.e(TAG,"In draw Square fShaderId: " + fShaderId + " mProgram: " + mProgram);
    	
    	s = MyGLRenderer.mShaders[MyGLRenderer.currentShader];
    	int mProgram = s.mProgram;
    	
    	// Add program to OpenGL environment
        GLES20.glUseProgram(s.mProgram);
        
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        MyGLRenderer.checkGlError("glGetUniformLocation vPosition");

        // get handle to fragment shader's vColor member
        mTimeHandle = GLES20.glGetUniformLocation(mProgram, "time");
        MyGLRenderer.checkGlError("glGetUniformLocation time");
        
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation uMVPMatrix");
        
        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        MyGLRenderer.checkGlError("glGetUniformLocation mColor");


        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);
        
        GLES20.glUniform1f(mTimeHandle, time );

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        
    }
}
