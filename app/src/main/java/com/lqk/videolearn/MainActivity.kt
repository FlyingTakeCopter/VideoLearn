package com.lqk.videolearn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.lqk.videolearn.ui.FramePlayerActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.frame_player).setOnClickListener(this)
    }

    override fun onClick(v: View) = when(v.id){
        R.id.frame_player -> {
            startActivity(Intent(this, FramePlayerActivity::class.java))
        }
        else -> {}
    }
}