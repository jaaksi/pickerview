package org.jaaksi.pickerview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import org.jaaksi.pickerview.R
import org.jaaksi.pickerview.dataset.PickerDataSet
import org.jaaksi.pickerview.util.Util.computeGradientColor
import org.jaaksi.pickerview.util.Util.dip2px

/**
 * 字符串滚动选择器
 * https://github.com/1993hzw/Androids/blob/master/androids/src/cn/forward/androids/views/StringScrollPicker.java
 * 做以下修改：
 * 1.数据不仅仅支持String，支持任意数据。方便设置数据
 * 2.绘制文字不使用StaticLayout，只绘制一行，且居中
 *
 * 其实View不关心泛型，需要关心的是Adapter，但是我们的adapter是通用的，并不需要用户再定义，所以无法指明泛型，所以不得以view这里依然用泛型。
 * 这里为了兼容支持直接设置String,int数据
 *
 * @see PickerDataSet 如果是自定义数据类型，请实现该接口
 */
class PickerView<T> @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BasePickerView<T>(context, attrs, defStyleAttr) {
    private val mPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var outTextSize = 0 // 最小的字体
    private var centerTextSize = 0 // 最大的字体

    // 字体渐变颜色
    private var centerColor = sCenterColor // 中间选中item的颜色
    private var outColor = sOutColor // 上下两边的颜色

    /**
     * 设置对其方式
     *
     * @param alignment 对齐方式
     */
    var alignment = Layout.Alignment.ALIGN_CENTER // 对齐方式,默认居中
    private var mShadowColors: IntArray? = sShadowColors

    // Shadows drawables
    private var mStartShadow: GradientDrawable? = null
    private var mEndShadow: GradientDrawable? = null

    init {
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.BLACK
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PickerView)
            outTextSize =
                typedArray.getDimensionPixelSize(R.styleable.PickerView_pv_out_text_size, 0)
            centerTextSize =
                typedArray.getDimensionPixelSize(R.styleable.PickerView_pv_center_text_size, 0)
            centerColor = typedArray.getColor(R.styleable.PickerView_pv_start_color, centerColor)
            outColor = typedArray.getColor(R.styleable.PickerView_pv_end_color, outColor)
            val align = typedArray.getInt(R.styleable.PickerView_pv_alignment, 1)
            if (align == 2) {
                alignment = Layout.Alignment.ALIGN_NORMAL
            } else if (align == 3) {
                alignment = Layout.Alignment.ALIGN_OPPOSITE
            } else {
                alignment = Layout.Alignment.ALIGN_CENTER
            }
            typedArray.recycle()
        }
        if (outTextSize <= 0) {
            outTextSize = dip2px(context, sOutTextSize.toFloat())
        }
        if (centerTextSize <= 0) {
            centerTextSize = dip2px(context, sCenterTextSize.toFloat())
        }
        resetShadow()
    }

    private fun resetShadow() {
        if (mShadowColors == null) {
            mStartShadow = null
            mEndShadow = null
        } else {
            if (isHorizontal) {
                mStartShadow =
                    GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mShadowColors)
                mEndShadow =
                    GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mShadowColors)
            } else {
                mStartShadow =
                    GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, mShadowColors)
                mEndShadow =
                    GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, mShadowColors)
            }
        }
    }

    /**
     * 设置蒙版
     */
    fun setShadowsColors(@ColorInt colors: IntArray?) {
        mShadowColors = colors
        resetShadow()
    }

    /**
     * 设置center out 文字 color
     *
     * @param centerColor 正中间的颜色
     * @param outColor 上下两边的颜色
     */
    fun setColor(@ColorInt centerColor: Int, @ColorInt outColor: Int) {
        this.centerColor = centerColor
        this.outColor = outColor
        invalidate()
    }

    /**
     * 设置item文字大小，单位dp
     *
     * @param minText 沒有被选中时的最小文字
     * @param maxText 被选中时的最大文字
     */
    fun setTextSize(minText: Int, maxText: Int) {
        outTextSize = dip2px(context, minText.toFloat())
        centerTextSize = dip2px(context, maxText.toFloat())
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mShadowColors != null) {
            drawShadows(canvas)
        }
    }

    override fun drawItem(
        canvas: Canvas?, data: T?, position: Int, relative: Int, moveLength: Float,
        top: Float
    ) {
        //  添加一层装饰器
        var text: CharSequence? = if (data is PickerDataSet) {
            (data as PickerDataSet).getCharSequence()
        } else {
            data.toString()
        }
        text = if (formatter == null) text else formatter!!.format(this, position, text)
        if (text == null) return
        val itemSize = itemSize

        // 设置文字大小
        if (relative == -1) { // 上一个
            if (moveLength < 0) { // 向上滑动
                mPaint.textSize = outTextSize.toFloat()
            } else { // 向下滑动
                mPaint.textSize =
                    outTextSize + (centerTextSize - outTextSize) * moveLength / itemSize
            }
        } else if (relative == 0) { // 中间item,当前选中
            mPaint.textSize = (outTextSize
                    + (centerTextSize - outTextSize) * (itemSize - Math.abs(moveLength)) / itemSize)
        } else if (relative == 1) { // 下一个
            if (moveLength > 0) { // 向下滑动
                mPaint.textSize = outTextSize.toFloat()
            } else { // 向上滑动
                mPaint.textSize =
                    outTextSize + (centerTextSize - outTextSize) * -moveLength / itemSize
            }
        } else { // 其他
            mPaint.textSize = outTextSize.toFloat()
        }

        // 不换行
        val layout = StaticLayout(
            text, 0, text.length, mPaint, dip2px(
                context, 1000f
            ), alignment,
            1.0f, 0.0f, true, null, 0
        )
        var x = 0f
        var y = 0f
        val lineWidth = layout.width.toFloat()
        if (isHorizontal) { // 水平滚动
            x = top + (itemWidth - lineWidth) / 2
            y = ((itemHeight - layout.height) / 2).toFloat()
        } else { // 垂直滚动
            x = (itemWidth - lineWidth) / 2
            y = top + (itemHeight - layout.height) / 2
        }
        // 计算渐变颜色
        computeColor(relative, itemSize, moveLength)
        canvas!!.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }

    /**
     * Draws shadows on top and bottom of control
     */
    private fun drawShadows(canvas: Canvas) {
        val height = itemHeight
        mStartShadow!!.setBounds(0, 0, width, height)
        mStartShadow!!.draw(canvas)
        mEndShadow!!.setBounds(0, getHeight() - height, width, getHeight())
        mEndShadow!!.draw(canvas)
    }

    /**
     * 计算字体颜色，渐变
     * 1.中间区域为 centerColor，其他未 outColor 参考AndroidPickers
     * 2.如果再当前位置松开手后，应该选中的那个item的文字颜色为centerColor,其他为outColor
     * 把这个做成接口，提供默认实现
     *
     * @param relative 　相对中间item的位置
     */
    private fun computeColor(relative: Int, itemSize: Int, moveLength: Float) {
        var color = outColor // 　其他默认为 mOutColor
        if (relative == -1 || relative == 1) { // 上一个或下一个
            // 处理上一个item且向上滑动　或者　处理下一个item且向下滑动　，颜色为 mOutColor
            color = if (relative == -1 && moveLength < 0 || relative == 1 && moveLength > 0) {
                outColor
            } else { // 计算渐变的颜色
                val rate = (itemSize - Math.abs(moveLength)) / itemSize
                computeGradientColor(centerColor, outColor, rate)
            }
        } else if (relative == 0) { // 中间item
            val rate = Math.abs(moveLength) / itemSize
            color = computeGradientColor(centerColor, outColor, rate)
        }
        mPaint.color = color
    }

    companion object {
        /** default out text size 18dp  */
        var sOutTextSize = 18 // dp

        /** default center text size 20dp  */
        var sCenterTextSize = 20 // dp

        /** default center text color  */
        var sCenterColor = "#41bc6a".toColorInt()

        /** default out text color  */
        var sOutColor = "#666666".toColorInt()

        /** Top and bottom shadows colors  */
        var sShadowColors =
            intArrayOf(Color.WHITE, "#88ffffff".toColorInt(), "#00FFFFFF".toColorInt())
    }
}