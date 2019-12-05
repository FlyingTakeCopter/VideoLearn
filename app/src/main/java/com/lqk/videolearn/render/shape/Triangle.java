package com.lqk.videolearn.render.shape;

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
 * 普通三角形 无矩阵
 */
public class Triangle extends Shape {
    private int program;
    private final int COORDS_PER_VERTEX = 2;

    private FloatBuffer vertexBuffer;

    private final float vertex[] = {
            0.0f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f
    };

    private final float color[] = {
            0.9f, 0.9f, 0.9f, 1.0f
    };

    private int vertexStride = COORDS_PER_VERTEX * FLOAT_SIZE;
    private int vertexCount = vertex.length / COORDS_PER_VERTEX;

    private int maPosition;
    private int muColor;

    public Triangle(View mView) {
        super(mView);
        // 将变量填充到本地底层 为了让opengl可以访问到(opengl是设备层的接口，访问不到虚拟机中的变量)
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 申请gl program
        program = GlUtil.createProgramRes(mView.getResources(),
                "shape/triangle.vert", "shape/flatshaded.frag");
        // 绑定参数
        maPosition = glGetAttribLocation(program, "aPosition");
        muColor = glGetUniformLocation(program, "uColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 申请
        glUseProgram(program);
        // 填参数
        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maPosition, COORDS_PER_VERTEX, GL_FLOAT, false,
                vertexStride, vertexBuffer);

        glUniform4fv(muColor, 1, color, 0);
        // 绘制
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        // 释放
        glDisableVertexAttribArray(maPosition);
        glUseProgram(0);
    }
}
