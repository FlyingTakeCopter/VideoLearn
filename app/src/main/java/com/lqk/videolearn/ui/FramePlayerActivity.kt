package com.lqk.videolearn.ui

import android.app.Activity
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.lqk.videolearn.R
import com.lqk.videolearn.grafika.FrameControlCallback
import com.lqk.videolearn.grafika.FramePlayer
import java.io.File
import java.io.IOException

class FramePlayerActivity : Activity(), View.OnClickListener {

    private var mTextureView: TextureView? = null

    private var mPlayTask: FramePlayer.PlayTask? = null

    private var mFps: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_frame)
        mTextureView = findViewById(R.id.frame_player)
        findViewById<Button>(R.id.play).setOnClickListener(this)
        mFps = findViewById(R.id.fps)
    }

    override fun onClick(v: View) = when (v.id) {
        R.id.play -> {
            clickPlayStop()
        }
        else -> {
        }
    }

    /**
     * 播放与暂停按钮
     */
    private fun clickPlayStop() {
        if (mPlayTask != null) {
            return
        }

        val st: SurfaceTexture = mTextureView!!.surfaceTexture
        val surface = Surface(st)

        val framePlayer : FramePlayer
        try {
            val callback = FrameControlCallback()
            callback.setFps(30)

            val infoCallback = FramePlayer.InfoCallback{
                runOnUiThread{
                    mFps!!.text = String.format("FPS: %d", it)
                }
            }
            framePlayer = FramePlayer(File("sdcard/ffmpegtest/test.mp4"),
                    surface, callback, infoCallback)
        } catch (e: IOException) {
            surface.release()
            return
        }

        adjustAspectRatio(framePlayer.width, framePlayer.height)
        mPlayTask = FramePlayer.PlayTask(framePlayer)
        mPlayTask!!.execute()
    }

    /**
     * 调整TextureView的显示矩阵
     */
    private fun adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
        val surfaceWidth: Int = mTextureView!!.width
        val surfaceHeight: Int = mTextureView!!.height
        val aspectRatio: Double = videoHeight.toDouble() / videoWidth

        val newWidth: Int
        val newHeight: Int
        when (surfaceHeight > (videoWidth * aspectRatio).toInt()) {
            true -> {
                // limited by narrow width; restrict height
                newWidth = surfaceWidth
                newHeight = (surfaceWidth * aspectRatio).toInt()
            }
            false -> {
                // limited by short height; restrict width
                newWidth = (surfaceHeight / aspectRatio).toInt()
                newHeight = surfaceHeight
            }
        }
        val xOff = (surfaceWidth - newWidth) / 2
        val yOff = (surfaceHeight - newHeight) / 2

        val txForm = Matrix()
        mTextureView!!.getTransform(txForm)
        txForm.setScale(newWidth.toFloat() / surfaceWidth, newHeight.toFloat() / surfaceHeight)
//        txForm.postRotate(10F)
        txForm.postTranslate(xOff.toFloat(), yOff.toFloat())
        mTextureView!!.setTransform(txForm)

    }
}