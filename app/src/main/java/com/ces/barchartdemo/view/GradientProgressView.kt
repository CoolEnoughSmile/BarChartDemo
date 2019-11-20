package com.ces.barchartdemo.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class GradientProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mRectF: RectF? = null

    private var mTextBound: Rect? = null

    private var mCx: Int = 0

    private var mCy: Int = 0

    var mProgress : Int = 0
    set(value) {
        field = value
        invalidate()
    }

    var mDuration : Long = DEFAULT_ANIMATION_DURATION

    private var mGradientCirclePaint: Paint? = null

    private var mBackgroundCirclePaint: Paint? = null

    private var mTextPaint: Paint? = null

    companion object{
        private val DEFAULT_ANIMATION_DURATION = 3000L
        private val DEFALUT_COLORS = intArrayOf(Color.BLUE,Color.GREEN)
        var mGradientColors = DEFALUT_COLORS
    }


    init {
        mTextPaint = Paint()
        mTextPaint?.isAntiAlias = true
        val textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,22F,resources.displayMetrics)
        mTextPaint?.textSize = textSize

        mGradientCirclePaint = Paint()
        mBackgroundCirclePaint = Paint()

        mRectF = RectF()

        mTextBound = Rect()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        initRectF(w,h)
        mCx = w / 2
        mCy = h / 2
        //初始化背景圆环画笔和渐变画笔
        initCiclePaint()
    }

    private fun initRectF(w: Int,h: Int){
        //初始化圆弧边界
        val left = paddingLeft.toFloat()
        val top = paddingTop.toFloat()
        val right = w - paddingRight.toFloat()
        val bottom = h - paddingBottom.toFloat()
        mRectF?.set(left,top,right,bottom)
    }

    private fun initCiclePaint(){
        val shader = SweepGradient(mCx.toFloat(),mCy.toFloat(),mGradientColors,null)
        mGradientCirclePaint?.shader = shader
        mGradientCirclePaint?.style = Paint.Style.STROKE
        mGradientCirclePaint?.strokeWidth = 30F
        mGradientCirclePaint?.isAntiAlias = true
        mGradientCirclePaint?.strokeCap = Paint.Cap.ROUND

        mBackgroundCirclePaint?.style = Paint.Style.STROKE
        mBackgroundCirclePaint?.strokeWidth = 30F
        mBackgroundCirclePaint?.isAntiAlias = true
        mBackgroundCirclePaint?.color = Color.LTGRAY
    }

    override fun onDraw(canvas: Canvas?) {
        mRectF?.let { mBackgroundCirclePaint?.let { it1 -> canvas?.drawArc(it,0F,360F,false, it1) } }
        val startAngle =  -90F
        val sweepAngle = mProgress.toFloat() / 100 * 360
        mTextPaint?.getTextBounds(mProgress.toString(),0,mProgress.toString().length,mTextBound)
        val x = mCx - (mTextBound?.width() ?: 0) / 2
        val y = mCy + (mTextBound?.height() ?: 0) / 2
        mTextPaint?.let {
            canvas?.drawText(mProgress.toString(),x.toFloat(),y.toFloat(), it)
        }
        //绘制进度
        mRectF?.let { mGradientCirclePaint?.let { it1 ->
            canvas?.drawArc(it,startAngle,sweepAngle,false,
                it1
            )
        } }
    }

    fun startAnimation(degree: Int){
        val objectAnimator = ObjectAnimator.ofInt(this,"mProgress",0,degree)
        objectAnimator.duration = mDuration
        objectAnimator.start()
    }

}