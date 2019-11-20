package com.ces.barchartdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_third.*

class ThirdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        progressView.mProgress = 100
        progressView.mDuration = 1000
        start_animation.setOnClickListener {
            progressView.startAnimation(100)
        }
        next_btn.setOnClickListener {
            intent.setClass(this,ThirdActivity::class.java)
            startActivity(intent)
        }
    }
}
