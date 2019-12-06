package com.lqk.videolearn.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class FGLView extends GLSurfaceView {
    private FGLRender renderer;

    public FGLView(Context context) {
        super(context);
        init();
    }

    public FGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setEGLContextClientVersion(2);
        setRenderer(renderer = new FGLRender(this));
        // RENDERMODE_WHEN_DIRTY:表示只有在调用requestRender或者onResume等方法时才会进行渲染
        // RENDERMODE_CONTINUOUSLY:表示持续渲染
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void setShape(Class<? extends Shape> clazz) {
        renderer.setShape(clazz);
    }

}
