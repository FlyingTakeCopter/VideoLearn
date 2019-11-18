package com.lqk.videolearn.grafika;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
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
    private static final String TAG = FramePlayer.TAG;

    private File mSourceFile;
    private int mWidth;
    private int mHeight;
    private Surface mOutputSurface;

    private volatile boolean mIsStop;

    /**
     * 在此声明防止重复创建
     */
    private MediaCodec.BufferInfo mOutputBufferInfo = new MediaCodec.BufferInfo();

    public FramePlayer(File sourceFile, Surface outputSurface) throws IOException {
        mSourceFile = sourceFile;
        mOutputSurface = outputSurface;
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
            mWidth = format.getInteger(MediaFormat.KEY_WIDTH);
            mHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
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
            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            // 切换到 configure 状态 绑定surface
            decoder.configure(format, mOutputSurface, null, 0);
            // 切换到 Running 状态
            decoder.start();

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
                    // decoderStatus >= 0
                    // 当前的 bufferinfo是终止符 停止读取解码器
                    if ((mOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }
                    // 判断当前是否要render
                    boolean doRender = (mOutputBufferInfo.size != 0);

                    // 一旦我们调用releaseOutputBuffer，缓冲区就会被转发
                    // 转换为纹理的表面纹理。我们无法控制什么时候出现在屏幕上，
                    // 但我们可以控制发布的速度缓冲器。
                    if (doRender){
                        // 通知即将进行绘制
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
                        // 通知已经绘制完成
                    }

                    // 如果要循环loop
                    if (false){
                        // 将分离器回到开始
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        // 将编码器切会flush状态
                        decoder.flush();
                        // 打开输入开关
                        inputDone = false;
                    }
                }

            }

        }
    }

    public static class PlayTask implements Runnable {
        // 解码器
        private FramePlayer mPlayer;
        // 单线程化线程池
        ExecutorService singleThreadExecutor;

        public PlayTask(FramePlayer mPlayer) {
            this.mPlayer = mPlayer;
        }


        /**
         * 在线程池中启动播放器进行解码
         */
        public void execute() {
            singleThreadExecutor = Executors.newSingleThreadExecutor();
            singleThreadExecutor.execute(this);
        }

        @Override
        public void run() {
            try {
                mPlayer.play();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
