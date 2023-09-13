package org.jaaksi.pickerview.util

import android.content.Context
import android.graphics.Color

object Util {
    /**
     * dip转换px
     *
     * @param context 上下文
     * @param dpValue dip值
     * @return px值
     */
    @JvmStatic
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 计算渐变后的颜色
     *
     * @param startColor 开始颜色
     * @param endColor 结束颜色
     * @param rate 渐变率（0,1）
     * @return 渐变后的颜色，当rate=0时，返回startColor，当rate=1时返回endColor
     */
    @JvmStatic
    fun computeGradientColor(startColor: Int, endColor: Int, rate: Float): Int {
        var rate = rate
        if (rate < 0) {
            rate = 0f
        }
        if (rate > 1) {
            rate = 1f
        }
        val alpha = Color.alpha(endColor) - Color.alpha(startColor)
        val red = Color.red(endColor) - Color.red(startColor)
        val green = Color.green(endColor) - Color.green(startColor)
        val blue = Color.blue(endColor) - Color.blue(startColor)
        return Color.argb(
            Math.round(Color.alpha(startColor) + alpha * rate),
            Math.round(Color.red(startColor) + red * rate),
            Math.round(Color.green(startColor) + green * rate),
            Math.round(Color.blue(startColor) + blue * rate)
        )
    }
}