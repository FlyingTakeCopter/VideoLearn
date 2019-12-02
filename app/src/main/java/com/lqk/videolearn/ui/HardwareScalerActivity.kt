package com.lqk.videolearn.ui

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.lqk.videolearn.R

/**
 * 练习SurfaceHolder.setFixedSize（）
 * http://android-developers.blogspot.com/2013/09/using-hardware-scaler-for-performance.html
 *
 * 该功能的目的是在高分辨率的显示设备上允许游戏以720p或1080p渲染并获得良好效果
 * 当分辨率调低时使我们更容易观测效果
 * 通常情况下分辨率是固定的，获取是通过调整来匹配设备显示比例
 * 但是在这个例子中我们将各种分辨率设置为参数来匹配显示窗口
 */
class HardwareScalerActivity :Activity(), SurfaceHolder.Callback {

    // 关于应用程序生命周期和surfaceView的一些想法
    // http://source.android.com/devices/graphics/architecture.html
    //
    // This Activity uses approach #2 (Surface-driven).
    // 索引数据数组.
    private val SURFACE_SIZE_TINY = 0
    private val SURFACE_SIZE_SMALL = 1
    private val SURFACE_SIZE_MEDIUM = 2
    private val SURFACE_SIZE_FULL = 3

    private val SURFACE_DIM = intArrayOf(64, 240, 480, -1)
    private val SURFACE_LABEL = arrayOf(
            "tiny", "small", "medium", "full"
    )

    private var mSelectedSize = 0
    private var mFullViewWidth = 0
    private var mFullViewHeight = 0
    // 四行两列数组
    private val mWindowWidthHeight = Array(4){IntArray(2)}
    private var mFlatShadingChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hardware_scaler)

        mSelectedSize = SURFACE_SIZE_FULL
        mFullViewWidth = 512
        mFullViewHeight = 512


        val sv : SurfaceView = findViewById(R.id.hardwareScaler_surfaceView)
        sv.holder.addCallback(this)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    fun onRadioButtonClicked(view: View) {}
    fun onFlatShadingClicked(view: View) {}

    override fun surfaceCreated(holder: SurfaceHolder?) {
        // 获取视图的宽度
        val size = holder!!.surfaceFrame
        mFullViewWidth = size.width()
        mFullViewHeight = size.height()

        //
        val windowAspect = mFullViewHeight.toFloat() / mFullViewWidth.toFloat()
        for (i in SURFACE_DIM){

        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}