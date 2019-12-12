package com.lqk.videolearn.camera;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.lqk.videolearn.R;

public class CameraActivity extends Activity implements View.OnClickListener {

    CameraView cameraDrawer;
    Button switchCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraDrawer = (CameraView)findViewById(R.id.mCameraView);
        switchCamera = (Button) findViewById(R.id.switchcamera);
        switchCamera.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraDrawer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraDrawer.onPause();
    }

    @Override
    public void onClick(View v) {
        cameraDrawer.switchCamera();
    }
}
