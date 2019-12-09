package com.lqk.videolearn.image;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.lqk.videolearn.R;
import com.lqk.videolearn.image.filter.ColorFilter;

public class SGLViewActivity extends Activity {
    SGLView sglView;
    boolean isHalf = false;

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
                sglView.getRender().setFilterRender(new ColorFilter(getResources(), ColorFilter.Filter.NONE));
                break;
            case R.id.filter_black:
                sglView.getRender().setFilterRender(new ColorFilter(getResources(), ColorFilter.Filter.GRAY));
                break;
            case R.id.filter_cold:
                sglView.getRender().setFilterRender(new ColorFilter(getResources(), ColorFilter.Filter.COOL));
                break;
            case R.id.filter_warm:
                sglView.getRender().setFilterRender(new ColorFilter(getResources(), ColorFilter.Filter.WARM));
                break;
            case R.id.filter_mohu:
                sglView.getRender().setFilterRender(new ColorFilter(getResources(), ColorFilter.Filter.BLUR));
                break;
            case R.id.filter_fdj:
                sglView.getRender().setFilterRender(new ColorFilter(getResources(), ColorFilter.Filter.MAGN));
                break;
            default:
                throw new RuntimeException("Click from unknown id " + rb.getId());
        }
        // 设置之前的参数
        sglView.getRender().getFilterRender().setIsHalf(isHalf);

        // requestRender
        sglView.requestRender();
    }

    public void onHalfClicked(View view) {
        isHalf = !isHalf;
        sglView.getRender().getFilterRender().setIsHalf(isHalf);
        // requestRender
        sglView.requestRender();
    }
}
