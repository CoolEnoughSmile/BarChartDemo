package com.ces.barchartdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val HORIZONTAL_AXIS =
        arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")

    private val DATA = floatArrayOf(12f, 24f, 45f, 56f, 89f, 70f, 49f, 22f, 23f, 10f, 12f, 3f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bar_chart.setDataList(DATA,89.toFloat())
        bar_chart.setHorizontalAxis(HORIZONTAL_AXIS)

        next_btn.setOnClickListener {
            intent.setClass(this,SecondActivity::class.java)
            startActivity(intent)
        }
    }
}
