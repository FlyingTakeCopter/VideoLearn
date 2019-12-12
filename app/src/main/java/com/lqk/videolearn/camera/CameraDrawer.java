package com.lqk.videolearn.camera;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;

import com.lqk.videolearn.filter.AFilter;
import com.lqk.videolearn.filter.OesFilter;
import com.lqk.videolearn.utils.Gl2Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class CameraDrawer implements GLSurfaceView.Renderer {
    private SurfaceTexture surfaceTexture;
    AFilter mOesFilter;

    private float[] matrix=new float[16];
    // 显示 宽高
    private int width,height;
    // 数据 宽高
    private int dataWidth,dataHeight;
    // 相机编号
    private int cameraId = 1;

    public CameraDrawer(Resources res) {
        mOesFilter = new OesFilter(res);
    }

    public void setViewSize(int w, int h){
        this.width = w;
        this.height = h;
        calculateMatrix();
    }

    public void setDataSize(int w, int h){
        this.dataWidth = w;
        this.dataHeight = h;
        calculateMatrix();
    }


    /**
     * 每次修改了宽高后都要重新计算矩阵
     */
    private void calculateMatrix(){
        Gl2Utils.getShowMatrix(matrix, dataWidth, dataHeight, width, height);

        if (cameraId == 1){
            // 前置
            Gl2Utils.flip(matrix,true, false);
            Gl2Utils.rotate(matrix, 90);
        }else {
            // 后置
            Gl2Utils.rotate(matrix, 270);
        }

        mOesFilter.setMatrix(matrix);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 创建一个OES的textureid 给surfaceTexture
        int textureId = createTextureID();
        surfaceTexture = new SurfaceTexture(textureId);
        mOesFilter.create();
        mOesFilter.setTextureId(textureId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 变更视图宽高
        setViewSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (surfaceTexture != null){
            surfaceTexture.updateTexImage();
        }
        mOesFilter.draw();
    }

    private int createTextureID(){
        int[] texture = new int[1];
        glGenTextures(1, texture, 0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }
}
