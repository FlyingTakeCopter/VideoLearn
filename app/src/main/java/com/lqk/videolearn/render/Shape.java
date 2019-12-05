package com.lqk.videolearn.render;

import android.opengl.GLSurfaceView;
import android.view.View;

public abstract class Shape implements GLSurfaceView.Renderer {
    protected View mView;

    protected static final int FLOAT_SIZE = 4;

    public Shape(View mView) {
        this.mView = mView;
    }
}
