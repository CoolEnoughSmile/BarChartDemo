package com.ces.barchartdemo.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //柱状图数据列表
    private var mDataList = floatArrayOf()
    //水平方向x轴坐标
    private var mHorizontalAxis = emptyArray<String>()
    //数组中的最大值
    private var mMax : Float = 0.toFloat()
    //Bar集合
    private var mBars = mutableListOf<Bar>()
    //柱状条宽度
    private var mBarWidth : Int = 0
    //柱状条顶部圆弧半径
    private var mRadius : Int = 0

    private var mAxisPaint : Paint? = null

    private var mBarPaint : Paint? =null

    private var mTextRect : Rect? = null

    private var mTemp : RectF? = null
    //坐标文本与柱状条之间间隔的变量
    private var mGap : Int = 0

    private var enableGrowAnimation : Boolean = true

    private var mSelectedIndex : Int = -1

    companion object {
        private val BAR_GROW_STEP : Int = 15
        private val DELAY : Long = 10
    }

    init {
        mAxisPaint = Paint()
        mAxisPaint!!.isAntiAlias = true
        mAxisPaint!!.textSize = 20F
        mAxisPaint!!.textAlign = Paint.Align.CENTER

        mBarPaint = Paint()
        mBarPaint!!.color = Color.BLUE
        mBarPaint!!.isAntiAlias = true
        mBarPaint!!.textAlign = Paint.Align.CENTER
        mBarPaint!!.textSize = 24F

        mTextRect = Rect()
        mTemp = RectF()
        //柱状条宽度 默认8dp
        mBarWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8F, resources.displayMetrics).toInt()
        //柱状条与坐标文本之间的间隔大小，默认8dp
        mGap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8F, resources.displayMetrics).toInt()
    }

    override fun onDraw(canvas: Canvas?) {
        if (enableGrowAnimation) {
            drawBarsWidthAnimation(canvas)
        } else {
            drawBars(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //判断数据源是否为空
        if (mDataList.isEmpty() || mHorizontalAxis.isEmpty()) {
            Log.d("BarChartView","data is empty")
            return
        }
        //清空柱状条Bar的集合
        mBars.clear()

        //去除padding，计算绘制所有的柱状条所占的宽和高
        val width = w - paddingLeft - paddingRight
        val height = h - paddingTop - paddingBottom

        //按照数据集合的大小平分宽度
        val step = width / mDataList.count()

        //mBarWidth为柱状条宽度的变量，可以设置mRadius为柱状条宽度的一半
        mRadius = mBarWidth / 2

        //计算第一条柱状条的左边位置
        var barLeft = paddingLeft + step / 2 - mRadius

        //通过坐标文本画笔计算绘制x轴第一个坐标文本占据的矩形边界，这里主要获取其高度，
        //为计算maxBarHeight提供数据
        mAxisPaint?.getTextBounds(mHorizontalAxis[0], 0, mHorizontalAxis[0].length, mTextRect)

        //计算柱状条高度的最大像素值大小，mTextRect.height为底部x轴坐标文本的高度，
        //mGap为坐标文本与柱状条之间间隔大小的变量
        val maxBarHeight = height - mTextRect?.height()!! - mGap

        //计算柱状条最大像素大小与最大数据的比值
        val heightRatio = maxBarHeight / mMax

        //循环遍历数据集合，初始化所有的柱状条Bar对象
        for (data in mDataList){
            // 创建柱状条对象
            val bar = Bar()
            //设置原始数据
            bar.value = data
            bar.transformedValue = bar.value * heightRatio
            //计算绘制柱状条的四个位置
            bar.left = barLeft
            bar.top = (paddingTop + maxBarHeight - bar.transformedValue).toInt()
            bar.right = barLeft + mBarWidth
            bar.bottom = paddingTop + maxBarHeight

            //初始化绘制柱状条时当前的top值，用作动画
            bar.currentTop = bar.bottom

            //将初始化好的Bar添加到集合中
            mBars.add(bar)

            //更新柱状条的左边位置，为下一个Bar对象做准备
            barLeft += step
        }
    }

    fun drawBarsWidthAnimation(canvas: Canvas?){
        //遍历所有的Bar
        mBars.forEachIndexed { index,bar ->
            //绘制坐标文本
            val axis = mHorizontalAxis[index]
            val textX = (bar.left + mRadius).toFloat()
            val textY = (height - paddingBottom).toFloat()
            mAxisPaint?.let { canvas?.drawText(axis,textX,textY, it) }

            //更新当前柱状条顶部位置变量，BAR_GROW_STEP为柱状条增长步长，即让柱状条长高BAR_GROW_STEP长度
            bar.currentTop -= BAR_GROW_STEP

            //当计算出来的currentTop小于柱状条本来的top值时，说明越界
            if (bar.currentTop <= bar.top) {
                //将currentTop重置为本来的top值，解决越界问题
                bar.currentTop = bar.top

                //高度最高的柱状条的顶部位置是paddingTop，说明高度最高的进度条也达到
                //其最高点，可以停止增长动画了，于是将enableGrowAnimation设置为false
                if (bar.value == mMax.toFloat() && bar.currentTop == bar.top) {
                    enableGrowAnimation = false
                }
            }
            //绘制圆角柱状条
            mTemp?.set(bar.left.toFloat(),bar.currentTop.toFloat(),bar.right.toFloat(),bar.bottom.toFloat())
            mBarPaint?.let {
                mTemp?.let { it1 ->
                    canvas?.drawRoundRect(
                        it1, mRadius.toFloat(),
                        mRadius.toFloat(), it)
                }
            }
        }

        //延时触发重新绘制，调用onDraw方法
        if (enableGrowAnimation) {
            postInvalidateDelayed(DELAY)
        }
    }

    fun drawBars(canvas: Canvas?){
        //遍历所有Bar对象，一个个绘制
        mBars.forEachIndexed { index,bar ->
            //绘制底部x轴坐标文本
            val axis = mHorizontalAxis[index] //获取对应位置的坐标文本
            //计算绘制文本的起始位置坐标*（textX， textY），textX为柱状条的中线位置，
            //由于我们对画笔设置了Paint.Align.CENTER,所以绘制出来
            //的文本的中线与柱状条的中线是重合的
            val textX = (bar.left + mRadius).toFloat()
            val textY = (height - paddingBottom).toFloat()
            //绘制坐标文本
            mAxisPaint?.let { canvas?.drawText(axis, textX,textY, it) }
            //设置柱状条的矩形颜色选中和未选中的颜色
            if (index == mSelectedIndex) {
                mBarPaint?.color = Color.RED
                val x = (bar.left + mRadius).toFloat()
                val y = (bar.top - mGap).toFloat()
                mBarPaint?.let { canvas?.drawText(bar.value.toString(),x,y, it) }
                mBarPaint?.color = Color.YELLOW
            }else {
                mBarPaint?.color = Color.BLUE
            }
            //设置柱状条矩形的四个位置
            mTemp?.set(bar.left.toFloat(), bar.top.toFloat(), bar.right.toFloat(), bar.bottom.toFloat())
            //绘制圆角矩形
            mBarPaint?.let { mTemp?.let { it1 -> canvas?.drawRoundRect(it1, mRadius.toFloat(),
                mRadius.toFloat(), it) } }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (enableGrowAnimation) {
            return false
        }
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                for (i : Int in 0 until mBars.count()) {
                    if (mBars[i].isInside(event.getX(),event.getY())){
                        enableGrowAnimation = false
                        mSelectedIndex = i
                        invalidate()
                        break
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mSelectedIndex = -1
                enableGrowAnimation = false
                invalidate()
            }
        }
        return true
    }

    /**
     * 设置水平方向x轴坐标值
     * @param horizontalAxis 坐标值数组， 如{"1","2","3","4","5","6","7","8","9","10","11","12"}
     */
    fun setHorizontalAxis(horizontalAxis : Array<String>){
        mHorizontalAxis = horizontalAxis
    }

    /**
     * 设置柱状图数据
     * @param dataList 数据数组，如{12,24,45,56,89,70,49,22,23,10,12,3}
     * @param max 数组中的最大值，如89，最大值用来计算绘制时的高度比例
     */
    fun setDataList(dataList : FloatArray, max: Float){
        mDataList = dataList
        mMax = max
    }

    private class Bar(
        //绘制柱状条的四个位置
        var left: Int = 0,
        var top: Int = 0,
        var right: Int = 0,
        var bottom: Int = 0,
        var value: Float = 0.toFloat(), //柱状条原始数据的大小
        var transformedValue: Float = 0.toFloat(), //柱状条原始数据大小转换成对应的像素大小
        var currentTop: Int = 0 //柱状图动画中用到，表示柱状条动画过程中顶部位置的变量，取值范围为[0,top]
    ) {
        fun isInside(x: Float, y: Float): Boolean {
            return x > left && x < right && y > top && y < bottom
        }
    }

}