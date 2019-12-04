package com.lqk.videolearn.render;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.lqk.videolearn.R;
import com.lqk.videolearn.gles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class FGLViewActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private final static String TAG = FGLViewActivity.class.getSimpleName();


    private FloatBuffer vertexBuffer;
    private final String vertexShaderCode =
            "attribute vec4 aPosition;" +
            "void main() {" +
            "   gl_Position = aPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 uColor;" +
            "void main() {" +
            "   gl_FragColor = uColor;" +
            "}";

    private int program;
    private int maPositation;
    private int muColor;


    private final int COORDS_PER_VERTEX = 2;
    private final float triangleCoords[] = {
        0.5f, 0.5f,
        -0.5f, -0.5f,
        0.5f, -0.5f
    };
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4;

    private float color[] = {
      1.0f, 1.0f, 1.0f, 1.0f
    };

    private float[] mMatrix = new float[16];

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fglview);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.fgl_gl_view);

        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // 将变量填充到本地底层 为了让opengl可以访问到(opengl是设备层的接口，访问不到虚拟机中的变量)
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

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

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        program = GlUtil.createProgram(vertexShaderCode, fragmentShaderCode);
        // gl参数绑定
        maPositation = glGetAttribLocation(program, "aPosition");
        GlUtil.checkLocation(maPositation, "aPositation");
        muColor = glGetUniformLocation(program, "uColor");
        GlUtil.checkLocation(muColor, "uColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清除屏幕
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        // 将program加入到opengl环境
        glUseProgram(program);

        // 参数设置
        glEnableVertexAttribArray(maPositation);
        glVertexAttribPointer(maPositation, COORDS_PER_VERTEX, GL_FLOAT, false,
                vertexStride, vertexBuffer);

        glUniform4fv(muColor, 1, color, 0);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glDisableVertexAttribArray(maPositation);
        glUseProgram(0);
    }

}
