package com.ces.barchartdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_second.*



class SecondActivity : AppCompatActivity() {

    private val HORIZONTAL_AXIS =
        arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")

    private val DATA = arrayOf(12, 24, 45, 56, 89, 70, 49, 22, 23, 10, 12, 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        line_chart_view.setHorizontalAxis(HORIZONTAL_AXIS)
        line_chart_view.setDataList(DATA,89)

        next_btn.setOnClickListener {
            intent.setClass(this,ThirdActivity::class.java)
            startActivity(intent)
        }
    }
}
