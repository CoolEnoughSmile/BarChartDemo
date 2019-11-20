package com.ces.barchartdemo.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.ces.barchartdemo.R

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mDataList = mutableListOf<Int>()

    private var mMax: Int = 0

    private var mHorizontalAxis = mutableListOf<String>()

    private var mDots = mutableListOf<Dot>()

    private var mStep: Int = 0

    private var mGap: Int = 0

    private var mAxisPaint: Paint? = null

    private var mLinePaint: Paint? = null

    private var mDotPaint: Paint? = null

    private var mGradientPaint: Paint? = null

    private var mTextRect: Rect? = null

    private var mPath: Path? = null

    private var mGradientPath: Path? = null

    private var mRadius: Int = 0

    private var mClickRadius: Int = 0

    private var mSelectedDotIndex: Int = -1

    private var mSelectedDotColor: Int = 0

    private var mNormalDotColor: Int = 0

    private var mLineColor: Int = 0

    companion object {
        private var DEFAULT_GRADIENT_COLORS = intArrayOf(Color.BLUE,Color.GREEN)
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LineChartView)
        mLineColor = typedArray.getColor(R.styleable.LineChartView_line_color,Color.BLACK)
        mNormalDotColor = typedArray.getColor(R.styleable.LineChartView_dot_normal_color, Color.BLACK)
        mSelectedDotColor = typedArray.getColor(R.styleable.LineChartView_dot_selected_color,Color.RED)
        typedArray.recycle()

        initPaint()

        mPath = Path()
        mGradientPath = Path()

        mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4F,resources.displayMetrics).toInt()
        mClickRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10F,resources.displayMetrics).toInt()
        mTextRect = Rect()
        mGap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8F,resources.displayMetrics).toInt()
    }

    fun initPaint(){
        mAxisPaint = Paint()
        mAxisPaint!!.isAntiAlias = true
        mAxisPaint!!.textSize = 20F
        mAxisPaint!!.textAlign = Paint.Align.CENTER

        mDotPaint = Paint()
        mDotPaint!!.isAntiAlias = true

        mLinePaint = Paint()
        mLinePaint!!.isAntiAlias = true
        mLinePaint!!.strokeWidth = 3F
        mLinePaint!!.style = Paint.Style.STROKE
        mLinePaint!!.color = mLineColor

        mGradientPaint = Paint()
        mGradientPaint!!.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //清空点的集合
        mDots.clear()
        //去除padding，计算绘制区域的宽高
        val width = w - paddingLeft - paddingRight
        val height = h - paddingTop - paddingBottom
        mStep = width / mDataList.count()
        //通过坐标文本笔画计算绘制x轴第一个坐标文本占据的矩形边界，这里主要获取其高度，
        //为计算maxBarHeight提供数据，maxBarHeight为值线图的最大高度
        mAxisPaint?.getTextBounds(mHorizontalAxis[0], 0, mHorizontalAxis[0].length, mTextRect)
        //计算折线图最大高度与最大像素大小，mTextRect.height为底部x轴坐标文本的高度
        //mGap为坐标文本与折线之间间隔大小的变量
        val maxBarHeight = height - mTextRect!!.height() - mGap
        //计算折线图最大高度与最大数据值的比值
        val heightRatio = maxBarHeight / mMax
        //遍历所有的点
        mDataList.forEachIndexed { index, it ->
            //初始化对应的点
            val dot = Dot()
            dot.value = it
            dot.transformedValue = dot.value * heightRatio
            dot.x = mStep * index + paddingLeft
            dot.y = paddingTop + maxBarHeight - dot.transformedValue
            //当是第一个点时，将路径移动到该点
            if (index == 0) {
                mPath?.moveTo(dot.x.toFloat(), dot.y.toFloat())
                mGradientPath?.moveTo(dot.x.toFloat(),dot.y.toFloat())
            } else {
                //路径连线到点dot
                mPath?.lineTo(dot.x.toFloat(), dot.y.toFloat())
                mGradientPath?.lineTo(dot.x.toFloat(),dot.y.toFloat())
            }
            //如果到了最后一个点
            if (index == mDataList.count() - 1) {
                val bottom = (paddingTop + maxBarHeight).toFloat()
                //将渐变路径连接到最后一个点在竖直方向到最低点
                mGradientPath?.lineTo(dot.x.toFloat(),bottom)
                val firstDot = mDots.first()
                //连接到第一个点在竖直方向点最低点
                mGradientPath?.lineTo(firstDot.x.toFloat(),bottom)
                //连接到第一个点，形成闭合区域
                mGradientPath?.lineTo(firstDot.x.toFloat(),firstDot.y.toFloat())
            }
            mDots.add(dot)
        }

        //LinearGradient构造方法里前两个参数(0,0)表示渐变起始点；第三第四个参数(0,getHeight())
        //表示渐变结束点；第五个参数表示渐变颜色点种类和排序；第六个参数配置各种渐变颜色所占点比重，
        //null表示渐变在前四个参数设置点范围内平分；第七个参数表示前四个参数配置点范围之外点绘制模
        //式。因为画笔绘制点范围可能要比前四个参数的范围要大，CLAMP模式表示超出范围部分的颜色为
        //第五个参数中颜色数组最后的一种颜色，REPEAT模式表示超出范围部分的颜色按照第五、六个参数重复
        //再分配一次；MIRROR模式表示超出范围部分的颜色按照第五个参数中颜色数组从后往前分配，给人一种镜像的感觉
        val shader = LinearGradient(0F,0F,0F, height.toFloat(),DEFAULT_GRADIENT_COLORS,null,Shader.TileMode.CLAMP);
        mGradientPaint?.shader = shader
    }

    override fun onDraw(canvas: Canvas?) {
        //绘制折线路径
        mPath?.let { mLinePaint?.let { it1 -> canvas?.drawPath(it, it1) } }
        mDots.forEachIndexed { index, dot ->
            //绘制坐标文本
            val axis = mHorizontalAxis[index]
            val x = paddingLeft + index * mStep
            val y = height - paddingBottom
            mAxisPaint?.let { canvas?.drawText(axis, x.toFloat(), y.toFloat(), it) }
            if (index == mSelectedDotIndex) {//如果是用户点击点位置
                //设置点点画笔颜色为用户点击的颜色
                mDotPaint?.color = mSelectedDotColor
                mAxisPaint!!.textSize = 24F
                //绘制数据文本
                val valueTextX = dot.x
                val valueTextY = dot.y - mRadius - mGap
                mAxisPaint?.let {
                    canvas?.drawText(mDataList[index].toString(),valueTextX.toFloat(),valueTextY.toFloat(),
                        it
                    )
                }
            } else {
                mAxisPaint!!.textSize = 20F
                mDotPaint?.color = mNormalDotColor
            }
            //绘制点
            mDotPaint?.color = mNormalDotColor
            mDotPaint?.let {
                canvas?.drawCircle(
                    dot.x.toFloat(), dot.y.toFloat(), mRadius.toFloat(),
                    it
                )
            }
        }
        mGradientPath?.let { mGradientPaint?.let { it1 -> canvas?.drawPath(it, it1) } }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mSelectedDotIndex =  getClickDotIndex(event.x,event.y)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                mSelectedDotIndex = -1
                invalidate()
            }
        }
        return true
    }

    private fun getClickDotIndex(x: Float, y: Float): Int{
        var index = -1
        mDots.forEachIndexed{ i,dot ->
            //初始化接受点击事件到矩形区域
            val left = dot.x - mClickRadius
            val top = dot.y - mClickRadius
            val right = dot.x + mClickRadius
            val bottom = dot.y + mClickRadius
            //判断点(x,y)是否在矩形区域内
            if (x > left && x < right && y > top && y < bottom) {
                return i
            }
        }
        return index
    }

    fun setDataList(dataList: Array<Int>, max: Int) {
        mDataList.addAll(dataList)
        mMax = max
    }

    fun setHorizontalAxis(horizontalAxis: Array<String>) {
        mHorizontalAxis.addAll(horizontalAxis)
    }

    private class Dot(
        //绘制点的坐标(x,y)
        var x: Int = 0,
        var y: Int = 0,
        //点的数值
        var value: Int = 0,
        //计算点点原始数值value对应高度像素大小
        var transformedValue: Int = 0
    )
}