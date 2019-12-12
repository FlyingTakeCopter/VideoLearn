package com.lqk.videolearn.filter;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.util.Arrays;

import static android.opengl.GLES20.*;

/**
 * OES 是专门用来给 SurfaceTexture来显示数据的 详细请看SurfaceTexture的注释
 */
public class OesFilter extends AFilter {
    private int muCoordMatrix;
    private float[] coordsMatrix = Arrays.copyOf(OM, 16);
    public OesFilter(Resources mResources) {
        super(mResources);
    }

    @Override
    protected void onCreate() {
        createProgram("shader/oes/oes_base.vert", "shader/oes/oes_base.frag");
        muCoordMatrix = glGetUniformLocation(program, "uCoordMatrix");
    }

    @Override
    protected void onSizeChange(int width, int height) {

    }

    @Override
    protected void onSetExpandData() {
        super.onSetExpandData();
        glUniformMatrix4fv(muCoordMatrix,1,false,coordsMatrix, 0);
    }

    @Override
    protected void onBindTexture() {
        // 激活 "GL_TEXTURE0 + getTextureType()" 层纹理 接下来的所有操作都在这个纹理层上发生
        glActiveTexture(GL_TEXTURE0 + getTextureType());
        // 将纹理ID为textureId的纹理 以OES的方式绑定到这个纹理层上
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTextureId());
        // 将 getTextureType() 这个层的纹理 设置到片元着色器上
        glUniform1i(muTexture, getTextureType());
    }
}
