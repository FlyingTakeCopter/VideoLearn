package com.lqk.videolearn.ui

import android.app.Activity
import android.opengl.GLES20.*
import android.opengl.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.*
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import com.lqk.videolearn.R
import com.lqk.videolearn.gles.*
import com.lqk.videolearn.gles.program.FlatShadedProgram
import com.lqk.videolearn.gles.program.Texture2dProgram
import java.lang.ref.WeakReference
import kotlin.math.min

/**
 * 练习SurfaceHolder.setFixedSize（）
 * http://android-developers.blogspot.com/2013/09/using-hardware-scaler-for-performance.html
 *
 * 该功能的目的是在高分辨率的显示设备上允许游戏以720p或1080p渲染并获得良好效果
 * 当分辨率调低时使我们更容易观测效果
 * 通常情况下分辨率是固定的，获取是通过调整来匹配设备显示比例
 * 但是在这个例子中我们将各种分辨率设置为参数来匹配显示窗口
 */
class HardwareScalerActivity : Activity(), SurfaceHolder.Callback, Choreographer.FrameCallback {

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
    private var mWindowWidthHeight = Array(4){IntArray(size = 2)}
    private var mFlatShadingChecked = false

    // Rendering code runs on this thread.  The thread's life span is tied to the Surface.
    private var mRenderThread: RenderThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hardware_scaler)

        mSelectedSize = SURFACE_SIZE_FULL
        mFullViewWidth = 512
        mFullViewHeight = 512

        updateControls()

        val sv: SurfaceView = findViewById(R.id.hardwareScaler_surfaceView)
        sv.holder.addCallback(this)
    }

    override fun onPause() {
        super.onPause()
        // If the callback was posted, remove it.  This stops the notifications.  Ideally we
        // would send a message to the thread letting it know, so when it wakes up it can
        // reset its notion of when the previous Choreographer event arrived.
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun onResume() {
        super.onResume()

        // If we already have a Surface, we just need to resume the frame notifications.
        if (mRenderThread != null) {
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        // 获取视图的宽度
        val size = holder!!.surfaceFrame
        mFullViewWidth = size.width()
        mFullViewHeight = size.height()

        //
        val windowAspect = mFullViewHeight.toFloat() / mFullViewWidth.toFloat()
        for (i in SURFACE_DIM.indices) {
            if (i == SURFACE_SIZE_FULL) { // special-case for full size
                mWindowWidthHeight[i][0] = mFullViewWidth
                mWindowWidthHeight[i][1] = mFullViewHeight
            } else if (mFullViewWidth < mFullViewHeight) { // portrait
                mWindowWidthHeight[i].set(0, SURFACE_DIM.get(i))
                mWindowWidthHeight[i].set(1, (SURFACE_DIM.get(i) * windowAspect).toInt())
            } else { // landscape
                mWindowWidthHeight[i][0] = (SURFACE_DIM.get(i) / windowAspect) as Int
                mWindowWidthHeight[i][1] = SURFACE_DIM.get(i)
            }
        }

        // Some controls include text based on the view dimensions, so update now.
        updateControls()

        val sv: SurfaceView = findViewById(R.id.hardwareScaler_surfaceView)
        mRenderThread = RenderThread(sv.holder)
        mRenderThread!!.name = "HardwareScaler GL render"
        mRenderThread!!.start()
        mRenderThread!!.waitUntilReady()

        val rh = mRenderThread!!.mHandler
        rh?.sendSetFlatShading(mFlatShadingChecked)
        rh?.sendSurfaceCreated()

        // start the draw events
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        val rh = mRenderThread!!.mHandler
        rh?.sendSurfaceChanged(format, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        val rh = mRenderThread!!.mHandler
        rh?.sendShutdown()
        try {
            mRenderThread!!.join()
        }catch (ie : InterruptedException){
            throw RuntimeException("join was interrupted", ie)
        }
        mRenderThread = null
    }


    override fun doFrame(frameTimeNanos: Long) {
        val rh = mRenderThread!!.mHandler
        if (rh != null){
            Choreographer.getInstance().postFrameCallback(this)
            rh.sendDoFrame(frameTimeNanos)
        }
    }

    fun onRadioButtonClicked(view: View) {
        val newSize: Int

        val rb = view as RadioButton
        if (!rb.isChecked) {
            return
        }

        when (rb.id) {
            R.id.surfaceSizeTiny_radio -> newSize = SURFACE_SIZE_TINY
            R.id.surfaceSizeSmall_radio -> newSize = SURFACE_SIZE_SMALL
            R.id.surfaceSizeMedium_radio -> newSize = SURFACE_SIZE_MEDIUM
            R.id.surfaceSizeFull_radio -> newSize = SURFACE_SIZE_FULL
            else -> throw java.lang.RuntimeException("Click from unknown id " + rb.id)
        }
        mSelectedSize = newSize

        val wh = mWindowWidthHeight[newSize]

        // Update the Surface size.  This causes a "surface changed" event, but does not
        // destroy and re-create the Surface.
        // Update the Surface size.  This causes a "surface changed" event, but does not
        // destroy and re-create the Surface.
        val sv = findViewById<View>(R.id.hardwareScaler_surfaceView) as SurfaceView
        val sh = sv.holder
        sh.setFixedSize(wh[0], wh[1])
    }

    fun onFlatShadingClicked(view: View) {
        val cb = findViewById<View>(R.id.flatShading_checkbox) as CheckBox
        mFlatShadingChecked = cb.isChecked

        val rh: RenderHandler = mRenderThread!!.mHandler!!
        rh.sendSetFlatShading(mFlatShadingChecked)
    }

    /**
     * Updates the on-screen controls to reflect the current state of the app.
     */
    private fun updateControls() {
        configureRadioButton(R.id.surfaceSizeTiny_radio, SURFACE_SIZE_TINY)
        configureRadioButton(R.id.surfaceSizeSmall_radio, SURFACE_SIZE_SMALL)
        configureRadioButton(R.id.surfaceSizeMedium_radio, SURFACE_SIZE_MEDIUM)
        configureRadioButton(R.id.surfaceSizeFull_radio, SURFACE_SIZE_FULL)
        val tv = findViewById<View>(R.id.viewSizeValue_text) as TextView
        tv.text = mFullViewWidth.toString() + "x" + mFullViewHeight
        val cb = findViewById<View>(R.id.flatShading_checkbox) as CheckBox
        cb.isChecked = mFlatShadingChecked
    }

    /**
     * Generates the radio button text.
     */
    private fun configureRadioButton(id: Int, index: Int) {
        val rb: RadioButton
        rb = findViewById<View>(id) as RadioButton
        rb.isChecked = mSelectedSize == index
        rb.setText(SURFACE_LABEL.get(index).toString() + " (" + mWindowWidthHeight[index][0] + "x" +
                mWindowWidthHeight[index][1] + ")")
    }

    /**
     * 这个类操作所有opengl渲染
     *
     * 使用Choreographer来配合设备的vsync(垂直同步),每一个vsync我们发送一帧
     * 我们并不能准确的知道我们渲染的帧什么时候会被绘制，
     * 但是至少我们能保证每帧的时间间隔是一样的
     *
     * 在surfaceCreated之后开始渲染线程
     */
    class RenderThread : Thread {
        /**
         * 为了获得正确的looper,RenderHandler必须在渲染线程上创建，
         * 但是RenderHandler是被UI线程使用的，
         * 所以我们要用volatile来修饰他，以确保UI线程可以看到构造的对象
         */
        @Volatile
        var mHandler: RenderHandler? = null
        // 用来做互斥量
        private val mStartLock = Object()
        private var mReady = false

        // 可能在UI线程中被更新
        @Volatile
        var mSurfaceHolder: SurfaceHolder? = null
        // 显示辅助类
        var mEglCore: EglCore? = null
        var mWindowSurface: WindowSurface? = null
        // 两种gl program
        var mFlatProgram: FlatShadedProgram? = null
        var mTexProgram: Texture2dProgram? = null
        // 两种纹理ID
        var mCoarseTexture = -1
        var mFineTexture = -1
        // 是否使用平面纯色绘制
        var mUseFlatShading = false

        // 视口矩阵
        var mDisplayProjectionMatrix = FloatArray(16)
        // 两种显示的图形
        var mTriDrawable = Drawable2d(Drawable2d.Prefab.TRIANGLE)
        var mRectDrawable = Drawable2d(Drawable2d.Prefab.RECTANGLE)

        // 三种显示的精灵
        var mTri: Sprite2d? = null
        var mRect: Sprite2d? = null
        var mEdges: Array<Sprite2d>? = null

        // 记录每帧正方形的位置
        var mRectVelX = 0f
        var mRectVelY = 0f

        // 记录内部边界
        var mInnerLeft = 0.0f
        var mInnerRight = 0.0f
        var mInnerTop = 0.0f
        var mInnerBottom = 0.0f

        // 记录前一帧的时间
        var mPrevTimeNanos = 0L

        constructor(mSurfaceHolder: SurfaceHolder?) {
            this.mSurfaceHolder = mSurfaceHolder

            mTri = Sprite2d(mTriDrawable)
            mRect = Sprite2d(mRectDrawable)
            mEdges = Array<Sprite2d>(4) {
                Sprite2d(mRectDrawable)
            }
        }

        /**
         * 线程主入口
         * 在SurfaceHolder创建并关联之前，线程不会启动
         * 这样就不用一直等待 surfaceCreate 消息到达
         */
        override fun run() {
            Looper.prepare()
            mHandler = RenderHandler(this)
            mEglCore = EglCore()
            synchronized(mStartLock) {
                mReady = true
                mStartLock.notify()
            }
            Looper.loop()

            mEglCore!!.release()

            synchronized(mStartLock) {
                mReady = false
            }
        }

        // 等待 直到渲染线程的 RenderHandler已经准备好接受消息
        fun waitUntilReady() {
            synchronized(mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait()
                    } catch (e: InterruptedException) {

                    }
                }
            }
        }

        fun shutdown() {
            Looper.myLooper().quit()
        }

        fun surfaceCreated() {
            val surface = mSurfaceHolder!!.surface
            prepareGL(surface)
        }

        fun prepareGL(surface: Surface) {
            mWindowSurface = WindowSurface(mEglCore, surface, false)
            mWindowSurface!!.makeCurrent()

            mFlatProgram = FlatShadedProgram()
            mTexProgram = Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D)

            mCoarseTexture = GeneratedTexture.createTestTexture(GeneratedTexture.Image.COARSE)
            mFineTexture = GeneratedTexture.createTestTexture(GeneratedTexture.Image.FINE)

            // 清空背景色
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            // 关闭深度测试
            glDisable(GL_DEPTH_TEST)
            //
            glDisable(GL_CULL_FACE)
        }

        /**
         * 处理绘制尺寸，调整视口大小，必须在绘制之前调用
         */
        fun surfaceChanged(width: Int, height: Int) {
            /**
             * 这个方法会在第一次 surfaceCreate的时候调用，并且在setFiexSize()之后调用
             * 注意调用时机，是在surface"即将"更改大小，而不是"已经"更改大小
             * 对EGL surface查询会确认surface的尺寸是否已经变化
             * 如果在下一次swapbuffer()之后查询，会看到新的尺寸
             *
             * 为了平滑过度，我们应该按照旧尺寸绘制直到查询到的surface尺寸真正的改变
             *
             * 我认为一个正常的app不会去调用setFixedSize(),所以样例中的这种情况不应该会出现
             * 并且这种场景并不是值得去做的事
             */
            glViewport(0, 0, width, height)

            Matrix.orthoM(mDisplayProjectionMatrix,0,0.0f, width.toFloat(),
                    0.0f, height.toFloat(), -1.0f, 1.0f)

            val smallDim = min(width, height)

            /**
             * 根据窗口大小设置初始形状大小/位置/速度
             * 运动在所有设备上都是同样的感觉，但是实际的路线将会根据屏幕比例计算
             * 不能通过固定的值来设置投影矩阵，这样才能保证正方形在所有设备上都显示正常
             */
            mTri!!.mColor!![0] = 0.0f
            mTri!!.mColor!![1] = 1.0f
            mTri!!.mColor!![2] = 0.0f
            mTri!!.mTextureId = mFineTexture
            mTri!!.mScaleX = smallDim.toFloat() / 3.0f
            mTri!!.mScaleY = smallDim.toFloat() / 3.0f
            mTri!!.mPosX = width / 2.0f
            mTri!!.mPosY = height / 2.0f
            mRect!!.mColor!![0] = 1f
            mRect!!.mColor!![1] = 0f
            mRect!!.mColor!![2] = 0f
            mRect!!.mTextureId = mCoarseTexture
            mRect!!.mScaleX = smallDim / 5.0f
            mRect!!.mScaleY = smallDim / 5.0f
            mRect!!.mPosX = width / 2.0f
            mRect!!.mPosY = height / 2.0f
            mRectVelX = 1 + smallDim / 4.0f
            mRectVelY = 1 + smallDim / 5.0f

            // 边界
            val edgeWidth = 1 + width / 64.0f
            mEdges!![0].mColor!![0] = 0.5f
            mEdges!![0].mColor!![1] = 0.5f
            mEdges!![0].mColor!![2] = 0.5f
            mEdges!![0].mScaleX = edgeWidth
            mEdges!![0].mScaleY = height.toFloat()
            mEdges!![0].mPosX = edgeWidth / 2.0f
            mEdges!![0].mPosY= edgeWidth / 2.0f

            mEdges!![1].mColor!![0] = 0.5f
            mEdges!![1].mColor!![1] = 0.5f
            mEdges!![1].mColor!![2] = 0.5f
            mEdges!![1].mScaleX = edgeWidth
            mEdges!![1].mScaleY = height.toFloat()
            mEdges!![1].mPosX = width - edgeWidth / 2.0f
            mEdges!![1].mPosY= edgeWidth / 2.0f

            mEdges!![2].mColor!![0] = 0.5f
            mEdges!![2].mColor!![1] = 0.5f
            mEdges!![2].mColor!![2] = 0.5f
            mEdges!![2].mScaleX = width.toFloat()
            mEdges!![2].mScaleY = edgeWidth
            mEdges!![2].mPosX = width / 2.0f
            mEdges!![2].mPosY= height - edgeWidth / 2.0f

            mEdges!![3].mColor!![0] = 0.5f
            mEdges!![3].mColor!![1] = 0.5f
            mEdges!![3].mColor!![2] = 0.5f
            mEdges!![3].mScaleX = width.toFloat()
            mEdges!![3].mScaleY = edgeWidth
            mEdges!![3].mPosX = width / 2.0f
            mEdges!![3].mPosY= edgeWidth / 2.0f

            // Inner bounding rect, used to bounce objects off the walls.
            mInnerLeft = edgeWidth
            mInnerBottom = edgeWidth
            mInnerRight = width - 1 - edgeWidth
            mInnerTop = height - 1 - edgeWidth

        }

        fun releaseGL(){
            mWindowSurface?.release()
            mWindowSurface = null

            mFlatProgram?.release()
            mFlatProgram = null

            mTexProgram?.release()
            mTexProgram = null

            mEglCore!!.makeNothingCurrent()
        }

        /**
         * Handles the frame update.  Runs when Choreographer signals.
         */
        fun doFrame(timeStampNanos: Long) {
            /**
             * 如果不能保证每秒60帧，那么就要采用丢帧策略
             * 通过系统时间System.nanoTime和当前时间timeStampNanos做比较
             * 如果超过16ms(每帧需要的时间: 1000ms / 60fps),那么就采用丢帧策略
             */
            update(timeStampNanos)
            val diff = (System.nanoTime() - timeStampNanos) / 1000000
            if (diff > 15) { // too much, drop a frame
                return
            }
            draw()
            mWindowSurface!!.swapBuffers()
        }

        /**
         * 通过时间差 计算动画的位置
         */
        private fun update(timeStampNanos: Long){
            // 计算时间差
            var intervalNanos = 0L
            if (mPrevTimeNanos == 0L){
                intervalNanos = 0L
            }else{
                intervalNanos = timeStampNanos - mPrevTimeNanos

                val ONE_SECOND_NANOS = 1000000000L //一秒的纳秒数
                if (intervalNanos > ONE_SECOND_NANOS) {
                    // 当时间差大于1秒 说明之前绘制停止了
                    intervalNanos = 0
                }
            }

            mPrevTimeNanos = timeStampNanos

            val ONE_BILLION_F = 1000000000.0f
            val elapsedSeconds = intervalNanos / ONE_BILLION_F

            // Spin the triangle.  We want one full 360-degree rotation every 3 seconds,
            // or 120 degrees per second.
            // Spin the triangle.  We want one full 360-degree rotation every 3 seconds,
            // or 120 degrees per second.
            val SECS_PER_SPIN = 3
            val angleDelta = 360.0f / SECS_PER_SPIN * elapsedSeconds
            mTri!!.mAngle = mTri!!.mAngle!! + angleDelta

            // Bounce the rect around the screen.  The rect is a 1x1 square scaled up to NxN.
            // We don't do fancy collision detection, so it's possible for the box to slightly
            // overlap the edges.  We draw the edges last, so it's not noticeable.
            //            mTri.setRotation(mTri.getRotation() + angleDelta);
            // Bounce the rect around the screen.  The rect is a 1x1 square scaled up to NxN.
            // We don't do fancy collision detection, so it's possible for the box to slightly
            // overlap the edges.  We draw the edges last, so it's not noticeable.
            var xpos: Float = mRect!!.mPosX!!
            var ypos: Float = mRect!!.mPosY!!
            val xscale: Float = mRect!!.mScaleX!!
            val yscale: Float = mRect!!.mScaleY!!
            xpos += mRectVelX * elapsedSeconds
            ypos += mRectVelY * elapsedSeconds
            if (mRectVelX < 0 && xpos - xscale / 2 < mInnerLeft ||
                    mRectVelX > 0 && xpos + xscale / 2 > mInnerRight + 1) {
                mRectVelX = -mRectVelX
            }
            if (mRectVelY < 0 && ypos - yscale / 2 < mInnerBottom ||
                    mRectVelY > 0 && ypos + yscale / 2 > mInnerTop + 1) {
                mRectVelY = -mRectVelY
            }
            mRect!!.mPosX = xpos
            mRect!!.mPosY = ypos
        }

        /**
         * Draws the scene.
         */
        private fun draw() {
            // Clear to a non-black color to make the content easily differentiable from
            // the pillar-/letter-boxing.
            glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT)
            // Textures may include alpha, so turn blending on.
            glEnable(GL_BLEND)
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
            // 表示源颜色乘以自身的alpha 值，目标颜色乘以1.0减去源颜色的alpha值，
            // 这样一来，源颜色的alpha值越大，则产生的新颜色中源颜色所占比例就越大，而目标颜色所占比例则减 小。
            // 这种情况下，我们可以简单的将源颜色的alpha值理解为“不透明度”。这也是混合时最常用的方式
            //            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            // 完全使用目标颜色 不使用源颜色
            //            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
            // 完全使用源颜色和目标颜色，最终的颜色实际上就是两种颜色的简单相加。
            // 例如红色(1, 0, 0)和绿色(0, 1, 0)相加得到(1, 1, 0)，结果为黄色。
            //            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
            if (mUseFlatShading) {
                mTri!!.draw(mFlatProgram!!, mDisplayProjectionMatrix)
                mRect!!.draw(mFlatProgram!!, mDisplayProjectionMatrix)
            } else {
                mTri!!.draw(mTexProgram!!, mDisplayProjectionMatrix)
                mRect!!.draw(mTexProgram!!, mDisplayProjectionMatrix);
            }
            glDisable(GL_BLEND)
            for (i in 0..3) {
                mEdges!![i].draw(mFlatProgram!!, mDisplayProjectionMatrix)
            }
        }
    }

    /**
     *  RenderThread的handler,用于从UI线程向opengl render线程发消息
     *  这个对象在RenderThread中创建，但是send方法是在UI线程中调用的
     */
    class RenderHandler : Handler {
        private val MSG_SURFACE_CREATED = 0
        private val MSG_SURFACE_CHANGED = 1
        private val MSG_DO_FRAME = 2
        private val MSG_FLAT_SHADING = 3
        private val MSG_SHUTDOWN = 5

        // 这里并不需要弱引用，因为在Looper.quit之后会退出
        // 但是并没有真正的 干掉他
        private var mWeakREnderThread: WeakReference<RenderThread>? = null

        constructor(rt: RenderThread) {
            mWeakREnderThread = WeakReference<RenderThread>(rt)
        }

        fun sendSurfaceCreated() {
            sendMessage(obtainMessage(MSG_SURFACE_CREATED))
        }

        fun sendSurfaceChanged(format: Int, width: Int, height: Int) {
            sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height))
        }

        fun sendDoFrame(frameTimeNanos: Long) {
            sendMessage(obtainMessage(MSG_DO_FRAME,
                    (frameTimeNanos shr 32).toInt(), frameTimeNanos.toInt()))
        }

        fun sendSetFlatShading(useFlatShading: Boolean) {
            sendMessage(obtainMessage(MSG_FLAT_SHADING, if (useFlatShading) 1 else 0, 0))
        }

        fun sendShutdown() {
            sendMessage(obtainMessage(MSG_SHUTDOWN))
        }

        override fun handleMessage(msg: Message?) {
            val what = msg!!.what

            val renderThread = mWeakREnderThread!!.get()
            if (renderThread == null) {
                return
            }

            when (what) {
                MSG_SURFACE_CREATED -> {
                    renderThread.surfaceCreated()
                }
                MSG_SURFACE_CHANGED -> {
                    renderThread.surfaceChanged(msg.arg1, msg.arg2)
                }
                MSG_DO_FRAME -> {
                    val timestamp = msg.arg1.toLong() shl 32 or
                            (msg.arg2.toLong() and 0xffffffffL)
                    renderThread.doFrame(timestamp)
                }
                MSG_FLAT_SHADING -> {
                    renderThread.mUseFlatShading = msg.arg1 != 0
                }
                MSG_SHUTDOWN -> {
                    renderThread.shutdown()
                }
                else -> {
                    throw RuntimeException("unknown message $what")
                }
            }
        }
    }

}