package com.lqk.videolearn.gles.program

import android.opengl.GLES20.*
import android.util.Log
import com.lqk.videolearn.gles.GLUtil
import java.nio.FloatBuffer

/**
 * 平面着色GL程序
 */
class FlatShadedProgram {
    private val TAG: String = "FlatShadedProgram"

    companion object {
        // 顶点着色器
        private const val VERTEX_SHADER: String =
                "uniform mat4 uMVPMatrix;" +  //传入顶点矩阵
                "attribute vec4 aPosition;" +  //传入顶点坐标
                "void main(){" +
                "   gl_Position = uMVPMatrix * aPosition;" +
                "}"
        // 片元着色器
        private const val FRAGMENT_SHADER: String =
                "precision mediump float;" +
                "uniform vec uColor;" +  // 传入纯色
                "void main(){" +
                "   gl_FragColor = uColor;" +
                "}"
    }

    /**
     * gl program 句柄
     */
    private var mProgramHandle = -1

    /**
     * 片源绘制的颜色
     */
    private var muColorLoc = -1

    /**
     * 顶点矩阵
     */
    private var muMVPMatrixLoc = -1

    /**
     * 顶点坐标
     */
    private var maPostionLoc = -1

    /**
     * 初始化绑定gl 绑定shader 参数
     */
    constructor(){
        mProgramHandle = GLUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (mProgramHandle == 0){
            throw RuntimeException("Unable to create program")
        }
        Log.d(TAG, "Created program $mProgramHandle")

        // 绑定shader参数
        maPostionLoc = glGetAttribLocation(mProgramHandle, "aPosition")
        GLUtil.checkLocation(maPostionLoc, "aPosition")
        muColorLoc = glGetUniformLocation(mProgramHandle, "uColor")
        GLUtil.checkLocation(muColorLoc, "uColorLoc")
        muMVPMatrixLoc = glGetUniformLocation(mProgramHandle, "uMVPMatrix")
        GLUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrixLoc")
    }

    /**
     * 释放glprogram
     */
    fun release(){
        glDeleteProgram(mProgramHandle)
        mProgramHandle = -1
    }

    /**
     * draw
     *
     * @param mvpMatrix The 4x4 projection matrix.
     * @param color A 4-element color vector.
     * @param vertexBuffer 顶点buffer大小
     * @param firstVertex 顶点数组的从哪个开始绘制.
     * @param vertexCount 顶点数组绘制几个点.
     * @param coordsPerVertex 顶点数组中每几个是一个坐标.
     * @param vertexStride 步长,每一个坐标点的byte长度
     */
    fun draw(mvpMatrix: FloatArray, color: FloatArray, vertexBuffer: FloatBuffer,
             firstVertex: Int, vertexCount: Int, coordsPerVertex: Int, vertexStride: Int){
        // 选择gl program
        glUseProgram(mProgramHandle)
        // muMVPMatrixLoc赋值
        glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0)
        // muColorLoc赋值
        glUniform4fv(muColorLoc, 1, color, 0)
        // maPostionLoc赋值
        glEnableVertexAttribArray(maPostionLoc)
        glVertexAttribPointer(maPostionLoc, coordsPerVertex,
                GL_FLOAT, false, vertexStride, vertexBuffer)
        // 绘制
        glDrawArrays(GL_TRIANGLE_STRIP, firstVertex, vertexCount)
        // 绘制完成
        glDisableVertexAttribArray(maPostionLoc)
        glUseProgram(0)
    }

}