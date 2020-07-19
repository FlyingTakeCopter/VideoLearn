package com.example.ffmpeg_4_2_2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class FFmpegMainActivity extends AppCompatActivity {
    TextView version;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ffmpeg_main_layout);

        version = (TextView) findViewById(R.id.version);

        version.setText(NativeApi.native_getFFmpegVersion());

    }
}
