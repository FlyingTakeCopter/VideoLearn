package com.lqk.videolearn.render.shape;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.View;

import com.lqk.videolearn.gles.GlUtil;
import com.lqk.videolearn.render.Shape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class Cube extends Shape {

    private int program;

    private int COORDS_PER_VERTEX = 3;

    final float cubePositions[] = {
            -1.0f,1.0f,1.0f,    //正面左上0
            -1.0f,-1.0f,1.0f,   //正面左下1
            1.0f,-1.0f,1.0f,    //正面右下2
            1.0f,1.0f,1.0f,     //正面右上3
            -1.0f,1.0f,-1.0f,    //反面左上4
            -1.0f,-1.0f,-1.0f,   //反面左下5
            1.0f,-1.0f,-1.0f,    //反面右下6
            1.0f,1.0f,-1.0f,     //反面右上7
    };

    private int vertexStribe = COORDS_PER_VERTEX * FLOAT_SIZE;

    private int vertexCount = cubePositions.length / COORDS_PER_VERTEX;

    final short index[]={
            6,7,4,6,4,5,    //后面
            6,3,7,6,2,3,    //右面
            6,5,1,6,1,2,    //下面
            0,3,2,0,2,1,    //正面
            0,1,5,0,5,4,    //左面
            0,7,3,0,4,7,    //上面
    };

    float color[] = {
            0f,1f,0f,1f,
            0f,1f,0f,1f,
            0f,1f,0f,1f,
            0f,1f,0f,1f,
            1f,0f,0f,1f,
            1f,0f,0f,1f,
            1f,0f,0f,1f,
            1f,0f,0f,1f,
    };

    private int maPosition;
    private int maColor;
    private int muMatrix;

    FloatBuffer vertexBuffer;
    FloatBuffer colorBuffer;
    ShortBuffer indexBuffer;

    private float[] projectMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] vMatrix = new float[16];

    private float[] displayMatrix = new float[16];


    public Cube(View mView) {
        super(mView);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(cubePositions.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(cubePositions);
        vertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(color.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        colorBuffer = byteBuffer.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(index.length * SHORT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        indexBuffer = byteBuffer.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glEnable(GL_DEPTH_TEST);
        program = GlUtil.createProgramRes(mView.getResources(),
                "shape/trianglecolor.vert", "shape/trianglecolor.frag");

        maPosition = glGetAttribLocation(program, "aPosition");
        muMatrix = glGetUniformLocation(program, "uMatrix");
        maColor = glGetAttribLocation(program, "aColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float)width/height;
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        Matrix.setLookAtM(viewMatrix,0,
                5f,5f,15f,
                0f,0f,0f,
                0f,1f,0f);
        Matrix.multiplyMM(displayMatrix,0, projectMatrix, 0, viewMatrix, 0);
    }

    private float[] modelMatrix = new float[16];
    private float angle = 1;

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

        float[] modelView = modelMatrix;

        Matrix.setIdentityM(modelView, 0);

        angle++;
        while (angle >= 360.0f) {
            angle -= 360.0f;
        }
        while (angle <= -360.0f) {
            angle += 360.0f;
        }
        Matrix.rotateM(modelView, 0, angle, 0.0f, 0.0f, 1.0f);

        Matrix.multiplyMM(vMatrix, 0, displayMatrix, 0, modelView, 0);

        glUseProgram(program);

        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maPosition,COORDS_PER_VERTEX,GL_FLOAT, false,vertexStribe, vertexBuffer);

        glEnableVertexAttribArray(maColor);
        glVertexAttribPointer(maColor,4,GL_FLOAT,false,4*FLOAT_SIZE, colorBuffer);

        glUniformMatrix4fv(muMatrix,1,false,vMatrix,0);

        glDrawElements(GL_TRIANGLE_STRIP, index.length, GL_UNSIGNED_SHORT, indexBuffer);

        glDisableVertexAttribArray(maPosition);
        glDisableVertexAttribArray(maColor);
        glUseProgram(0);
    }
}
