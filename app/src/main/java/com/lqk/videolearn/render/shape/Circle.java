package com.lqk.videolearn.render.shape;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.View;

import com.lqk.videolearn.gles.GlUtil;
import com.lqk.videolearn.render.Shape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

/**
 * 圆
 */
public class Circle extends Shape {

    private int program;
    private int COORDS_PER_VERTEX = 3;
    private int vertexStribe = COORDS_PER_VERTEX * FLOAT_SIZE;
    private int vertexCount = 0;

    private int maPosition;
    private int muMatrix;
    private int muColor;

    private float[] vertex;
    private float[] color = {
        0.9f,0.9f,0.9f,1.0f
    };

    private float radius=1.0f;
    private int n=360;  //切割份数

    FloatBuffer vertexBuffer;

    private float[] projectMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] displayMatrix = new float[16];

    public void setDisplayMatrix(float[] displayMatrix) {
        this.displayMatrix = displayMatrix;
    }

    public Circle(View mView) {
        this(mView, 0.0f);
    }

    public Circle(View mView,float height) {
        super(mView);
        vertex = createPositions(height);
        vertexCount = vertex.length / COORDS_PER_VERTEX;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);

    }

    private float[]  createPositions(float height){
        ArrayList<Float> data=new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(height);
        float angDegSpan=360f/n;
        for(float i=0;i<360+angDegSpan;i+=angDegSpan){
            data.add((float) (radius*Math.sin(i*Math.PI/180f)));
            data.add((float)(radius*Math.cos(i*Math.PI/180f)));
            data.add(height);
        }
        float[] f=new float[data.size()];
        for (int i=0;i<f.length;i++){
            f[i]=data.get(i);
        }
        return f;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        program = GlUtil.createProgramRes(mView.getResources(),
                "shape/trianglecolor.vert", "shape/flatshaded.frag");

        maPosition = glGetAttribLocation(program, "aPosition");
        muMatrix = glGetUniformLocation(program, "uMatrix");
        muColor = glGetUniformLocation(program, "uColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float)width/height;
        Matrix.frustumM(projectMatrix,0,-ratio,ratio,-1,1,3,7);
        Matrix.setLookAtM(viewMatrix,0,
                0,0,7f,
                0f,0f,0f,
                0f,1.0f,0f);
        Matrix.multiplyMM(displayMatrix,0,projectMatrix,0,viewMatrix,0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glUseProgram(program);

        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maPosition,COORDS_PER_VERTEX,GL_FLOAT,false,vertexStribe,vertexBuffer);

        glUniformMatrix4fv(muMatrix,1,false,displayMatrix,0);

        glUniform4fv(muColor,1, color,0);

        glDrawArrays(GL_TRIANGLE_FAN, 0, vertexCount);

        glUseProgram(0);
    }
}
