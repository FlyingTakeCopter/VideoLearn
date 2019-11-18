package com.lqk.videolearn.ui;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.Nullable;

import com.lqk.videolearn.R;
import com.lqk.videolearn.grafika.FramePlayer;

import java.io.File;
import java.io.IOException;

/**
 * 帧播放器样例
 * @author lqk
 */
public class FramePlayerActivity extends Activity implements View.OnClickListener {
    private static final String TAG = FramePlayer.class.getSimpleName();

    /**
     * 用来显示解码结果的 Surface
     */
    TextureView mTextureView;

    /**
     * 播放线程
     */
    FramePlayer.PlayTask mPlayTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_frame);
        mTextureView = findViewById(R.id.frame_player);
        findViewById(R.id.play).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play:
                clickPlayStop();
                break;
            default:
                break;
        }
    }

    /**
     * 播放与暂停按钮
     */
    private void clickPlayStop(){
        if (mPlayTask != null){
            return;
        }

        SurfaceTexture st = mTextureView.getSurfaceTexture();
        Surface surface = new Surface(st);

        FramePlayer framePlayer = null;
        try{
            framePlayer = new FramePlayer(new File("sdcard/ffmpegtest/test.mp4"), surface);
        }catch (IOException e){
            surface.release();
            return;
        }
        adjustAspectRatio(framePlayer.getWidth(), framePlayer.getHeight());
        mPlayTask = new FramePlayer.PlayTask(framePlayer);
        mPlayTask.execute();
    }

    /**
     * 调整TextureView的显示矩阵
     * @param videoWidth
     * @param videoHeight
     */
    private void adjustAspectRatio(int videoWidth, int videoHeight){
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }
}
