package com.lqk.videolearn.solarsystem;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.lqk.videolearn.R;

public class SSViewActivity extends Activity {
    SSView ssView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssview);
        ssView = (SSView) findViewById(R.id.ssview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ssView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ssView.onResume();
    }


}
