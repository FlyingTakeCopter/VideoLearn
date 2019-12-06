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

public class Cone extends Shape {

    private Circle circle;
    private int program;

    private final int COORDS_PER_VERTEX = 3;

    private int vertexStribe = COORDS_PER_VERTEX * FLOAT_SIZE;
    private int vertexCount;

    private float[] vertex;
    private float radius=1.0f;
    private int n=360;  //切割份数

    FloatBuffer vertexBuffer;
    FloatBuffer endBuffer;

    private int maPosition;
    private int muMatrix;

    // 投影
    private float[] projectMatrix = new float[16];
    // 相机
    private float[] viewMatrix = new float[16];
    // 模型
    private float[] modelMatrix = new float[16];
    // 最终显示
    private float[] displayMatrix = new float[16];

    private float[] tempMatrix = new float[16];

    public Cone(View mView) {
        super(mView);
        circle = new Circle(mView);

        float[] vertex = createPositions();
        vertexCount = vertex.length / COORDS_PER_VERTEX;

        // 锥体
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);
    }

    private float[]  createPositions(){
        float z = 0.5f;
        ArrayList<Float> data=new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(2.0f);
        float angDegSpan=360f/n;
        for(float i=0;i<360+angDegSpan;i+=angDegSpan){
            data.add((float) (radius*Math.sin(i*Math.PI/180f)));
            data.add((float)(radius*Math.cos(i*Math.PI/180f)));
            data.add(0.0f);
        }
        float[] f=new float[data.size()];
        for (int i=0;i<f.length;i++){
            f[i]=data.get(i);
        }
        return f;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 开启深度测试 3d绘制
        glEnable(GL_DEPTH_TEST);

        program = GlUtil.createProgramRes(mView.getResources(),
                "shape/cone.vert", "shape/trianglecolor.frag");
        maPosition = glGetAttribLocation(program, "aPosition");
        muMatrix = glGetUniformLocation(program, "uMatrix");

        circle.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float)width / height;
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        Matrix.setLookAtM(viewMatrix, 0,
                1.0f,-10.0f,-4.0f,
                0f,0f,0f,
                0f,1f,0f);
        Matrix.multiplyMM(tempMatrix,0,projectMatrix,0,viewMatrix,0);
    }

    float angle = 0f;
    @Override
    public void onDrawFrame(GL10 gl) {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

        float[] modelView = modelMatrix;

        Matrix.setIdentityM(modelView, 0);

        angle++;
        while (angle >= 360.0f) {
            angle -= 360.0f;
        }
        while (angle <= -360.0f) {
            angle += 360.0f;
        }
        Matrix.rotateM(modelView, 0, angle, 1.0f, 0.5f, 0.0f);

        Matrix.multiplyMM(displayMatrix, 0, tempMatrix, 0, modelView, 0);

        glUseProgram(program);

        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maPosition,COORDS_PER_VERTEX,GL_FLOAT,false,vertexStribe,vertexBuffer);

        glUniformMatrix4fv(muMatrix, 1, false, displayMatrix, 0);

        glDrawArrays(GL_TRIANGLE_FAN, 0, vertexCount);

        glUseProgram(0);

        // 画个底儿
        circle.setDisplayMatrix(displayMatrix);
        circle.onDrawFrame(gl);
    }
}
