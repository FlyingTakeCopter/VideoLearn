package com.lqk.videolearn.render;

import android.view.View;

import com.lqk.videolearn.render.shape.Triangle;

import java.lang.reflect.Constructor;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class FGLRender extends Shape {
    //绘制的形状
    private Shape mShape;
    // TODO 新技能GET
    //绘制的类型，此处写的很好，外面构造类，只是把类型传进来，
    private Class<? extends Shape> clazz;

    public FGLRender(View view) {
        super(view);
    }

    public void setShape(Class<? extends Shape> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        glClearColor(0.5f,0.5f,0.5f,1.0f);

        // 反射 构造shape
        try {
            Constructor constructor = clazz.getDeclaredConstructor(View.class);
            constructor.setAccessible(true);
            mShape = (Shape) constructor.newInstance(mView);
        }catch (Exception e) {
            e.printStackTrace();
            mShape = new Triangle(mView);
        }

        mShape.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        glViewport(0,0,width,height);

        mShape.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        mShape.onDrawFrame(gl);
    }
}
