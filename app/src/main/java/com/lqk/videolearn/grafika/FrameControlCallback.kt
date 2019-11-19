package com.lqk.videolearn.grafika

/**
 * 帧控制器
 */
class FrameControlCallback : FramePlayer.FrameCallback {
    private val TAG = FrameControlCallback::class.java.simpleName

    /**
     * 代表每帧所需要的纳秒数量
     */
    private var mFixedFrameDurationUsec: Long = 0
    /**
     * 代表当前要绘制帧的timestep
     */
    private var mPrevPresentUsec: Long = 0
    /**
     * 代表当前计算机的时间
     */
    private var mPrevMonoUsec: Long = 0

    /**
     * 设置播放的帧率
     * @param fps fps
     */
    fun setFps(fps: Int) {
        mFixedFrameDurationUsec = ONE_MILLION / fps
    }

    override fun preRender(presentationTimeUsec: Long) {
        //对于第一帧，要记录当前时钟，和当前帧的显示时间
        //对于后续帧，要sleep一段时间以确保播放速度
        if (mPrevMonoUsec == 0L) {
            mPrevMonoUsec = System.nanoTime() / 1000
            mPrevPresentUsec = presentationTimeUsec
        } else {
            // 计算偏移时长，如果是设定了速度，那么按照速率为偏移时长
            var frameDelta: Long = if (mFixedFrameDurationUsec != 0L) {
                mFixedFrameDurationUsec
            } else {
                presentationTimeUsec - mPrevPresentUsec
            }
            frameDelta = when{
                (frameDelta < 0) -> 0
                (frameDelta > 10 * ONE_MILLION) -> (5 * ONE_MILLION)
                else -> frameDelta
            }
            // 计算何时显示的timeStep
            val desiredUsec = mPrevMonoUsec + frameDelta
            // 当前时长
            var nowUsec = System.nanoTime() / 1000
            // 等待 直到到达显示的timestep
            while (nowUsec < desiredUsec - 100) { // 要sleep的时长
                var sleepTimeUsec = desiredUsec - nowUsec
                // Thread.sleep最大值 500000
                if (sleepTimeUsec > 500000) {
                    sleepTimeUsec = 500000
                }
                try {
                    Thread.sleep(sleepTimeUsec / 1000, (sleepTimeUsec % 1000).toInt() * 1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                nowUsec = System.nanoTime() / 1000
            }
            // 偏移当前时间戳 计算机时钟
            mPrevMonoUsec += frameDelta
            // 偏移当前时间戳 帧timestep
            mPrevPresentUsec += frameDelta
        }
    }

    override fun postRender() {}


    companion object {

        /**
         * 1s的纳秒数
         */
        private const val ONE_MILLION = 1000000L
    }
}