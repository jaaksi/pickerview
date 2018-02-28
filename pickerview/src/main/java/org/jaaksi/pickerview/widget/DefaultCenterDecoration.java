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
 * 样式：背景图，上下两条线，支持设置线条颜色，宽度
 */

public class DefaultCenterDecoration implements BasePickerView.CenterDecoration {
  public static int sDEFAULT_LINE_COLOR = Color.BLUE;
  public static float sDEFAULT_LINE_WIDTH = 1;
  public static Drawable sDEFAULT_DRAWABLE;

  private Context mContext;
  private Paint mPaint;

  private Drawable mDrawable;
  private Rect mRect = new Rect();

  public DefaultCenterDecoration(Context context) {
    mContext = context;
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setStyle(Paint.Style.FILL);

    setLineWidth(sDEFAULT_LINE_WIDTH);
    setLineColor(sDEFAULT_LINE_COLOR);
    setDrawable(sDEFAULT_DRAWABLE);
  }

  /**
   * 设置linecolor
   *
   * @param lineColor line color 如果设置为Color.TRANSPARENT就不绘制线
   */
  public DefaultCenterDecoration setLineColor(int lineColor) {
    mPaint.setColor(lineColor);
    return this;
  }

  /**
   * 设置装饰线宽度
   *
   * @param lineWidth 装饰线宽度
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

  @Override
  public void drawIndicator(BasePickerView pickerView, Canvas canvas, int left, int top, int right,
    int bottom) {
    if (mDrawable != null) {
      mRect.set(left, top, right, bottom);
      mDrawable.setBounds(mRect);
      mDrawable.draw(canvas);
    }

    if (mPaint.getColor() == Color.TRANSPARENT) return;
    if (pickerView.isVertical()) {
      canvas.drawLine(left, top, right, top, mPaint);
      canvas.drawLine(left, bottom, right, bottom, mPaint);
    } else {
      canvas.drawLine(left, top, left, bottom, mPaint);
      canvas.drawLine(right, top, right, bottom, mPaint);
    }
  }
}
