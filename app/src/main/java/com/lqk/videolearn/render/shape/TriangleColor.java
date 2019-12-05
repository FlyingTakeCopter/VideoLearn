package com.lqk.videolearn.render.shape;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.View;

import com.lqk.videolearn.gles.GlUtil;
import com.lqk.videolearn.render.Shape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

/**
 * 彩色三角形
 */
public class TriangleColor extends Shape {

    private int program;

    private final int COORDS_PER_VERTEX = 2;

    private float[] vertex = {
            0f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f
    };

    private float[] color = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    private int vertexStride = COORDS_PER_VERTEX * FLOAT_SIZE;

    private int vertexCount = vertex.length / COORDS_PER_VERTEX;

    private float[] projectMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] displayMatrix = new float[16];

    private int maPosition;
    private int muMatrix;
    private int maColor;

    FloatBuffer vertexBuffer;
    FloatBuffer colorBuffer;

    public TriangleColor(View mView) {
        super(mView);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(color.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        colorBuffer = byteBuffer.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        program = GlUtil.createProgramRes(mView.getResources(),
                "shape/trianglecolor.vert", "shape/trianglecolor.frag");

        maPosition = glGetAttribLocation(program, "aPosition");
        muMatrix = glGetUniformLocation(program, "uMatrix");
        maColor = glGetAttribLocation(program, "aColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float)width / height;
        // 投影矩阵
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        // 相机矩阵
        Matrix.setLookAtM(viewMatrix, 0,
                0,0,5f,
                0,0,0,
                0,1f,0);
        // 显示矩阵
        Matrix.multiplyMM(displayMatrix, 0, projectMatrix, 0, viewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glUseProgram(program);

        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maPosition, COORDS_PER_VERTEX, GL_FLOAT, false, vertexStride, vertexBuffer);

        glEnableVertexAttribArray(maColor);
        glVertexAttribPointer(maColor, 4, GL_FLOAT, false, 4 * FLOAT_SIZE, colorBuffer);

        glUniformMatrix4fv(muMatrix, 1, false, displayMatrix, 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexCount);

        glUseProgram(0);
    }
}
