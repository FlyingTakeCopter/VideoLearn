package com.lqk.ndk_glv3;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NdkGLSurfaceView extends GLSurfaceView {

    private NativeRender mNativeRender;

    private MyGlRender mGlRender;

    public NdkGLSurfaceView(Context context) {
        super(context);
    }

    public NdkGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(){
        setEGLContextClientVersion(3);
        mNativeRender = new NativeRender();
        mGlRender = new MyGlRender(mNativeRender);
        setRenderer(mGlRender);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    public static class MyGlRender implements GLSurfaceView.Renderer{
        private NativeRender mNativeRender;

        public MyGlRender(NativeRender mNativeRender) {
            this.mNativeRender = mNativeRender;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mNativeRender.native_onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mNativeRender.native_onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mNativeRender.native_onDrawFrame();
        }
    }
}
