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

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class OpenGLES20Complete extends Activity {

    protected static final String TAG = "GLES20Complete";
	private GLSurfaceView mGLView;
    private MyGLRenderer mRenderer;
    
    private float mPreviousX;
    private float mPreviousY;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;

    
    private OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent e) {
	        // MotionEvent reports input details from the touch screen
	        // and other input controls. In this case, you are only
	        // interested in events where the touch position changed.

	        float x = e.getX();
	        float y = e.getY();

	        switch (e.getAction()) {
	            case MotionEvent.ACTION_MOVE:

	                float dx = x - mPreviousX;
	                float dy = y - mPreviousY;
//	                Log.d(TAG, "Touch x:" +x + " y:" +y + " dx:" + dx + " dy:" +dy);

	                // reverse direction of rotation above the mid-line
	                if (y > v.getHeight() / 2) {
	                  dx = dx * -1 ;
	                }

	                // reverse direction of rotation to left of the mid-line
	                if (x < v.getWidth() / 2) {
	                  dy = dy * -1 ;
	                }

	                mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
	                ((GLSurfaceView) v).requestRender();
	        }

	        mPreviousX = x;
	        mPreviousY = y;
	        return true;
		}
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.main);
        
        GLSurfaceView mGLView = (GLSurfaceView) this.findViewById(R.id.glSurface);
        if (mGLView != null)
        {
        	mGLView.setEGLContextClientVersion(2);
        	mGLView.setEGLConfigChooser(8, 8, 8, 8, 0, 0); // RGBA_8888 no depth/stencil buffer
        	
        	// Malc Debug
//          mGLView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);

        	mRenderer = new MyGLRenderer();
            mGLView.setRenderer(mRenderer);
            mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            mGLView.setOnTouchListener(mTouchListener);
                                   
    		((Button) this.findViewById(R.id.leftButton)).setOnClickListener(new View.OnClickListener() {
    			public void onClick(View v) {
    				mRenderer.drawTriangle = ! mRenderer.drawTriangle;
    			}
    		});       
        }
       
    }


    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        if (mGLView != null) {
        	mGLView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        if (mGLView != null) {
        	mGLView.onResume();
        }
    }
}
/*
class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                  dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                  dy = dy * -1 ;
                }

                mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
    
    
}
*/