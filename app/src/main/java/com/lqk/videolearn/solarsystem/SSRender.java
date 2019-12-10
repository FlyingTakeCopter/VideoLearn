package com.lqk.videolearn.solarsystem;

import android.content.res.Resources;
import android.opengl.GLSurfaceView;

import com.lqk.videolearn.utils.VaryTools;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class SSRender implements GLSurfaceView.Renderer {
    VaryTools varyTools;

    Star sun;
    Star earth;
    Star moon;

    public SSRender(Resources res) {
        varyTools = new VaryTools();
        sun = new Star(res);
        earth = new Star(res);
        moon = new Star(res);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.5f,0.5f,0.5f,1.0f);
        sun.create();
        earth.create();
        moon.create();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0,0,width,height);
        float ratio = (float)width / height;
        varyTools.frustum(-ratio, ratio, -1, 1, 3, 300);
        varyTools.setCamera(0,0,250,
                0,0,0,
                0,1,0);
    }

    float angle = 0.0f;
    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        angle+=1;
        while (angle >= 360.0f) {
            angle -= 360.0f;
        }
        while (angle <= -360.0f) {
            angle += 360.0f;
        }

        // sun
        varyTools.pushMatrix();//存储原始坐标
        varyTools.scale(1.5f,1.5f,1.5f);
        sun.setDisplayMatrix(varyTools.getFinalMatrix());
        sun.drawSelf();
        varyTools.pushMatrix();//储存太阳坐标

        // earth
        varyTools.rotate(angle, 0,0,1);
        varyTools.translate(30,0,0);
        varyTools.scale(0.5f,0.5f,0.5f);
        earth.setDisplayMatrix(varyTools.getFinalMatrix());
        earth.drawSelf();
        varyTools.pushMatrix();//储存地球坐标

        // moon
        varyTools.rotate(angle, 0,0,1);
        varyTools.translate(10,0,0);
        varyTools.scale(0.3f,0.3f,0.3f);
        moon.setDisplayMatrix(varyTools.getFinalMatrix());
        moon.drawSelf();

        varyTools.popMatrix();
        varyTools.popMatrix();
        varyTools.popMatrix();
    }
}
