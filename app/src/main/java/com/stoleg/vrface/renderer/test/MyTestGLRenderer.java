package com.stoleg.vrface.renderer.test;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.stoleg.vrface.Static;
import com.stoleg.vrface.model3d.test.CubeTest;

/**
 * Created by sov on 10.12.2016.
 */

public class MyTestGLRenderer implements GLSurfaceView.Renderer {

    private CubeTest mCube = new CubeTest();
    private float mCubeRotation;


    private static final String TAG = "MyGLRenderer_class";

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        //GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_NICEST);
    }

    public void onDrawFrame(GL10 gl) {
        if (Static.LOG_MODE) Log.i(TAG, "onDrawFrame");
        // Redraw background color
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -10.0f);
        gl.glRotatef(mCubeRotation, 1.0f, 1.0f, 1.0f);

        mCube.draw(gl);

        gl.glLoadIdentity();

        mCubeRotation -= 0.15f;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (Static.LOG_MODE) Log.i(TAG, "onSurfaceChanged");
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
}