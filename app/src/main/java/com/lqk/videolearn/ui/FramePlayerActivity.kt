package com.lqk.videolearn.ui

import android.app.Activity
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Bundle
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

class FramePlayerActivity : Activity(), View.OnClickListener, FramePlayer.PlayerFeedback {

    private var mTextureView: TextureView? = null

    private var mPlayTask: FramePlayer.PlayTask? = null

    private var mFps: TextView? = null

    private var mShowStopLabel = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_frame)
        mTextureView = findViewById(R.id.frame_player)
        findViewById<Button>(R.id.play).setOnClickListener(this)
        mFps = findViewById(R.id.fps)

        updateControls()
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
        if (mShowStopLabel){
            stopPlayback()
        }else{
            if (mPlayTask != null) {
                return
            }

            val st: SurfaceTexture = mTextureView!!.surfaceTexture
            val surface = Surface(st)

            val framePlayer : FramePlayer
            try {
                val callback = FrameControlCallback()

                val infoCallback = FramePlayer.InfoCallback{
                    runOnUiThread{
                        mFps!!.text = String.format("FPS: %d", it)
                    }
                }
                framePlayer = FramePlayer(File("sdcard/v1080.mp4"),
                        surface, callback, infoCallback)
                framePlayer.setLoop(true)

                if (framePlayer.frameRate != 0){
                    callback.setFps(framePlayer.frameRate)
                }else{
                    callback.setFps(30)
                }

            } catch (e: IOException) {
                surface.release()
                return
            }

            adjustAspectRatio(framePlayer.width, framePlayer.height)
            mPlayTask = FramePlayer.PlayTask(framePlayer, this)

            mShowStopLabel = true
            updateControls()
            mPlayTask!!.execute()
        }
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

    private fun stopPlayback() {
        if (mPlayTask != null) {
            mPlayTask!!.requestStop()
        }
    }

    override fun playbackStopped() {
        mShowStopLabel = false
        mPlayTask = null
        updateControls()
    }

    private fun updateControls() {
        val play = findViewById<View>(R.id.play) as Button
        if (mShowStopLabel) {
            play.setText(R.string.stop_button_text)
        } else {
            play.setText(R.string.play_button_text)
        }
    }

    override fun onPause() {
        // 确保暂停后 不再发送帧，因为view破坏了
        if (mPlayTask != null) {
            stopPlayback()
            mPlayTask!!.waitForStop()
        }

        super.onPause()
    }
}