package com.lqk.videolearn.gles.program

import android.opengl.GLES20.*
import android.util.Log
import com.lqk.videolearn.gles.GLUtil
import java.nio.FloatBuffer

/**
 * 纹理着色GL程序
 */
class Texture2dProgram {
    private val TAG: String = "Texture2dProgram"

    companion object {
        // 顶点着色器
        private const val VERTEX_SHADER: String =
                "uniform mat4 uMVPMatrix;\n" +  // 顶点矩阵
                "uniform mat4 uTexMatrix;\n" +  // 纹理矩阵
                "attribute vec4 aPosition;\n" +  // 传入的顶点
                "attribute vec4 aTextureCoord;\n" +  // 传入的顶点纹理位置
                "varying vec2 vTextureCoord;\n" +  // 用于传递给片着色器的纹理位置变量
                "void main() {\n" +
                "    gl_Position = uMVPMatrix * aPosition;\n" +
                "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                "}\n"
        // 片元着色器
        private const val FRAGMENT_SHADER_2D: String =
                "precision mediump float;\n" +
                "varying vec2 vTextureCoord;\n" +  // 纹理坐标，从顶点着色器传入
                "uniform sampler2D sTexture;\n" +  // 纹理采样器，代表一副纹理
                "void main() {\n" +
                // 片元的颜色不再是简单的单色，而是通过texture2D进行纹理采样，得到的颜色。
                //
                // 纹理采样器 sampler2D sTexture，变量名sTexture是我们自己取的，
                // 我们不用为sTexture赋值，着色器会自动处理sTexture，
                // 指向我们待会儿要传过去的纹理图片，纹理坐标由顶点着色器传过来。
                "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                "}\n"
    }

    enum class ProgramType {
        TEXTURE_2D, TEXTURE_EXT, TEXTURE_EXT_BW, TEXTURE_EXT_FILT
    }

    private var mProgramType: ProgramType

    private var mProgramHandle = -1
    private var muMVPMatrixLoc = -1
    private var muTexMatrixLoc = -1
    private var maPositionLoc = -1
    private var maTextureCoordLoc = -1

    private var mTextureTarget = -1

    constructor(programType: ProgramType){
        mProgramType = programType
        // 创建gl program
        when(programType){
            ProgramType.TEXTURE_2D ->{
                mTextureTarget = GL_TEXTURE_2D
                mProgramHandle = GLUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D)
            }
            else -> {

            }
        }

        if (mProgramHandle == 0){
            throw RuntimeException("Unable to create program")
        }

        Log.d(TAG, "Created program $mProgramHandle ($programType)")
        // 参数初始化
        maPositionLoc = glGetAttribLocation(mProgramHandle, "aPosition")
        maTextureCoordLoc = glGetAttribLocation(mProgramHandle, "aTextureCoord")
        muMVPMatrixLoc = glGetUniformLocation(mProgramHandle, "uMVPMatrix")
        muTexMatrixLoc = glGetUniformLocation(mProgramHandle, "uTexMatrix")

    }

    /**
     * 释放glprogram
     */
    fun release(){
        glDeleteProgram(mProgramHandle)
        mProgramHandle = -1
    }

    /**
     * 纹理绘制
     *
     * @param mvpMatrix 4*4 顶点矩阵
     * @param vertexBuffer 顶点坐标buffer
     * @param firstVertex vertexBuffer 中第一个绘制点的INDEX
     * @param vertexCount vertexBuffer绘制点的数量
     * @param coordsPerVertex 每一个坐标点的数量
     * @param vertexStride 步长,每一个vertex顶点的byte长度(often coordsPerVertex * sizeof(float)).
     * @param texMatrix 4*4 纹理矩阵.  (Primarily intended for use with SurfaceTexture.)
     * @param texBuffer 纹理坐标buffer.
     * @param texStride 步长,每一个纹理顶点的byte长度
     */
    fun draw(mvpMatrix: FloatArray, vertexBuffer: FloatBuffer, firstVertex: Int,
             vertexCount: Int, coordsPreVertex: Int, vertexStride: Int,
             texMatrix: FloatArray, texBuffer: FloatBuffer, textureId: Int, texStride: Int){
        // 选择gl program
        glUseProgram(mProgramHandle)
        // 设置纹理ID
        glBindTexture(mTextureTarget, textureId)
        // 接下来的操作都是对上一步绑定的texture做的
        // 设置顶点矩阵
        glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0)
        // 设置纹理矩阵
        glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0)
        // 设置顶点坐标
        glEnableVertexAttribArray(maPositionLoc)
        glVertexAttribPointer(maPositionLoc, coordsPreVertex,
                GL_FLOAT, false, vertexStride, vertexBuffer)
        // 设置纹理坐标
        glEnableVertexAttribArray(maTextureCoordLoc)
        glVertexAttribPointer(maTextureCoordLoc, 2,
                GL_FLOAT, false, texStride, texBuffer)
        // draw
        glDrawArrays(GL_TRIANGLE_STRIP, firstVertex, vertexCount)
        // 释放
        glDisableVertexAttribArray(maPositionLoc)
        glDisableVertexAttribArray(maTextureCoordLoc)
        // 释放当前绑定的texture
        glBindTexture(mTextureTarget, 0)
        // 取消gl program
        glUseProgram(0)
    }

}