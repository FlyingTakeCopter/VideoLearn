package com.lqk.videolearn.ui

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.lqk.videolearn.R
import com.lqk.videolearn.grafika.FrameControlCallback
import com.lqk.videolearn.grafika.FramePlayer
import java.io.File
import java.io.IOException

/**
 * 双解码线程显示
 * 旋转更新TextureView绑定的SurfaceTexture,解码线程不停
 */
class DoubleDecodeActivity : AppCompatActivity(){
    companion object{
        private val TAG = DoubleDecodeActivity::class.java.simpleName

        private const val VIDEO_COUNT : Int = 2

        var sVideoRunning : Boolean = false

        private val sBlob = arrayOfNulls<VideoBlob>(VIDEO_COUNT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_double_decoder)

        if (!sVideoRunning){
            sBlob[0] = VideoBlob(findViewById(R.id.double_decoder_1), "sdcard/v1080.mp4",0)
            sBlob[1] = VideoBlob(findViewById(R.id.double_decoder_2), "sdcard/v1080.mp4",1)
        }else{
            sBlob[0]!!.recreateView(findViewById(R.id.double_decoder_1))
            sBlob[1]!!.recreateView(findViewById(R.id.double_decoder_2))
        }
    }

    override fun onPause() {
        super.onPause()

        val finishing : Boolean = isFinishing

        for (i in 0 until VIDEO_COUNT) {
            if (finishing) {
                sBlob[i]!!.stopPlayback()
                sBlob[i] = null
            }
        }
        sVideoRunning = !finishing
    }

    /**
     * 视频容器
     * 封装解码器 和 显示的surface
     * 避免在方向改变时重新创建解码器
     *
     * 方向的改变可能会导致UI线程的EGL上下文销毁并重新创建
     * 所以我们需要在销毁时从EGL中分离surfacetecture，并在
     * 新的表面结构变得可用。幸运的是，TextureView为我们做到了这一点
     */
    private class VideoBlob(textureView: TextureView, path: String, index: Int) :
            TextureView.SurfaceTextureListener{
        var LTAG : String? = null
        var mTextureView : TextureView? = null
        var mVideoPath : String = path

        var mSurfaceTexture : SurfaceTexture? = null
        var mFrameControlCallback : FrameControlCallback
        var mPlayMovieThread : PlayMovieThread? = null

        init {
            LTAG = TAG + index
            mFrameControlCallback = FrameControlCallback()
            recreateView(textureView)
        }

        /**
         * 执行部分构造。VideoBlob已经创建，
         * 但是活动已重新创建，因此我们需要更新视图。
         */
        fun recreateView(view: TextureView?){
            this.mTextureView = view
            mTextureView!!.surfaceTextureListener = this

            if (mSurfaceTexture != null){
                view!!.surfaceTexture = mSurfaceTexture
            }
        }

        /**
         * 停止播放线程 并销毁surfaceTexture
         */
        fun stopPlayback(){
            mPlayMovieThread!!.requestStop()

            //不在需要这个了
            //这也是一个信号让onSurfaceTexturDestoryed()知道 它可以告诉TextureView去释放SurfaceTexture
            mSurfaceTexture = null
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            /**
             * 此时SurfaceTexture已经和EGL context解绑，所以我们不需要那么做
             * 如果要关闭，成员变量SurfaceTexture会被置null
             * 所以在这种情况下 要返回true(表示TextureView可以释放ST)
             */
            return mSurfaceTexture == null
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            /**
             * 如果是第一次，那么使用TextureView提供的SurfaceTexture
             * 如果不是，将会用当前的替换原始的
             */
            if (mSurfaceTexture == null){
                mSurfaceTexture = surface

                mPlayMovieThread = PlayMovieThread(File(mVideoPath),
                        Surface(surface), mFrameControlCallback)

            }else{
                /**
                 * 在Android中不能这样做<=4.4。
                 * 纹理视图不添加新SurfaceTexture上的侦听器，因此它从不看到任何更新。
                 * 需要在activity onCreate（）中发生--请参阅recreateView（）。
                 * if (mSurfaceTexture != null){
                 *     view!!.surfaceTexture = mSurfaceTexture
                 * }
                 */
            }
        }

    }

    // 简单的播放解码线程
    private class PlayMovieThread(var mFile: File, var mSurface: Surface,
                                  var mFrameControlCallback: FrameControlCallback) : Thread() {
        var mPlayer: FramePlayer? = null

        init {
            start()
        }

        fun requestStop(){
            mPlayer?.requestStop()
        }

        override fun run() {
            try {
                mPlayer = FramePlayer(mFile, mSurface, mFrameControlCallback, null)
//                mFrameControlCallback.setFps(mPlayer!!.frameRate)
                mFrameControlCallback.setFps(15)
                mPlayer!!.setLoop(true)
                mPlayer!!.play()
            }catch (e : IOException){

            }finally {
                mSurface.release()
            }
        }
    }
}