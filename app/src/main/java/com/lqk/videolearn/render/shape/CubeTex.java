package com.lqk.videolearn.render.shape;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.View;

import com.lqk.videolearn.gles.GlUtil;
import com.lqk.videolearn.render.Shape;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

//TODO 未完成 没想明白呢
public class CubeTex extends Shape {
    private Bitmap mBitmap;
    private int mTextureId;

    int program;
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

//    final short index[]={
//            6,7,4,6,4,5,    //后面
//            6,3,7,6,2,3,    //右面
//            6,5,1,6,1,2,    //下面
//            0,3,2,0,2,1,    //正面
//            0,1,5,0,5,4,    //左面
//            0,7,3,0,4,7,    //上面
//    };

    final short index1[]={
            6,7,4,6,4,5,    //后面
    };
    //0,0 0,1 1,1 1,0

    final short index2[]={
            6,3,7,6,2,3,    //右面
    };
    //

    final short index3[]={
            6,5,1,6,1,2,    //下面
    };

    final short index4[]={
            0,3,2,0,2,1,    //正面
    };
    // 0,0 0,1 1,1 1,0

    final short index5[]={
            0,1,5,0,5,4,    //左面
    };

    final short index6[]={
            0,7,3,0,4,7,    //上面
    };

//    float color[] = {
//            0f,1f,0f,1f,
//            0f,1f,0f,1f,
//            0f,1f,0f,1f,
//            0f,1f,0f,1f,
//            1f,0f,0f,1f,
//            1f,0f,0f,1f,
//            1f,0f,0f,1f,
//            1f,0f,0f,1f,
//    };

    private int maPosition;
    private int muMatrix;
    private int maCoords;

    FloatBuffer vertexBuffer;
//    FloatBuffer colorBuffer;
    ShortBuffer indexBuffer1;
    ShortBuffer indexBuffer2;
    ShortBuffer indexBuffer3;
    ShortBuffer indexBuffer4;
    ShortBuffer indexBuffer5;
    ShortBuffer indexBuffer6;


    private float[] projectMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] vMatrix = new float[16];

    private float[] displayMatrix = new float[16];

    private void initIndex(short[] index, ShortBuffer buffer){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(index.length * SHORT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        buffer = byteBuffer.asShortBuffer();
        buffer.put(index);
        buffer.position(0);
    }

    public CubeTex(View mView) {
        super(mView);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(cubePositions.length * FLOAT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(cubePositions);
        vertexBuffer.position(0);

        initIndex(index1, indexBuffer1);
        initIndex(index2, indexBuffer2);
        initIndex(index3, indexBuffer3);
        initIndex(index4, indexBuffer4);
        initIndex(index5, indexBuffer5);
        initIndex(index6, indexBuffer6);

        try {
            mBitmap = BitmapFactory.decodeStream(mView.getResources().getAssets().open("image/beauty.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glEnable(GL_DEPTH_TEST);
        program = GlUtil.createProgramRes(mView.getResources(),
                "shape/ball_tex.vert","shape/ball_tex.frag");

        maPosition = glGetAttribLocation(program, "aPosition");
        muMatrix = glGetUniformLocation(program, "uMVPMatrix");
        maCoords = glGetAttribLocation(program, "aTexCoor");

        mTextureId = createTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float)width / height;
        Matrix.frustumM(projectMatrix,0,-ratio,ratio, -1, 1, 3, 10);
        Matrix.setLookAtM(viewMatrix, 0,
                0,0,7f,
                0,0,0,
                0,1,0);
        Matrix.multiplyMM(displayMatrix,0,projectMatrix,0,viewMatrix,0);
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
        glEnableVertexAttribArray(maCoords);
        glVertexAttribPointer(maPosition,COORDS_PER_VERTEX,GL_FLOAT,false,vertexStribe,vertexBuffer);


//        glVertexAttribPointer(maCoords,2,GL_FLOAT,false,2*SHORT_SIZE, indexBuffer);

        glUniformMatrix4fv(muMatrix, 1, false, vMatrix, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);

        glDrawElements(GL_TRIANGLE_STRIP, index.length, GL_UNSIGNED_SHORT, indexBuffer);

        glDisableVertexAttribArray(maPosition);
        glDisableVertexAttribArray(maCoords);
        glUseProgram(0);
    }

    private int createTexture(){
        int[] texture = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()){
            // 生成纹理
            glGenTextures(1, texture, 0);
            // 平时使用单张纹理怎么不需要glActiveTexture?
            // sampler2D默认值为0(GLES20.glActiveTexture(GLES20.GL_TEXTURE0))，纹理也默认与GL_TEXTURE0关联
            // glActiveTexture是一个状态机，表示启用第几层纹理，之后的操作都在这层纹理上执行
            glActiveTexture(GL_TEXTURE0);
            //生成纹理 表示这个纹理(texture[0])是一个2D纹理
            glBindTexture(GL_TEXTURE_2D, texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            //绘制纹理
            GLUtils.texImage2D(GL_TEXTURE_2D,0,mBitmap,0);
            return texture[0];
        }
        return 0;
    }
}
