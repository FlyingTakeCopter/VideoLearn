package com.lqk.videolearn.image.filter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.lqk.videolearn.gles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public abstract class AFilter implements GLSurfaceView.Renderer {
    boolean isHalf = false;
    Bitmap mBitmap;

    int program;

    int COORDS_PER_VERTEX = 2;

    int vertexCount;

    int vertexStribe = COORDS_PER_VERTEX * 4;

    float[] vertex = {
            -1f, 1f,
            -1f, -1f,
            1f, 1f,
            1f, -1f
    };

    float[] coords = {
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f
    };

    float[] projectMatrix = new float[16];
    float[] viewMatrix = new float[16];
    float[] displayMatrix = new float[16];

    FloatBuffer vertexBuffer;
    FloatBuffer coordsBuffer;

    int maPosition;
    int maCoords;
    int muMatrix;
    int muTexture;
    int muXy;
    int muIsHalf;


    int mTextureId;

    Resources mRes;

    private float uXY;

    public AFilter(Resources res) {
        mRes = res;
        vertexCount = vertex.length / COORDS_PER_VERTEX;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(coords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        coordsBuffer = byteBuffer.asFloatBuffer();
        coordsBuffer.put(coords);
        coordsBuffer.position(0);
    }

    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public abstract void onDrawSet();
    public abstract void onDrawCreatedSet(int mProgram);

    public int createTexture(){
        int[] texture = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()){
            // 生成纹理
            glGenTextures(1, texture, 0);
            // 平时使用单张纹理怎么不需要glActiveTexture?
            // sampler2D默认值为0(GLES20.glActiveTexture(GLES20.GL_TEXTURE0))，纹理也默认与GL_TEXTURE0关联
            // glActiveTexture是一个状态机，表示启用第几层纹理，之后的操作都在这层纹理上执行
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

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glDisable(GL_DEPTH_TEST);
        glClearColor(0.5f,0.5f,0.5f,1.0f);

        program = GlUtil.createProgramRes(mRes,
                "image/sglview.vert","image/sglview_filter.frag");

        maPosition = glGetAttribLocation(program, "aPosition");
        maCoords = glGetAttribLocation(program, "aCoordinate");
        muMatrix = glGetUniformLocation(program, "uMatrix");
        muTexture = glGetUniformLocation(program, "uTexture");
        muXy = glGetUniformLocation(program, "uXY");
        muIsHalf = glGetUniformLocation(program, "uIsHalf");

        mTextureId = createTexture();

        onDrawCreatedSet(program);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0,0,width,height);

        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float sBitmapWH = (float)w/h;
        float sScreenWH = (float)width/height;

        uXY=sScreenWH;

        if (width > height){
            if(sBitmapWH>sScreenWH){
                Matrix.orthoM(projectMatrix, 0, -sScreenWH*sBitmapWH,sScreenWH*sBitmapWH, -1,1, 3, 7);
            }else{
                Matrix.orthoM(projectMatrix, 0, -sScreenWH/sBitmapWH,sScreenWH/sBitmapWH, -1,1, 3, 7);
            }
        }else {
            if(sBitmapWH>sScreenWH){
                Matrix.orthoM(projectMatrix, 0, -1, 1, -1/sScreenWH*sBitmapWH, 1/sScreenWH*sBitmapWH,3, 7);
            }else{
                Matrix.orthoM(projectMatrix, 0, -1, 1, -sBitmapWH/sScreenWH, sBitmapWH/sScreenWH,3, 7);
            }
        }

        Matrix.setLookAtM(viewMatrix, 0,
                0,0,6f,
                0f,0f,0f,
                0f,1f,0f);

        Matrix.multiplyMM(displayMatrix,0, projectMatrix,0, viewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(program);

        onDrawSet();
        glUniform1f(muXy, uXY);
        glUniform1i(muIsHalf, isHalf?1:0);

        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maPosition, COORDS_PER_VERTEX, GL_FLOAT, false, vertexStribe, vertexBuffer);

        glEnableVertexAttribArray(maCoords);
        glVertexAttribPointer(maCoords, COORDS_PER_VERTEX, GL_FLOAT, false, COORDS_PER_VERTEX*4, coordsBuffer);

        glUniformMatrix4fv(muMatrix,1,false,displayMatrix,0);

        //绘制纹理方法
        //第一种: 让opengl自动处理纹理
        // 启用0层纹理
        glActiveTexture(GL_TEXTURE0);
        // 绑定 textureId
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        //第二种: 自己处理 片元着色器的uTexture
        // 将0层纹理设置到 片源着色器的 sampler2D
//        glUniform1i(muTexture, 0);
        // 在0层纹理上 绘制图片
//        createTexture();

        glDrawArrays(GL_TRIANGLE_STRIP,0,vertexCount);

        glUseProgram(0);
    }

    public void setIsHalf(boolean isHalf) {
        this.isHalf = isHalf;
    }

    public boolean isHalf() {
        return isHalf;
    }
}
