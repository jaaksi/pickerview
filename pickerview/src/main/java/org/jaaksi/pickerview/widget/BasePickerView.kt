package org.jaaksi.pickerview.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.widget.Scroller
import org.jaaksi.pickerview.R
import org.jaaksi.pickerview.adapter.WheelAdapter
import org.jaaksi.pickerview.util.Util.dip2px

/**
 * 滚动选择器,带惯性滑动
 * https://github.com/1993hzw/Androids/blob/master/androids/src/cn/forward/androids/views/ScrollPickerView.java
 * 做一下修改：
 * 改为adapter填充数据，不再直接持有数据源。提供两种常用adapter
 * 增加属性itemSize，支持高度为wrap_content时，根据itemCount和visibleItemCount计算总高度，便于动态改变visibleItemCount
 * 提供接口Formatter 外界可以对显示的文案处理。比如添加把2018变成2018年，8变成 08月，MixedTimePicker中用处更大
 * 绘制中心item由绘制drawable改为由接口CenterDecoration控制，提供默认实现。用户可以自定义，更强大，使用更方便
 * 修改数据源size < visibleItemCount时强制不进行循环
 * 修改默认选中第0个
 */
abstract class BasePickerView<T> @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mVisibleItemCount = sDefaultVisibleItemCount // 可见的item数量

    /**
     * 设置快速滑动时是否惯性滚动一段距离
     *
     * @param inertiaScroll 快速滑动时是否惯性滚动一段距离
     */
    var isInertiaScroll = true // 快速滑动时是否惯性滚动一段距离，默认开启
    private var mIsCirculation = false // 是否循环滚动，默认关闭
    var isIsCirculation = false // 是否有必要循环滚动
        private set

    /**
     * 是否允许父元素拦截事件，设置true后可以保证在ScrollView下正常滚动
     *//*
         不允许父组件拦截触摸事件，设置为true为不允许拦截，此时该设置才生效
         当嵌入到ScrollView等滚动组件中，为了使该自定义滚动选择器可以正常工作，请设置为true
        */
    var isDisallowInterceptTouch = false
    private var mSelected = 0 // 当前选中的item下标
    protected var mAdapter: WheelAdapter<T>? = null
    var itemHeight = 0 // 每个条目的高度,当垂直滚动时，高度=mMeasureHeight／mVisibleItemCount
        private set
    var itemWidth = 0 // 每个条目的宽度，当水平滚动时，宽度=mMeasureWidth／mVisibleItemCount
        private set
    private var mItemSize = 0 // 当垂直滚动时，mItemSize = mItemHeight;水平滚动时，mItemSize = mItemWidth

    // 标记是否使用默认的 centerPosition = mVisibleItemCount / 2
    private var mUseDefaultCenterPosition = true
    private var mCenterPosition = -1

    /**
     * @return 中间item的起始坐标y(不考虑偏移), 当垂直滚动时，y= mCenterPosition*mItemHeight
     */
    // 中间item的位置，0<=mCenterPosition＜mVisibleItemCount，默认为 mVisibleItemCount / 2
    var centerY = 0 // 中间item的起始坐标y(不考虑偏移),当垂直滚动时，y= mCenterPosition*mItemHeight
        private set

    /**
     * @return 中间item的起始坐标x(不考虑偏移), 当垂直滚动时，x = mCenterPosition*mItemWidth
     */
    var centerX = 0 // 中间item的起始坐标x(不考虑偏移),当垂直滚动时，x = mCenterPosition*mItemWidth
        private set

    /**
     * @return 当垂直滚动时，mCenterPoint = mCenterY;水平滚动时，mCenterPoint = mCenterX
     */
    var centerPoint = 0 // 当垂直滚动时，mCenterPoint = mCenterY;水平滚动时，mCenterPoint = mCenterX
        private set
    private var mLastMoveY = 0f // 触摸的坐标y
    private var mLastMoveX = 0f // 触摸的坐标X
    private var mMoveLength = 0f // item移动长度，负数表示向上移动，正数表示向下移动
    private val mGestureDetector: GestureDetector
    var listener: OnSelectedListener? = null
        private set

    /**
     * 设置内容Formatter
     *
     * @param formatter formatter
     */
    var formatter: Formatter? = null
    private val mScroller: Scroller
    var isFling = false // 是否正在惯性滑动
        private set
    var isMovingCenter = false // 是否正在滑向中间
        private set

    // 可以把scroller看做模拟的触屏滑动操作，mLastScrollY为上次触屏滑动的坐标
    private var mLastScrollY = 0 // Scroller的坐标y
    private var mLastScrollX = 0 // Scroller的坐标x

    /**
     * 设置是否允许手动触摸滚动
     */
    var isDisallowTouch = false // 不允许触摸
    private var isTouching = false //手指按下
    private var mSelectedOnTouch = 0
    private val mPaint: Paint
    private var mCenterDecoration: CenterDecoration? = null

    /** 是否绘制 CenterDecoration  */
    private var mDrawIndicator = sDefaultDrawIndicator

    // 当没有数据时是否绘制指示器
    private var mDrawIndicatorNoData = DEFAULT_DRAW_INDICATOR_NO_DATA

    /**
     * 设置 单击切换选项或触发点击监听器
     */
    var isCanTap = true // 单击切换选项或触发点击监听器
    private var mIsHorizontal = false // 是否水平滚动
    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BasePickerView)
            mVisibleItemCount = typedArray.getInt(
                R.styleable.BasePickerView_pv_visible_item_count, sDefaultVisibleItemCount
            )
            mItemSize = typedArray.getDimensionPixelSize(R.styleable.BasePickerView_pv_item_size, 0)
            val centerPosition =
                typedArray.getInt(R.styleable.BasePickerView_pv_center_item_position, -1)
            if (centerPosition != -1) {
                setSafeCenterPosition(centerPosition)
            }
            setIsCirculation(
                typedArray.getBoolean(
                    R.styleable.BasePickerView_pv_is_circulation, sDefaultIsCirculation
                )
            )
            isDisallowInterceptTouch = typedArray.getBoolean(
                R.styleable.BasePickerView_pv_disallow_intercept_touch, isDisallowInterceptTouch
            )
            mIsHorizontal = typedArray.getInt(
                R.styleable.BasePickerView_pv_orientation, if (mIsHorizontal) 1 else 2
            ) == 1
            typedArray.recycle()
        } else {
            setIsCirculation(sDefaultIsCirculation)
        }
        if (mItemSize == 0) mItemSize = dip2px(context, sDefaultItemSize.toFloat())
    }

    /**
     * 设置 中心装饰
     *
     * @param centerDecoration 中心装饰
     */
    fun setCenterDecoration(centerDecoration: CenterDecoration?) {
        mCenterDecoration = centerDecoration
    }

    /**
     * 设置是否绘制指示器
     *
     * @param drawIndicator 是否绘制指示器
     */
    fun setDrawIndicator(drawIndicator: Boolean) {
        mDrawIndicator = drawIndicator
    }

    /**
     * 设置没有数据时是否绘制指示器
     *
     * @param drawIndicatorNoData 没有数据时是否绘制指示器
     */
    fun setDrawIndicatorNoData(drawIndicatorNoData: Boolean) {
        mDrawIndicatorNoData = drawIndicatorNoData
    }

    override fun onDraw(canvas: Canvas) {
        val noData = mAdapter == null || mAdapter!!.itemCount <= 0
        if (mDrawIndicator && (!noData || mDrawIndicatorNoData)) {
            if (mCenterDecoration == null) {
                mCenterDecoration = DefaultCenterDecoration(context)
            }
            mCenterDecoration!!.drawIndicator(
                this, canvas, centerX, centerY, centerX + itemWidth, centerY + itemHeight
            )
        }
        if (noData) return
        isIsCirculation = mIsCirculation && mVisibleItemCount < mAdapter!!.itemCount

        // 1.只绘制可见的item，找到绘制的起始点
        // 比较头和尾找距离中心点较远的
        val length = Math.max(mCenterPosition + 1, mVisibleItemCount - mCenterPosition)
        var position: Int
        //int start = Math.min(length, mAdapter.getItemCount());
        val start: Int
        // fix 当itemcount <= visibleCount时，设置循环也不进行循环绘制
        start = if (isIsCirculation) {
            length
        } else {
            Math.min(length, mAdapter!!.itemCount)
        }

        // 2.绘制mCenterPoint上下两边的item：当前选中的绘制在mCenter
        for (i in start downTo 1) { // 先从远离中间位置的item绘制，当item内容偏大时，较近的item覆盖在较远的上面
            if (i <= mCenterPosition + 1) {  // 上面的items,相对位置为 -i
                // 根据是否循环，计算出偏离mCenterPoint i个item对应的position
                position =
                    if (mSelected - i < 0) mAdapter!!.itemCount + mSelected - i else mSelected - i
                // 传入位置信息，绘制item
                if (isIsCirculation) {
                    drawItem(
                        canvas,
                        mAdapter!!.getItem(position),
                        position,
                        -i,
                        mMoveLength,
                        centerPoint + mMoveLength - i * mItemSize
                    )
                } else if (mSelected - i >= 0) { // 非循环滚动
                    // 如果当前选中的下标 < 偏离mCenter i个距离的，就不绘制了
                    drawItem(
                        canvas,
                        mAdapter!!.getItem(position),
                        position,
                        -i,
                        mMoveLength,
                        centerPoint + mMoveLength - i * mItemSize
                    )
                }
            }
            if (i <= mVisibleItemCount - mCenterPosition) {  // 下面的items,相对位置为 i
                position =
                    if (mSelected + i >= mAdapter!!.itemCount) mSelected + i - mAdapter!!.itemCount else mSelected + i
                // 传入位置信息，绘制item
                if (isIsCirculation) {
                    drawItem(
                        canvas,
                        mAdapter!!.getItem(position),
                        position,
                        i,
                        mMoveLength,
                        centerPoint + mMoveLength + i * mItemSize
                    )
                } else if (mSelected + i < mAdapter!!.itemCount) { // 非循环滚动
                    // 如果当前选中的下标 + 偏移mCenter i个距离的 > 数据个数，也不绘制
                    drawItem(
                        canvas,
                        mAdapter!!.getItem(position),
                        position,
                        i,
                        mMoveLength,
                        centerPoint + mMoveLength + i * mItemSize
                    )
                }
            }
        }
        // 选中的item
        drawItem(
            canvas,
            mAdapter!!.getItem(mSelected),
            mSelected,
            0,
            mMoveLength,
            centerPoint + mMoveLength
        )
    }

    /**
     * 绘制item
     *
     * @param data 　数据集
     * @param position 在data数据集中的位置
     * @param relative 相对中间item的位置,relative==0表示中间item,relative<0表示上（左）边的item,relative>0表示下(右)边的item
     * @param moveLength 中间item滚动的距离，moveLength<0则表示向上（右）滚动的距离，moveLength＞0则表示向下（左）滚动的距离
     * @param top 当前绘制item的坐标,当垂直滚动时为顶部y的坐标；当水平滚动时为item最左边x的坐标
     */
    abstract fun drawItem(
        canvas: Canvas?, data: T?, position: Int, relative: Int, moveLength: Float, top: Float
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 根据方向判断是不是MeasureSpec.EXACTLY的，如vertical，height=match_parent则根据高度计算itemSize，否则根据itemSize设置高度
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        if (mIsHorizontal) {
            if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
                mItemSize = MeasureSpec.getSize(widthMeasureSpec) / mVisibleItemCount
            } else {
                widthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(mItemSize * mVisibleItemCount, MeasureSpec.EXACTLY)
            }
        } else {
            // 如果高度为MeasureSpec.EXACTLY，则size=height/count
            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
                mItemSize = MeasureSpec.getSize(heightMeasureSpec) / mVisibleItemCount
            } else {
                heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(mItemSize * mVisibleItemCount, MeasureSpec.EXACTLY)
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 测量结果之后的尺寸才是有效的，调用reset重新计算
        reset()
    }

    private fun reset() {
        // bug fix 使用标记，避免 default和用户设置的相互覆盖
        if (mUseDefaultCenterPosition) {
            mCenterPosition = mVisibleItemCount / 2
        }
        if (mIsHorizontal) {
            itemHeight = measuredHeight
            itemWidth = mItemSize
            centerY = 0
            centerX = mCenterPosition * itemWidth
            centerPoint = centerX
        } else {
            itemHeight = mItemSize
            itemWidth = measuredWidth
            centerY = mCenterPosition * itemHeight
            centerX = 0
            centerPoint = centerY
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isDisallowTouch) { // 不允许触摸
            return true
        }
        if (mAdapter == null || mAdapter!!.itemCount <= 0) {
            return false
        }
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            mSelectedOnTouch = mSelected
        }
        if (mGestureDetector.onTouchEvent(event)) {
            return true
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                isTouching = true
                mMoveLength += if (mIsHorizontal) {
                    if (Math.abs(event.x - mLastMoveX) < 0.1f) {
                        return true
                    }
                    event.x - mLastMoveX
                } else {
                    if (Math.abs(event.y - mLastMoveY) < 0.1f) {
                        return true
                    }
                    event.y - mLastMoveY
                }
                mLastMoveY = event.y
                mLastMoveX = event.x
                checkCirculation()
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                isTouching = false
                mLastMoveY = event.y
                mLastMoveX = event.x
                if (mMoveLength == 0f) {
                    if (mSelectedOnTouch != mSelected) { //前后发生变化
                        notifySelected()
                    }
                } else {
                    moveToCenter() // 滚动到中间位置
                }
            }

            MotionEvent.ACTION_CANCEL -> isTouching = false
        }
        return true
    }

    /**
     *
     */
    private fun computeScroll(curr: Int, end: Int, rate: Float) {
        if (rate < 1) { // 正在滚动
            if (mIsHorizontal) {
                // 可以把scroller看做模拟的触屏滑动操作，mLastScrollX为上次滑动的坐标
                mMoveLength = mMoveLength + curr - mLastScrollX
                mLastScrollX = curr
            } else {
                // 可以把scroller看做模拟的触屏滑动操作，mLastScrollY为上次滑动的坐标
                mMoveLength = mMoveLength + curr - mLastScrollY
                mLastScrollY = curr
            }
            checkCirculation()
            invalidate()
        } else { // 滚动完毕
            isMovingCenter = false
            mLastScrollY = 0
            mLastScrollX = 0

            // 直接居中，不通过动画
            mMoveLength = if (mMoveLength > 0) { //// 向下滑动
                if (mMoveLength < mItemSize / 2) {
                    0f
                } else {
                    mItemSize.toFloat()
                }
            } else {
                if (-mMoveLength < mItemSize / 2) {
                    0f
                } else {
                    -mItemSize.toFloat()
                }
            }
            checkCirculation()
            notifySelected()
            invalidate()
        }
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) { // 正在滚动
            mMoveLength = if (mIsHorizontal) {
                // 可以把scroller看做模拟的触屏滑动操作，mLastScrollX为上次滑动的坐标
                mMoveLength + mScroller.currX - mLastScrollX
            } else {
                // 可以把scroller看做模拟的触屏滑动操作，mLastScrollY为上次滑动的坐标
                mMoveLength + mScroller.currY - mLastScrollY
            }
            mLastScrollY = mScroller.currY
            mLastScrollX = mScroller.currX
            checkCirculation() //　检测当前选中的item
            invalidate()
        } else { // 滚动完毕
            if (isFling) {
                isFling = false
                if (equalsFloat(mMoveLength.toDouble(), 0.0)) { //惯性滑动后的位置刚好居中的情况
                    notifySelected()
                } else {
                    moveToCenter() // 滚动到中间位置
                }
            } else if (isMovingCenter) { // 选择完成，回调给监听器
                notifySelected()
            }
        }
    }

    fun cancelScroll() {
        mLastScrollY = 0
        mLastScrollX = 0
        isMovingCenter = false
        isFling = isMovingCenter
        mScroller.abortAnimation()
        stopAutoScroll()
    }

    // 检测当前选择的item位置
    private fun checkCirculation() {
        if (mMoveLength >= mItemSize) { // 向下滑动
            // 该次滚动距离中越过的item数量
            val span = (mMoveLength / mItemSize).toInt()
            mSelected -= span
            if (mSelected < 0) {  // 滚动顶部，判断是否循环滚动
                if (isIsCirculation) {
                    do {
                        mSelected = mAdapter!!.itemCount + mSelected
                    } while (mSelected < 0) // 当越过的item数量超过一圈时
                    mMoveLength = (mMoveLength - mItemSize) % mItemSize
                } else { // 非循环滚动
                    mSelected = 0
                    mMoveLength = mItemSize.toFloat()
                    if (isFling) { // 停止惯性滑动，根据computeScroll()中的逻辑，下一步将调用moveToCenter()
                        mScroller.forceFinished(true)
                    }
                    if (isMovingCenter) { //  移回中间位置
                        scroll(mMoveLength, 0)
                    }
                }
            } else {
                mMoveLength = (mMoveLength - mItemSize) % mItemSize
            }
        } else if (mMoveLength <= -mItemSize) { // 向上滑动
            // 该次滚动距离中越过的item数量
            val span = (-mMoveLength / mItemSize).toInt()
            mSelected += span
            if (mSelected >= mAdapter!!.itemCount) { // 滚动末尾，判断是否循环滚动
                if (isIsCirculation) {
                    do {
                        mSelected = mSelected - mAdapter!!.itemCount
                    } while (mSelected >= mAdapter!!.itemCount) // 当越过的item数量超过一圈时
                    mMoveLength = (mMoveLength + mItemSize) % mItemSize
                } else { // 非循环滚动
                    mSelected = mAdapter!!.itemCount - 1
                    mMoveLength = -mItemSize.toFloat()
                    if (isFling) { // 停止惯性滑动，根据computeScroll()中的逻辑，下一步将调用moveToCenter()
                        mScroller.forceFinished(true)
                    }
                    if (isMovingCenter) { //  移回中间位置
                        scroll(mMoveLength, 0)
                    }
                }
            } else {
                mMoveLength = (mMoveLength + mItemSize) % mItemSize
            }
        }
    }

    // 移动到中间位置
    private fun moveToCenter() {
        if (!mScroller.isFinished || isFling || mMoveLength == 0f) {
            return
        }
        cancelScroll()

        // 向下滑动
        if (mMoveLength > 0) {
            if (mIsHorizontal) {
                if (mMoveLength < itemWidth / 2) {
                    scroll(mMoveLength, 0)
                } else {
                    scroll(mMoveLength, itemWidth)
                }
            } else {
                if (mMoveLength < itemHeight / 2) {
                    scroll(mMoveLength, 0)
                } else {
                    scroll(mMoveLength, itemHeight)
                }
            }
        } else {
            if (mIsHorizontal) {
                if (-mMoveLength < itemWidth / 2) {
                    scroll(mMoveLength, 0)
                } else {
                    scroll(mMoveLength, -itemWidth)
                }
            } else {
                if (-mMoveLength < itemHeight / 2) {
                    scroll(mMoveLength, 0)
                } else {
                    scroll(mMoveLength, -itemHeight)
                }
            }
        }
    }

    // 平滑滚动
    private fun scroll(from: Float, to: Int) {
        if (mIsHorizontal) {
            mLastScrollX = from.toInt()
            isMovingCenter = true
            mScroller.startScroll(from.toInt(), 0, 0, 0)
            mScroller.finalX = to
        } else {
            mLastScrollY = from.toInt()
            isMovingCenter = true
            mScroller.startScroll(0, from.toInt(), 0, 0)
            mScroller.finalY = to
        }
        invalidate()
    }

    // 惯性滑动，
    private fun fling(from: Float, vel: Float) {
        if (mIsHorizontal) {
            mLastScrollX = from.toInt()
            isFling = true
            // 最多可以惯性滑动10个item，这个数值越大，滑动越快
            mScroller.fling(from.toInt(), 0, vel.toInt(), 0, -10 * itemWidth, 10 * itemWidth, 0, 0)
        } else {
            mLastScrollY = from.toInt()
            isFling = true
            // 最多可以惯性滑动10个item
            mScroller.fling(
                0, from.toInt(), 0, vel.toInt(), 0, 0, -10 * itemHeight, 10 * itemHeight
            )
        }
        invalidate()
    }

    private fun notifySelected() {
        mMoveLength = 0f
        cancelScroll()
        if (listener != null) {
            // 告诉监听器选择完毕
            listener!!.onSelected(this@BasePickerView, mSelected)
        }
    }

    var isAutoScrolling = false
        private set
    private val mAutoScrollAnimator: ValueAnimator

    init {
        mGestureDetector = GestureDetector(getContext(), FlingOnGestureListener())
        mScroller = Scroller(getContext())
        mAutoScrollAnimator = ValueAnimator.ofInt(0, 0)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        init(attrs)
    }
    /**
     * 自动滚动(必须设置为可循环滚动)
     *
     * @param speed 每毫秒移动的像素点
     */
    /**
     * 自动滚动
     *
     * @see BasePickerView.autoScrollFast
     */
    @JvmOverloads
    fun autoScrollFast(
        position: Int,
        duration: Long,
        speed: Float,
        interpolator: Interpolator? = sAutoScrollInterpolator
    ) {
        if (isAutoScrolling || !isIsCirculation) {
            return
        }
        cancelScroll()
        isAutoScrolling = true
        val length = (speed * duration).toInt()
        var circle = (length * 1f / (mAdapter!!.itemCount * mItemSize) + 0.5f).toInt() // 圈数
        circle = if (circle <= 0) 1 else circle
        val aPlan = circle * mAdapter!!.itemCount * mItemSize + (mSelected - position) * mItemSize
        val bPlan = aPlan + mAdapter!!.itemCount * mItemSize // 多一圈
        // 让其尽量接近length
        val end = if (Math.abs(length - aPlan) < Math.abs(length - bPlan)) aPlan else bPlan
        mAutoScrollAnimator.cancel()
        mAutoScrollAnimator.setIntValues(0, end)
        mAutoScrollAnimator.interpolator = interpolator
        mAutoScrollAnimator.duration = duration
        mAutoScrollAnimator.removeAllUpdateListeners()
        if (end != 0) { // itemHeight为0导致endy=0
            mAutoScrollAnimator.addUpdateListener { animation ->
                var rate = 0f
                rate = animation.currentPlayTime * 1f / animation.duration
                computeScroll(animation.animatedValue as Int, end, rate)
            }
            mAutoScrollAnimator.removeAllListeners()
            mAutoScrollAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    isAutoScrolling = false
                }
            })
            mAutoScrollAnimator.start()
        } else {
            computeScroll(end, end, 1f)
            isAutoScrolling = false
        }
    }

    /**
     * 自动滚动，默认速度为 0.6dp/ms
     *
     * @see BasePickerView.autoScrollFast
     */
    fun autoScrollFast(position: Int, duration: Long) {
        val speed = dip2px(context, 0.6f).toFloat()
        autoScrollFast(position, duration, speed, sAutoScrollInterpolator)
    }

    /**
     * 滚动到指定位置
     *
     * @param toPosition 　需要滚动到的位置
     * @param duration 　滚动时间
     */
    fun autoScrollToPosition(toPosition: Int, duration: Long, interpolator: Interpolator?) {
        var toPosition = toPosition
        toPosition %= mAdapter!!.itemCount
        val endY = (mSelected - toPosition) * itemHeight
        autoScrollTo(endY, duration, interpolator, false)
    }

    /**
     * @param endY 　需要滚动到的位置
     * @param duration 　滚动时间
     * @param canIntercept 能否终止滚动，比如触摸屏幕终止滚动
     */
    fun autoScrollTo(
        endY: Int, duration: Long, interpolator: Interpolator?, canIntercept: Boolean
    ) {
        if (isAutoScrolling) {
            return
        }
        val temp = isDisallowTouch
        isDisallowTouch = !canIntercept
        isAutoScrolling = true
        mAutoScrollAnimator.cancel()
        mAutoScrollAnimator.setIntValues(0, endY)
        mAutoScrollAnimator.interpolator = interpolator
        mAutoScrollAnimator.duration = duration
        mAutoScrollAnimator.removeAllUpdateListeners()
        mAutoScrollAnimator.addUpdateListener { animation ->
            var rate = 0f
            rate = animation.currentPlayTime * 1f / animation.duration
            computeScroll(animation.animatedValue as Int, endY, rate)
        }
        mAutoScrollAnimator.removeAllListeners()
        mAutoScrollAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isAutoScrolling = false
                isDisallowTouch = temp
            }
        })
        mAutoScrollAnimator.start()
    }

    /**
     * 停止自动滚动
     */
    fun stopAutoScroll() {
        isAutoScrolling = false
        mAutoScrollAnimator.cancel()
    }

    private class SlotInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return (Math.cos((input + 1) * Math.PI) / 2.0f).toFloat() + 0.5f
        }
    }

    /**
     * 快速滑动时，惯性滑动一段距离
     *
     */
    private inner class FlingOnGestureListener : SimpleOnGestureListener() {
        private var mIsScrollingLastTime = false
        override fun onDown(e: MotionEvent): Boolean {
            if (isDisallowInterceptTouch) {  // 不允许父组件拦截事件
                val parent = parent
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            mIsScrollingLastTime = isScrolling // 记录是否从滚动状态终止
            // 点击时取消所有滚动效果
            cancelScroll()
            mLastMoveY = e.y
            mLastMoveX = e.x
            return true
        }

        override fun onFling(
            e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean {
            // 惯性滑动
            if (isInertiaScroll) {
                cancelScroll()
                if (mIsHorizontal) {
                    fling(mMoveLength, velocityX)
                } else {
                    fling(mMoveLength, velocityY)
                }
            }
            if (e2.action == MotionEvent.ACTION_UP) {
                isTouching = false
            }
            return true
        }

        /**
         * 快速点击，立刻抬起触发。这里用来
         */
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            mLastMoveY = e.y
            mLastMoveX = e.x
            var lastMove = 0f
            if (isHorizontal) {
                centerPoint = centerX
                lastMove = mLastMoveX
            } else {
                centerPoint = centerY
                lastMove = mLastMoveY
            }
            if (isCanTap && !isScrolling && !mIsScrollingLastTime) {
                if (lastMove >= centerPoint && lastMove <= centerPoint + mItemSize) { //点击中间item，回调点击事件
                    performClick()
                } else if (lastMove < centerPoint) { // 点击两边的item，移动到相应的item
                    val move = mItemSize
                    autoScrollTo(move, 150, sAutoScrollInterpolator, false)
                } else { // lastMove > mCenterPoint + mItemSize
                    val move = -mItemSize
                    autoScrollTo(move, 150, sAutoScrollInterpolator, false)
                }
            } else {
                moveToCenter()
            }
            isTouching = false
            return true
        }
    }

    var adapter: WheelAdapter<T>?
        get() = mAdapter
        /**
         * 设置数据适配器
         *
         * @param adapter adapter
         */
        set(adapter) {
            mAdapter = adapter
            mSelected = 0
            invalidate()
        }
    val selectedItem: T?
        /**
         * @return 获取选中的item
         */
        get() = mAdapter!!.getItem(mSelected)
    var selectedPosition: Int
        /**
         * @return 获取选中的item position
         */
        get() = mSelected
        /**
         * 选中position
         *
         * @param position 选中position
         */
        set(position) {
            setSelectedPosition(position, true)
        }

    /**
     * 供开发者内部使用，用户不要使用该方法。使用[.setSelectedPosition]
     *
     * @param isNotify 是否回调[.notifySelected]
     */
    fun setSelectedPosition(position: Int, isNotify: Boolean) {
        // bugfix: 这里不能判断position == mSelected，因为可能页面滑动了，但是没有点确定，一样不是目标位置
        if (position < 0 || position > mAdapter!!.itemCount - 1 /*|| position == mSelected*/) {
            return
        }
        mSelected = position
        invalidate()
        if (isNotify /* && mListener != null*/) {
            notifySelected()
        }
    }

    /**
     * 设置滑动选中监听
     *
     * @param listener 滑动选中监听
     */
    fun setOnSelectedListener(listener: OnSelectedListener?) {
        this.listener = listener
    }

    /**
     * 设置是否循环绘制，如果 adapter.getItemCount < visibleCount，则即使设置为循环，也无效
     *
     * @param isCirculation 是否循环
     */
    fun setIsCirculation(isCirculation: Boolean) {
        mIsCirculation = isCirculation
    }

    var visibleItemCount: Int
        get() = mVisibleItemCount
        /**
         * 设置可见的item count
         *
         * @param visibleItemCount 可见的item count
         */
        set(visibleItemCount) {
            mVisibleItemCount = visibleItemCount
            // 如果没有
            reset()
            invalidate()
        }
    var itemSize: Int
        /**
         * @return 当垂直滚动时，mItemSize = mItemHeight;水平滚动时，mItemSize = mItemWidth
         */
        get() = mItemSize
        /**
         * 设置item的高度/宽度
         *
         * @param itemSize dp
         */
        set(itemSize) {
            mItemSize =
                dip2px(context, (if (itemSize <= 0) sDefaultItemSize else itemSize).toFloat())
        }

    private fun setSafeCenterPosition(centerPosition: Int) {
        mUseDefaultCenterPosition = false
        mCenterPosition = if (centerPosition < 0) {
            0
        } else if (centerPosition >= mVisibleItemCount) {
            mVisibleItemCount - 1
        } else {
            centerPosition
        }
    }

    var centerPosition: Int
        /**
         * 中间item的位置,默认为 mVisibleItemCount / 2
         */
        get() = mCenterPosition
        /**
         * 中间item的位置，0 <= centerPosition <= mVisibleItemCount
         */
        set(centerPosition) {
            setSafeCenterPosition(centerPosition)
            // bugfix 这里应该调用reset
            //mCenterY = mCenterPosition * mItemHeight;
            reset()
            invalidate()
        }

    /**
     * @return 是否可选择的状态。非滑动及触摸状态
     */
    fun canSelected(): Boolean {
        return !isTouching && !isScrolling
    }

    val isScrolling: Boolean
        get() = isFling || isMovingCenter || isAutoScrolling
    var isHorizontal: Boolean
        get() = mIsHorizontal
        set(horizontal) {
            if (mIsHorizontal == horizontal) {
                return
            }
            mIsHorizontal = horizontal
            reset()
            invalidate()
        }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) {
            moveToCenter()
        }
    }

    /**
     * 绘制中心指示器
     */
    interface CenterDecoration {
        fun drawIndicator(
            pickerView: BasePickerView<*>,
            canvas: Canvas,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int
        )
    }

    interface OnSelectedListener {
        fun onSelected(pickerView: BasePickerView<*>, position: Int)
    }

    fun interface Formatter {
        fun format(
            pickerView: BasePickerView<*>, position: Int, charSequence: CharSequence?
        ): CharSequence?
    }

    companion object {
        private const val TAG = "BasePickerView"

        /** 默认可见的item个数：5个  */
        var sDefaultVisibleItemCount = 5

        /** 默认itemSize：50dp  */
        var sDefaultItemSize = 50 //dp

        /** 默认是否循环：false  */
        var sDefaultIsCirculation = false

        /** 默认值：是否绘制 CenterDecoration  */
        var sDefaultDrawIndicator = true
        const val DEFAULT_DRAW_INDICATOR_NO_DATA = true
        private val sAutoScrollInterpolator = SlotInterpolator()

        /**
         * 在一定精度内比较浮点数
         */
        fun equalsFloat(a: Double, b: Double): Boolean {
            return a == b || Math.abs(a - b) < 0.01f
        }
    }
}