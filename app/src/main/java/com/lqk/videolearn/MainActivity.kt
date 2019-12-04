package com.lqk.videolearn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.lqk.videolearn.ui.DoubleDecodeActivity
import com.lqk.videolearn.ui.FramePlayerActivity
import com.lqk.videolearn.ui.HardwareScalerActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.frame_player).setOnClickListener(this)
        findViewById<Button>(R.id.double_decoder).setOnClickListener(this)
        findViewById<Button>(R.id.hardware_scaler).setOnClickListener(this)
    }

    override fun onClick(v: View) = when(v.id){
        R.id.frame_player -> {
            startActivity(Intent(this, FramePlayerActivity::class.java))
        }
        R.id.double_decoder -> {
            startActivity(Intent(this, DoubleDecodeActivity::class.java))
        }
        R.id.hardware_scaler -> {
            startActivity(Intent(this, HardwareScalerActivity::class.java))
        }
        else -> {}
    }
}