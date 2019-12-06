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

public class Ball extends Shape {

    private float step=5f;

    int program;

    private final int COORDS_PER_VERTEX = 3;

    int vertexStribe = COORDS_PER_VERTEX * FLOAT_SIZE;

    int vertexCount;

    int maPosition;
    int muMatrix;

    // 投影
    private float[] projectMatrix = new float[16];
    // 相机
    private float[] viewMatrix = new float[16];
    // 模型
    private float[] modelMatrix = new float[16];
    // 最终显示
    private float[] displayMatrix = new float[16];

    private float[] tempMatrix = new float[16];

    FloatBuffer vertexBuffer;

    public Ball(View mView) {
        super(mView);
        float[] vertex = createBallPos();
        vertexCount = vertex.length / COORDS_PER_VERTEX;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);
    }

    private float[] createBallPos(){
        //球以(0,0,0)为中心，以R为半径，则球上任意一点的坐标为
        // ( R * cos(a) * sin(b),y0 = R * sin(a),R * cos(a) * cos(b))
        // 其中，a为圆心到点的线段与xz平面的夹角，b为圆心到点的线段在xz平面的投影与z轴的夹角
        ArrayList<Float> data=new ArrayList<>();
        float r1,r2;
        float h1,h2;
        float sin,cos;
        for(float i=-90;i<90+step;i+=step){
            r1 = (float)Math.cos(i * Math.PI / 180.0);
            r2 = (float)Math.cos((i + step) * Math.PI / 180.0);
            h1 = (float)Math.sin(i * Math.PI / 180.0);
            h2 = (float)Math.sin((i + step) * Math.PI / 180.0);
            // 固定纬度, 360 度旋转遍历一条纬线
            float step2=step*2;
            for (float j = 0.0f; j <360.0f+step; j +=step2 ) {
                cos = (float) Math.cos(j * Math.PI / 180.0);
                sin = -(float) Math.sin(j * Math.PI / 180.0);

                data.add(r2 * cos);
                data.add(h2);
                data.add(r2 * sin);
                data.add(r1 * cos);
                data.add(h1);
                data.add(r1 * sin);
            }
        }
        float[] f=new float[data.size()];
        for(int i=0;i<f.length;i++){
            f[i]=data.get(i);
        }
        return f;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glEnable(GL_DEPTH_TEST);
        program = GlUtil.createProgramRes(mView.getResources(),
                "shape/ball.vert", "shape/trianglecolor.frag");

        maPosition = glGetAttribLocation(program, "aPosition");
        muMatrix = glGetUniformLocation(program, "uMatrix");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float)width / height;
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio,-1, 1, 3, 20);
        Matrix.setLookAtM(viewMatrix, 0,
                1.0f, -10.0f, -4.0f,
                0f,0f,0f,
                0f,1.0f,0f);
        Matrix.multiplyMM(tempMatrix,0,projectMatrix,0,viewMatrix,0);
    }

    float angle = 0f;

    @Override
    public void onDrawFrame(GL10 gl) {
        float[] modelView = modelMatrix;

        Matrix.setIdentityM(modelView, 0);

        angle+=5;
        while (angle >= 360.0f) {
            angle -= 360.0f;
        }
        while (angle <= -360.0f) {
            angle += 360.0f;
        }
        Matrix.rotateM(modelView, 0, angle, 1.0f, 0.0f, 1.0f);

        Matrix.multiplyMM(displayMatrix, 0, tempMatrix, 0, modelView, 0);

        glUseProgram(program);

        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maPosition, COORDS_PER_VERTEX, GL_FLOAT, false, vertexStribe, vertexBuffer);

        glUniformMatrix4fv(muMatrix, 1, false, displayMatrix, 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexCount);

        glUseProgram(0);
    }
}
