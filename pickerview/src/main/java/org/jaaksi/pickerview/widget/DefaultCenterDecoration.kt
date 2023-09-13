package org.jaaksi.pickerview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import org.jaaksi.pickerview.util.Util.dip2px
import org.jaaksi.pickerview.widget.BasePickerView.CenterDecoration

/**
 * 创建时间：2018年02月17日10:55 <br></br>
 * 作者：fuchaoyang <br></br>
 * 描述：default centerdecoration
 * 样式：背景图，上下两条线，支持设置线条颜色，宽度，margin
 */
class DefaultCenterDecoration(private val mContext: Context) : CenterDecoration {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mDrawable: Drawable? = null
    private var marginRect: Rect? = null
    private val mRect = Rect()

    init {
        paint.style = Paint.Style.FILL
        setLineWidth(sDefaultLineWidth)
        setLineColor(sDefaultLineColor)
        setDrawable(sDefaultDrawable)
        setMargin(sDefaultMarginRect)
    }

    /**
     * 设置linecolor
     *
     * @param lineColor line color 如果设置为Color.TRANSPARENT就不绘制线
     */
    fun setLineColor(@ColorInt lineColor: Int): DefaultCenterDecoration {
        paint.color = lineColor
        return this
    }

    /**
     * 设置装饰线宽度
     *
     * @param lineWidth 装饰线宽度 单位dp
     */
    fun setLineWidth(lineWidth: Float): DefaultCenterDecoration {
        paint.strokeWidth = dip2px(mContext, lineWidth).toFloat()
        return this
    }

    /**
     * 设置CenterDecoration drawable
     */
    fun setDrawable(drawable: Drawable?): DefaultCenterDecoration {
        mDrawable = drawable
        return this
    }

    fun setDrawable(@ColorInt color: Int): DefaultCenterDecoration {
        mDrawable = ColorDrawable(color)
        return this
    }

    /**
     * 设置装饰线的margin 单位px
     * 水平方向认为。left=topmargin,top为rightmargin,right=botommargin,botom=leftmargin
     */
    fun setMargin(left: Int, top: Int, right: Int, bottom: Int): DefaultCenterDecoration {
        marginRect = Rect(left, top, right, bottom)
        return this
    }

    /**
     * 设置装饰线的margin
     * 水平方向认为。left=topmargin,top为rightmargin,right=botommargin,botom=leftmargin
     */
    fun setMargin(marginRect: Rect?): DefaultCenterDecoration {
        this.marginRect = marginRect
        return this
    }

    override fun drawIndicator(
        pickerView: BasePickerView<*>,
        canvas: Canvas,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        if (marginRect == null) {
            marginRect = Rect()
        }
        val isHorizontal = pickerView.isHorizontal
        if (mDrawable != null) {
            if (!isHorizontal) {
                mRect[left + marginRect!!.left, top + marginRect!!.top + (paint.strokeWidth / 2).toInt(), right - marginRect!!.right] =
                    bottom - marginRect!!.bottom - (paint.strokeWidth / 2).toInt()
            } else {
                mRect[left + marginRect!!.top + (paint.strokeWidth / 2).toInt(), top + marginRect!!.right, right - marginRect!!.bottom - (paint.strokeWidth / 2).toInt()] =
                    bottom - marginRect!!.left
            }
            mDrawable!!.bounds = mRect
            mDrawable!!.draw(canvas)
        }
        if (paint.color == Color.TRANSPARENT) return
        if (!isHorizontal) {
            canvas.drawLine(
                (left + marginRect!!.left).toFloat(),
                (top + marginRect!!.top).toFloat(),
                (right - marginRect!!.right).toFloat(),
                (top + marginRect!!.top).toFloat(),
                paint
            )
            canvas.drawLine(
                (left + marginRect!!.left).toFloat(),
                (bottom - marginRect!!.bottom).toFloat(),
                (right - marginRect!!.right).toFloat(),
                (bottom - marginRect!!.bottom).toFloat(),
                paint
            )
        } else {
            // 水平方向认为。left=topmargin,top为rightmargin,right=botommargin,botom=leftmargin
            canvas.drawLine(
                (left + marginRect!!.top).toFloat(),
                (top + marginRect!!.right).toFloat(),
                (left + marginRect!!.top).toFloat(),
                (bottom - marginRect!!.left).toFloat(),
                paint
            )
            canvas.drawLine(
                (right - marginRect!!.bottom).toFloat(),
                (top + marginRect!!.right).toFloat(),
                (right - marginRect!!.bottom).toFloat(),
                (bottom - marginRect!!.left).toFloat(),
                paint
            )
        }
    }

    companion object {
        /** default line color  */
        var sDefaultLineColor = "#ECECEE".toColorInt()

        /** default line width  */
        var sDefaultLineWidth = 1f

        /** default item background drawable  */
        var sDefaultDrawable: Drawable? = null

        /** default line margin  */
        var sDefaultMarginRect: Rect? = null
    }
}