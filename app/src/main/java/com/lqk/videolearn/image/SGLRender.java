package com.lqk.videolearn.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;

import com.lqk.videolearn.image.filter.AFilter;
import com.lqk.videolearn.image.filter.ColorFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SGLRender implements GLSurfaceView.Renderer {
    Boolean refreshFlag = false;
    AFilter mFilterRender;
    EGLConfig mConfig;
    int width,height;
    Bitmap mBitmap;

    public SGLRender(Resources res) {
        mFilterRender = new ColorFilter(res, ColorFilter.Filter.NONE);
    }

    public void setImage(Bitmap bitmap){
        mFilterRender.setBitmap(bitmap);
        this.mBitmap = bitmap;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mConfig = config;
        mFilterRender.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        mFilterRender.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 切换filter重新初始化
        if (refreshFlag){
            mFilterRender.onSurfaceCreated(gl, mConfig);
            mFilterRender.onSurfaceChanged(gl, width, height);
            refreshFlag = false;
        }
        mFilterRender.onDrawFrame(gl);
    }

    public void setFilterRender(AFilter mFilterRender) {
        refreshFlag = true;
        this.mFilterRender = mFilterRender;
        if (mBitmap != null){
            mFilterRender.setBitmap(mBitmap);
        }
    }

    public AFilter getFilterRender() {
        return mFilterRender;
    }
}
