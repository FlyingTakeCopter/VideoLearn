package com.lqk.videolearn.grafika;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 帧播放器 控制每帧显示
 * @author lqk
 */
public class FramePlayer {
    private static final String TAG = FramePlayer.class.getSimpleName();

    private long mTimeStep;
    private File mSourceFile;
    private int mWidth;
    private int mHeight;
    private int mFrameRate;
    private int mDrawFps;
    private Surface mOutputSurface;
    private FrameCallback mFrameCallback;
    private InfoCallback mInfoCallback;

    private volatile boolean mIsStopRequested;

    private boolean mLoop;

    /**
     * 外部UI监听
     */
    public interface PlayerFeedback {
        void playbackStopped();
    }

    /**
     * 帧绘制监听
     */
    public interface FrameCallback{

        /**
         * 预绘制
         * @param presentationTimeUsec 当前的时间戳
         */
        void preRender(long presentationTimeUsec);

        /**
         * 绘制完成
         */
        void postRender();

        /**
         * 循环
         */
        void loopReset();
    }

    public interface InfoCallback{
        void onDrawFps(int fps);
    }

    /**
     * 在此声明防止重复创建
     */
    private MediaCodec.BufferInfo mOutputBufferInfo = new MediaCodec.BufferInfo();

    public FramePlayer(File sourceFile, Surface outputSurface,
                       FrameCallback callback, InfoCallback infoCallback)
            throws IOException {
        mSourceFile = sourceFile;
        mOutputSurface = outputSurface;
        mFrameCallback = callback;
        mInfoCallback = infoCallback;
        // 使用分离器 解析出视频信息
        MediaExtractor extractor = null;
        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(mSourceFile.toString());
            // 获取视频轨道编号
            int videoTrack = selectTrack(extractor);
            if (videoTrack < 0) {
                // 没有视频 直接抛出异常
                throw new RuntimeException("FramePlayer no video track found in " + mSourceFile);
            }
            extractor.selectTrack(videoTrack);
            // 获取宽高
            MediaFormat format = extractor.getTrackFormat(videoTrack);
            int width = format.getInteger(MediaFormat.KEY_WIDTH);
            int height = format.getInteger(MediaFormat.KEY_HEIGHT);
            int rotation = 0;
            if (format.containsKey(MediaFormat.KEY_ROTATION)){
                rotation = format.getInteger(MediaFormat.KEY_ROTATION);
            }
            if (format.containsKey(MediaFormat.KEY_FRAME_RATE)){
                mFrameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            }
            if (format.containsKey(MediaFormat.KEY_I_FRAME_INTERVAL)){
                int iFrameInterval = format.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL);
            }
            if (rotation == 90){
                mWidth = height;
                mHeight = width;
            }else {
                mWidth = width;
                mHeight = height;
            }
        } finally {
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
        }
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    public void setLoop(boolean mLoop) {
        this.mLoop = mLoop;
    }

    public void requestStop() {
        mIsStopRequested = true;
    }

    public void play() throws IOException {
        MediaExtractor extractor = null;
        MediaCodec decoder = null;

        if (!mSourceFile.canRead()) {
            throw new FileNotFoundException("Unable to read " + mSourceFile);
        }

        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(mSourceFile.toString());
            int videoTrack = selectTrack(extractor);
            if (videoTrack < 0) {
                throw new RuntimeException("FramePlayer no video track found in " + mSourceFile);
            }
            extractor.selectTrack(videoTrack);
            // 创建解码器
            MediaFormat format = extractor.getTrackFormat(videoTrack);
            // 获取当前设备的解码器类型 并创建一个解码器
            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            // 切换到 configure 状态 绑定surface
            decoder.configure(format, mOutputSurface, null, 0);
            // 切换到 Running 状态
            decoder.start();

            mTimeStep = System.currentTimeMillis();
            // 开始解码
            doExtract(extractor, videoTrack, decoder);
        } finally {
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
        }
    }

    /**
     * 获得视轨编号
     *
     * @param extractor 分离器
     * @return track
     */
    private static int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (!TextUtils.isEmpty(mime) && mime.startsWith("video/")) {
                Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }

        return -1;
    }


    /**
     * 解码函数
     *
     * @param extractor  分离器 用来读取原始数据
     * @param trackIndex 轨道
     * @param decoder   解码器
     */
    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder) {

        final int TIMEOUT_USEC = 10000;

        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {
            if (mIsStopRequested){
                // 收到停止请求
                return;
            }
            // 给解码器 喂数据
            if (!inputDone) {
                // 获取 解码器 可用的输入 buffer 编号
                // 从Flushed态 切换到 Running态 让编码器 等待接收并处理数据
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    // 获取解码器可用的输入缓冲区(将未解码的数据写进这个缓冲区中，解码器会自动读取并解码)
                    ByteBuffer inputBuffer = decoder.getInputBuffer(inputBufIndex);
                    if (inputBuffer == null){
                        // 没有可用的 输入缓冲区
                        return;
                    }
                    // 从分离器中读取数据，将未解码数据写入的编码器的输入缓冲区，让编码器解码
                    int chunkSize = extractor.readSampleData(inputBuffer, 0);
                    if (chunkSize < 0) {
                        // end of stream
                        // 通过queueInputBuffer来通知解码器当前已经到了结尾处，不需要再传入时间戳
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        // 到结尾了 表示输入完成
                        inputDone = true;
                    } else {
                        // 有效的数据
                        // 从分离器中读取当前时间戳
                        long presentationTimeUs = extractor.getSampleTime();
                        // 通过queueInputBuffer来通知编码器当前数据的大小以及时间戳
                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0);
                        // 请求分离器的下一数据
                        extractor.advance();
                    }
                } else {
                    // 解码器当前没有可用的输入缓冲区
                }
            }

            // 从解码器中读取解码后的数据 解码器上面吃完了 该"拉屎"了
            if (!outputDone) {
                // 获取解码器可用的输出缓冲区状态 如果是有效状态 mOutputBufferInfo则可用
                int decoderStatus = decoder.dequeueOutputBuffer(mOutputBufferInfo, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // 还没有有效的输出
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // 输出缓冲区已经变更
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // 输出格式已更改，后续数据将遵循新的format
                } else if (decoderStatus < 0) {
                    throw new RuntimeException(
                            "unexpected result from decoder.dequeueOutputBuffer: " +
                                    decoderStatus);
                } else {
                    // 循环
                    boolean doLoop = false;
                    // decoderStatus >= 0
                    // 当前的 bufferinfo是终止符 停止读取解码器
                    if ((mOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (mLoop){
                            doLoop = true;
                        }else {
                            outputDone = true;
                        }
                    }
                    // 判断当前是否要render
                    boolean doRender = (mOutputBufferInfo.size != 0);

                    // 一旦我们调用releaseOutputBuffer，缓冲区就会被转发
                    // 转换为纹理的表面纹理。我们无法控制什么时候出现在屏幕上，
                    // 但我们可以控制发布的速度缓冲器。
                    if (doRender){
                        if (mFrameCallback != null){
                            // 通知即将进行绘制
                            mFrameCallback.preRender(mOutputBufferInfo.presentationTimeUs);
                        }
                    }
                    // 如果使用完缓冲区，请调用releaseOutputBuffer将
                    // 1.释放这个编解码器的缓冲区 或者
                    // 2.在surface上渲染它
                    // 如果给解码器configure一个输出的surface，
                    // 设置@Param render为true将会发送这个buffer到输出surface
                    // 一旦surface不再使用/显示,surface会将释放缓冲区到编解码器
                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                    // 在surface上绘制完成
                    if (doRender){
                        if (mFrameCallback != null){
                            // 通知已经绘制完成
                            mFrameCallback.postRender();
                        }

                        // 在绘制完成后 计算当前绘制帧率
                        long curTimeStep = System.currentTimeMillis();
                        if (curTimeStep - mTimeStep > 1000){
                            mTimeStep = curTimeStep;
                            if (mInfoCallback != null){
                                mInfoCallback.onDrawFps(mDrawFps);
                            }
                            mDrawFps = 0;
                        }else {
                            ++mDrawFps;
                        }
                    }

                    // 如果要循环loop
                    if (doLoop){
                        // 将分离器回到开始
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        // 打开输入开关
                        inputDone = false;
                        // 将编码器切会flush状态
                        decoder.flush();
                        // 通知外部
                        mFrameCallback.loopReset();
                    }
                }

            }

        }
    }

    /**
     * 播放线程
     */
    public static class PlayTask implements Runnable {
        private static final int MSG_PLAY_STOPPED = 0;
        // 解码器
        private FramePlayer mPlayer;
        // 单线程化线程池
        ExecutorService singleThreadExecutor;
        // 外部监听
        PlayerFeedback mFeedback;

        private LocalHandler mLocalHandler;

        private final Object mStopLock = new Object();
        private boolean mStopped = false;

        public PlayTask(FramePlayer mPlayer, PlayerFeedback callback) {
            this.mPlayer = mPlayer;
            this.mFeedback = callback;

            mLocalHandler = new LocalHandler();
        }

        public void requestStop(){
            mPlayer.requestStop();
        }

        /**
         * 在线程池中启动播放器进行解码
         */
        public void execute() {
            singleThreadExecutor = Executors.newSingleThreadExecutor();
            singleThreadExecutor.execute(this);
        }

        public void waitForStop(){
            synchronized (mStopLock){
                while (!mStopped){
                    try {
                        mStopLock.wait();
                    }catch (InterruptedException e){

                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                mPlayer.play();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                // 通知当前播放器已经停止
                synchronized (mStopLock){
                    mStopped = true;
                    mStopLock.notifyAll();
                }

                // 通过handler 发送消息 以保证mFeedback可以在正确的线程上运行
                mLocalHandler.sendMessage(mLocalHandler.obtainMessage(MSG_PLAY_STOPPED, mFeedback));

            }
        }

        // 在线程中通过 handler的方式来通知外部UI更新 防止线程阻塞
        private static class LocalHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;

                switch (what) {
                    case MSG_PLAY_STOPPED:
                        PlayerFeedback fb = (PlayerFeedback) msg.obj;
                        fb.playbackStopped();
                        break;
                    default:
                        throw new RuntimeException("Unknown msg " + what);
                }
            }
        }
    }

}
