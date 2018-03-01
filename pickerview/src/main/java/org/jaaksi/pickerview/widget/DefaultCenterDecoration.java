package org.jaaksi.pickerview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import org.jaaksi.pickerview.util.Util;

/**
 * 创建时间：2018年02月17日10:55 <br>
 * 作者：fuchaoyang <br>
 * 描述：default centerdecoration
 * 样式：背景图，上下两条线，支持设置线条颜色，宽度，margin
 */

public class DefaultCenterDecoration implements BasePickerView.CenterDecoration {
  public static int sDefaultLineColor = Color.BLUE;
  public static float sDefaultLineWidth = 1;
  public static Drawable sDefaultDrawable;
  public static Rect sDefaultMarginRect;

  private Context mContext;
  private Paint mPaint;

  private Drawable mDrawable;
  private Rect mMarginRect;
  private Rect mRect = new Rect();

  public DefaultCenterDecoration(Context context) {
    mContext = context;
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setStyle(Paint.Style.FILL);

    setLineWidth(sDefaultLineWidth);
    setLineColor(sDefaultLineColor);
    setDrawable(sDefaultDrawable);
    setMargin(sDefaultMarginRect);
  }

  /**
   * 设置linecolor
   *
   * @param lineColor line color 如果设置为Color.TRANSPARENT就不绘制线
   */
  public DefaultCenterDecoration setLineColor(@ColorInt int lineColor) {
    mPaint.setColor(lineColor);
    return this;
  }

  /**
   * 设置装饰线宽度
   *
   * @param lineWidth 装饰线宽度 单位dp
   */
  public DefaultCenterDecoration setLineWidth(float lineWidth) {
    mPaint.setStrokeWidth(Util.dip2px(mContext, lineWidth));
    return this;
  }

  /**
   * 设置CenterDecoration drawable
   */
  public DefaultCenterDecoration setDrawable(Drawable drawable) {
    mDrawable = drawable;
    return this;
  }

  public DefaultCenterDecoration setDrawable(@ColorInt int color) {
    mDrawable = new ColorDrawable(color);
    return this;
  }

  /**
   * 设置装饰线的margin 单位px
   */
  public DefaultCenterDecoration setMargin(int left, int top, int right, int bottom) {
    mMarginRect = new Rect(left, top, right, bottom);
    return this;
  }

  /**
   * 设置装饰线的margin
   */
  public DefaultCenterDecoration setMargin(Rect marginRect) {
    mMarginRect = marginRect;
    return this;
  }

  @Override
  public void drawIndicator(BasePickerView pickerView, Canvas canvas, int left, int top, int right,
    int bottom) {
    if (mDrawable != null) {
      mRect.set(left, top, right, bottom);
      mDrawable.setBounds(mRect);
      mDrawable.draw(canvas);
    }

    if (mPaint.getColor() == Color.TRANSPARENT) return;

    if (mMarginRect == null) {
      mMarginRect = new Rect();
    }
    if (pickerView.isVertical()) {
      canvas.drawLine(left + mMarginRect.left, top + mMarginRect.top, right - mMarginRect.right,
        top + mMarginRect.top, mPaint);
      canvas.drawLine(left + mMarginRect.left, bottom - mMarginRect.bottom,
        right - mMarginRect.right, bottom - mMarginRect.bottom, mPaint);
    } else {
      // 水平方向认为left为topmargin,top为leftmargin
      canvas.drawLine(left + mMarginRect.top, top + mMarginRect.left, left + mMarginRect.top,
        bottom - mMarginRect.right, mPaint);
      canvas.drawLine(right - mMarginRect.bottom, top + mMarginRect.left,
        right - mMarginRect.bottom, bottom - mMarginRect.right, mPaint);
    }
  }
}
