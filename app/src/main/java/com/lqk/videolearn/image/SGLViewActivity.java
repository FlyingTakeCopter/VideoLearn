package com.lqk.videolearn.image;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.lqk.videolearn.R;

public class SGLViewActivity extends Activity {
    SGLView sglView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sglview);
        sglView = (SGLView) findViewById(R.id.sglview);
    }

    public void onRadioButtonClicked(View view) {
        int newSize;

        RadioButton rb = (RadioButton) view;
        if (!rb.isChecked()) {
            return;
        }

        switch (rb.getId()) {
            case R.id.filter_origin:
                break;
            case R.id.filter_black:
                break;
            case R.id.filter_cold:
                break;
            case R.id.filter_warm:
            case R.id.filter_mohu:
                break;
            case R.id.filter_fdj:
                break;
            default:
                throw new RuntimeException("Click from unknown id " + rb.getId());
        }

        // requestRender
        sglView.requestRender();
    }

    public void onHalfClicked(View view) {

        // requestRender
        sglView.requestRender();
    }
}
