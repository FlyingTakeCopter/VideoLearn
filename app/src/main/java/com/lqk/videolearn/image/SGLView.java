package com.lqk.videolearn.image;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.io.IOException;

public class SGLView extends GLSurfaceView {
    private SGLRender render;

    public SGLView(Context context) {
        super(context);
        init();
    }

    public SGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setEGLContextClientVersion(2);
        setRenderer(render = new SGLRender(getResources()));
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        try {
            render.setImage(BitmapFactory.decodeStream(getResources().getAssets().open("image/beauty.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SGLRender getRender() {
        return render;
    }
}
