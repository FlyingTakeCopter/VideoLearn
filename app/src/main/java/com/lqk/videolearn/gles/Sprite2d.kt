package com.lqk.videolearn.gles

import android.opengl.Matrix
import com.lqk.videolearn.gles.program.FlatShadedProgram
import com.lqk.videolearn.gles.program.Texture2dProgram

/**
 * 基础2d对象绘制参数 包括 坐标 缩放 旋转 颜色
 */
class Sprite2d (drawable2d: Drawable2d){
    // 2d 预制图形
    var mDrawable : Drawable2d? = null
    // 颜色
    var mColor : FloatArray? = null
    // 纹理ID
    var mTextureId : Int? = -1
    // 角度
    var mAngle : Float? = 0.0f
        set(value) {
            var angle = value
            // Normalize.  We're not expecting it to be way off, so just iterate.
            while (angle!! >= 360.0f) {
                angle -= 360.0f
            }
            while (angle <= -360.0f) {
                angle += 360.0f
            }
            mMatrixReady = false
            field = value
        }
    // 缩放
    var mScaleX : Float? = 0.0f
        set(value) {
            mMatrixReady = false
            field = value
        }
    var mScaleY : Float? = 0.0f
        set(value) {
            mMatrixReady = false
            field = value
        }
    // 坐标
    var mPosX : Float? = 0.0f
        set(value) {
            mMatrixReady = false
            field = value
        }
    var mPosY : Float? = 0.0f
        set(value) {
            mMatrixReady = false
            field = value
        }
    // 计算矩阵
    var mModelViewMatrix : FloatArray? = null
        get(){
            // 当参数发生变化时 重新计算当前矩阵
            if (mMatrixReady == false){
                recomputeMatrix()
            }
            return field
        }
    // 矩阵是否正确(参数是否改变)
    var mMatrixReady : Boolean? = false
    // 最终绘制的矩阵(通过矩阵乘法得到)
    var mScratchMatrix : FloatArray = FloatArray(16)

    init {
        mDrawable = drawable2d
        mColor = FloatArray(4)
        mColor!![3] = 1.0f
    }

    /**
     * 根据 角度 缩放 坐标 重新计算矩阵
     */
    private fun recomputeMatrix(){
        val modelView = mModelViewMatrix
        // 正交投影
        Matrix.setIdentityM(modelView, 0)
        // translate
        Matrix.translateM(modelView, 0, mPosX!!, mPosY!!, 0.0f)
        // angle
        if (mAngle != 0.0f){
            Matrix.rotateM(modelView, 0, mAngle!!, 0.0f, 0.0f, 0.0f)
        }
        // scale
        Matrix.scaleM(modelView, 0, mScaleX!!, mScaleY!!, 1.0f)
        mMatrixReady = true
    }

    // 平面颜色绘制
    public fun draw(program: FlatShadedProgram, projectionMatrix: FloatArray){
        // 计算最终绘制的矩阵
        Matrix.multiplyMM(mScratchMatrix, 0, projectionMatrix, 0, mModelViewMatrix, 0)
        // 绘制
        program.draw(
                mScratchMatrix,
                mColor!!,
                mDrawable!!.mVertexArray!!,
                0,
                mDrawable!!.mVertexCount!!,
                mDrawable!!.mCoordsPerVertex!!,
                mDrawable!!.mVertexStride!!)
    }
    // 纹理绘制
    public fun draw(program: Texture2dProgram, projectionMatrix: FloatArray){
        // 计算最终绘制的矩阵
        Matrix.multiplyMM(mScratchMatrix, 0, projectionMatrix, 0, mModelViewMatrix, 0)
        // 绘制
        program.draw(
                mScratchMatrix,
                mDrawable!!.mVertexArray!!,
                0,
                mDrawable!!.mVertexCount!!,
                mDrawable!!.mCoordsPerVertex!!,
                mDrawable!!.mVertexStride!!,
                GLUtil.IDENTITY_MATRIX,
                mDrawable!!.mTexCoordArray!!,
                mTextureId!!,
                mDrawable!!.mTexCoordStride!!)
    }
}