package com.lqk.videolearn.render;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.lqk.videolearn.R;
import com.lqk.videolearn.render.shape.Triangle;

public class FGLViewActivity extends AppCompatActivity {
    private final static String TAG = FGLViewActivity.class.getSimpleName();
    private FGLView glSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fglview);

        glSurfaceView = (FGLView) findViewById(R.id.fgl_gl_view);
        glSurfaceView.setShape(Triangle.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
}
