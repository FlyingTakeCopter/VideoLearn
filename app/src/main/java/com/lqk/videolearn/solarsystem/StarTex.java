package com.lqk.videolearn.solarsystem;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.lqk.videolearn.gles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static android.opengl.GLES20.*;

public class StarTex {
    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    Bitmap mBitmap;

    int mProgram;
    int muMVPMatrixHandle;
    int maPositionHandle;
    int maTexCoorHandle;

    FloatBuffer mVertexBuffer;
    FloatBuffer mTexCoorBuffer;

    int vCount = 0;
    final float UNIT_SIZE = 1f;
    final float angleSpan = 10f;
    float R = 5f;

    public float roateX;
    public float roateY;

    private float[] displayMatrix;

    Resources mRes;

    int mTextureId;
    public StarTex(Resources res) {
        // TODO Auto-generated constructor stub
        this.mRes = res;
        initVertex();
//        initShader(mProgram);
    }

    private void initVertex() {
        // TODO Auto-generated method stub
//        R = r;
        ArrayList<Float> alVertix = new ArrayList<Float>();
        for (float vAngle = 90; vAngle > -90; vAngle -= angleSpan) {
            for (float hAngle = 360; hAngle > 0; hAngle -= angleSpan) {
                float x1 = getCoor(0, vAngle, hAngle);
                float y1 = getCoor(1, vAngle, hAngle);
                float z1 = getCoor(2, vAngle, hAngle);

                float x2 = getCoor(0, vAngle - angleSpan, hAngle);
                float y2 = getCoor(1, vAngle - angleSpan, hAngle);
                float z2 = getCoor(2, vAngle - angleSpan, hAngle);

                float x3 = getCoor(0, vAngle - angleSpan, hAngle - angleSpan);
                float y3 = getCoor(1, vAngle - angleSpan, hAngle - angleSpan);
                float z3 = getCoor(2, vAngle - angleSpan, hAngle - angleSpan);

                float x4 = getCoor(0, vAngle, hAngle - angleSpan);
                float y4 = getCoor(1, vAngle, hAngle - angleSpan);
                float z4 = getCoor(2, vAngle, hAngle - angleSpan);

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);
                alVertix.add(x4);
                alVertix.add(y4);
                alVertix.add(z4);
                // 构建第二三角形
                alVertix.add(x4);
                alVertix.add(y4);
                alVertix.add(z4);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
            }
        }

        vCount = alVertix.size() / 3;

        float vertices[] = new float[vCount * 3];
        for (int i = 0; i < alVertix.size(); i++) {
            vertices[i] = alVertix.get(i);
        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        float[] texCoor = generateTexCoor(// 获取切分整图的纹理数组
                (int) (360 / angleSpan), // 纹理图切分的列数
                (int) (180 / angleSpan) // 纹理图切分的行数
        );
        ByteBuffer llbb = ByteBuffer.allocateDirect(texCoor.length * 4);
        llbb.order(ByteOrder.nativeOrder());// 设置字节顺序
        mTexCoorBuffer = llbb.asFloatBuffer();
        mTexCoorBuffer.put(texCoor);
        mTexCoorBuffer.position(0);
    }

    public void create() {
        // TODO Auto-generated method stub
        mProgram = GlUtil.createProgramRes(mRes,
                "shape/ball_tex.vert", "shape/ball_tex.frag");
//        this.mProgram = mProgram;
        muMVPMatrixHandle = glGetUniformLocation(mProgram, "uMVPMatrix");
        maPositionHandle = glGetAttribLocation(mProgram, "aPosition");
        maTexCoorHandle = glGetAttribLocation(mProgram, "aTexCoor");

        mTextureId = createTexture();
    }

    public void setDisplayMatrix(float[] displayMatrix) {
        this.displayMatrix = Arrays.copyOf(displayMatrix, 16);
    }

    public void drawSelf() {
        glUseProgram(mProgram);
        glUniformMatrix4fv(muMVPMatrixHandle, 1, false, displayMatrix, 0);
        glVertexAttribPointer(maPositionHandle, 3, GL_FLOAT, false, 3 * 4, mVertexBuffer);
        glVertexAttribPointer(maTexCoorHandle, 2, GL_FLOAT, false, 2 * 4, mTexCoorBuffer);
        glEnableVertexAttribArray(maPositionHandle);
        glEnableVertexAttribArray(maTexCoorHandle);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        glDrawArrays(GL_TRIANGLES, 0, vCount);
    }

    private float getCoor(int which, float vAngle, float hAngle) {
        switch (which) {
            case 0:// x
                return (float) (R * UNIT_SIZE * Math.cos(Math.toRadians(vAngle)) * Math.cos(Math.toRadians(hAngle)));
            case 1:// y
                return (float) (R * UNIT_SIZE * Math.sin(Math.toRadians(vAngle)));
            case 2:// z
                return (float) (R * UNIT_SIZE * Math.cos(Math.toRadians(vAngle)) * Math.sin(Math.toRadians(hAngle)));
        }
        return 0;
    }

    // 自动切分纹理产生纹理数组的方法
    public float[] generateTexCoor(int bw, int bh) {
        float[] result = new float[bw * bh * 6 * 2];
        float sizew = 1.0f / bw;// 列数
        float sizeh = 1.0f / bh;// 行数
        int c = 0;
        for (int i = 0; i < bh; i++) {
            for (int j = 0; j < bw; j++) {
                // 每行列一个矩形，由两个三角形构成，共六个点，12个纹理坐标
                float s = j * sizew;
                float t = i * sizeh;
                result[c++] = s;
                result[c++] = t;
                result[c++] = s;
                result[c++] = t + sizeh;
                result[c++] = s + sizew;
                result[c++] = t;
                result[c++] = s + sizew;
                result[c++] = t;
                result[c++] = s;
                result[c++] = t + sizeh;
                result[c++] = s + sizew;
                result[c++] = t + sizeh;
            }
        }
        return result;
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
