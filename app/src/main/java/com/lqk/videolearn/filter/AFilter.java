package com.lqk.videolearn.filter;

import android.content.res.Resources;
import android.opengl.Matrix;

import com.lqk.videolearn.gles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import static android.opengl.GLES20.*;

public abstract class AFilter {

    protected int program;

    protected int maPosition;

    protected int maCoords;

    protected int muMatrix;

    protected int muTexture;
    // 顶点坐标
    float[] vertex = {
        -1f, 1f,
        -1f, -1f,
        1f, 1f,
        1f, -1f
    };
    // 纹理坐标
    float[] coord = {
        0f, 0f,
        0f, 1f,
        1f, 0f,
        1f, 1f
    };

    protected int COORDS_PER_VERTEX = 2;

    protected int vertexStribe = COORDS_PER_VERTEX * 4;

    protected int vertexCount = vertex.length / COORDS_PER_VERTEX;

    protected FloatBuffer vertexBuf;
    protected FloatBuffer coordsBuf;

    protected int mTextureType=0;      //默认使用Texture2D0

    protected int mTextureId;

    protected Resources mResources;

    // 单位矩阵
    public static final float[] OM= new float[]{
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1
    };

    private float[] matrix = Arrays.copyOf(OM, 16);

    public AFilter(Resources mResources) {
        this.mResources = mResources;
        Matrix.setIdentityM(matrix, 0);
        initBuffer();
    }

    private void initBuffer(){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuf = byteBuffer.asFloatBuffer();
        vertexBuf.put(vertex);
        vertexBuf.position(0);

        byteBuffer = ByteBuffer.allocateDirect(coord.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        coordsBuf = byteBuffer.asFloatBuffer();
        coordsBuf.put(coord);
        coordsBuf.position(0);
    }

    public void create(){
        onCreate();
    }

    public void draw(){
        // 清理
        onClear();
        // 绑定program
        onUsePorgram();
        // 设置参数
        onSetExpandData();
        // 绑定纹理
        onBindTexture();
        // 绘制
        onDraw();
    }

    protected abstract void onCreate();
    protected abstract void onSizeChange(int width, int height);

    protected final void createProgram(String vertex, String frag){
        program = GlUtil.createProgramRes(mResources, vertex, frag);
        maPosition = glGetAttribLocation(program, "aPosition");
        maCoords = glGetAttribLocation(program, "aCoords");
        muMatrix = glGetUniformLocation(program, "uMatrix");
        muTexture = glGetUniformLocation(program, "uTexture");
    }

    protected void onClear(){
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
    }

    protected void onUsePorgram(){
        glUseProgram(program);
    }

    protected void onSetExpandData(){
        glUniformMatrix4fv(muMatrix,1,false,matrix, 0);
    }

    protected void onBindTexture(){
        glActiveTexture(GL_TEXTURE0 + mTextureType);
        glBindTexture(GL_TEXTURE_2D, getTextureId());
        glUniform1i(muTexture,mTextureType);
    }

    protected void onDraw(){
        glEnableVertexAttribArray(maPosition);
        glEnableVertexAttribArray(maCoords);
        glVertexAttribPointer(maPosition, COORDS_PER_VERTEX, GL_FLOAT, false, vertexStribe, vertexBuf);
        glVertexAttribPointer(maCoords, COORDS_PER_VERTEX, GL_FLOAT, false, vertexStribe, coordsBuf);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexCount);
        glDisableVertexAttribArray(maPosition);
        glDisableVertexAttribArray(maCoords);
    }

    public int getTextureType() {
        return mTextureType;
    }

    public void setTextureType(int textureType) {
        this.mTextureType = textureType;
    }

    public int getTextureId() {
        return mTextureId;
    }

    public void setTextureId(int mTextureId) {
        this.mTextureId = mTextureId;
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }
}
