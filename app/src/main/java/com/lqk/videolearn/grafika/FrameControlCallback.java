package com.lqk.videolearn.grafika;

/**
 * 帧控制器
 */
public class FrameControlCallback implements FramePlayer.FrameCallback {

    /**
     * 1s的纳秒数
     */
    private static final long ONE_MILLION = 1000000L;

    /**
     * 代表每帧所需要的纳秒数量
     */
    private long mFixedFrameDurationUsec;

    /**
     * 代表当前要绘制帧的timestep
     */
    private long mPrevPresentUsec;

    /**
     * 代表当前计算机的时间
     */
    private long mPrevMonoUsec;

    /**
     * 设置播放的帧率
     * @param fps fps
     */
    public void setFps(int fps){
        mFixedFrameDurationUsec = ONE_MILLION / fps;
    }

    @Override
    public void preRender(long presentationTimeUsec) {
        //对于第一帧，要记录当前时钟，和当前帧的显示时间
        //对于后续帧，要sleep一段时间以确保播放速度
        if (mPrevMonoUsec == 0){
            mPrevMonoUsec = System.nanoTime() / 1000;
            mPrevPresentUsec = presentationTimeUsec;
        }else {
            // 计算偏移时长，如果是设定了速度，那么按照速率为偏移时长
            long frameDelta;
            if (mFixedFrameDurationUsec != 0){
                frameDelta = mFixedFrameDurationUsec;
            }else {
                frameDelta = presentationTimeUsec - mPrevPresentUsec;
            }

            if (frameDelta < 0){
                frameDelta = 0;
            }else if (frameDelta == 0){

            }else if (frameDelta > 10 * ONE_MILLION){
                frameDelta = 5 * ONE_MILLION;
            }
            // 计算何时显示的timestep
            long desiredUsec = mPrevMonoUsec + frameDelta;
            // 当前时长
            long nowUsec = System.nanoTime() / 1000;
            // 等待 直到到达显示的timestep
            while(nowUsec < (desiredUsec - 100)){
                // 要sleep的时长
                long sleepTimeUsec = desiredUsec - nowUsec;
                // Thread.sleep最大值 500000
                if (sleepTimeUsec > 500000){
                    sleepTimeUsec = 500000;
                }
                try {
                    Thread.sleep(sleepTimeUsec / 1000, (int)(sleepTimeUsec % 1000) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nowUsec = System.nanoTime() / 1000;
            }
            // 偏移当前时间戳 计算机时钟
            mPrevMonoUsec += frameDelta;
            // 偏移当前时间戳 帧timestep
            mPrevPresentUsec += frameDelta;
        }

    }

    @Override
    public void postRender() {

    }
}
