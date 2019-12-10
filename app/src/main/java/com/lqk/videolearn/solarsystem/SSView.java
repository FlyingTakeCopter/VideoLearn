package com.lqk.videolearn.solarsystem;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class SSView extends GLSurfaceView {
    SSRender render;
    Context context;

    public SSView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SSView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init(){
        setEGLContextClientVersion(2);
        setRenderer(render = new SSRender(context.getResources()));
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
}
