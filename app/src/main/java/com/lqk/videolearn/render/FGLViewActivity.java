package com.lqk.videolearn.render;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lqk.videolearn.R;
import com.lqk.videolearn.render.shape.Triangle;
import com.lqk.videolearn.render.shape.TriangleEquilateral;

public class FGLViewActivity extends AppCompatActivity {
    private final static String TAG = FGLViewActivity.class.getSimpleName();
    private FGLView glSurfaceView;

    private static final int REQ_CHOOSE=0x0101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fglview);

        glSurfaceView = (FGLView) findViewById(R.id.fgl_gl_view);
//        glSurfaceView.setShape(TriangleEquilateral.class);
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

    public void onClick(View view) {
        Intent intent=new Intent(this,ChooseActivity.class);
        startActivityForResult(intent,REQ_CHOOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            glSurfaceView.setShape((Class<? extends Shape>) data.getSerializableExtra("name"));
        }
    }
}
