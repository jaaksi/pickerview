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
  /** default line color */
  public static int sDefaultLineColor = Color.BLUE;
  /** default line width */
  public static float sDefaultLineWidth = 1;
  /** default item background drawable */
  public static Drawable sDefaultDrawable;
  /** default line margin */
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
   * 水平方向认为。left=topmargin,top为rightmargin,right=botommargin,botom=leftmargin
   */
  public DefaultCenterDecoration setMargin(int left, int top, int right, int bottom) {
    mMarginRect = new Rect(left, top, right, bottom);
    return this;
  }

  /**
   * 设置装饰线的margin
   * 水平方向认为。left=topmargin,top为rightmargin,right=botommargin,botom=leftmargin
   */
  public DefaultCenterDecoration setMargin(Rect marginRect) {
    mMarginRect = marginRect;
    return this;
  }

  @Override
  public void drawIndicator(BasePickerView pickerView, Canvas canvas, int left, int top, int right,
    int bottom) {
    if (mMarginRect == null) {
      mMarginRect = new Rect();
    }
    boolean isVertical = pickerView.isVertical();
    if (mDrawable != null) {
      if (isVertical) {
        mRect.set(left + mMarginRect.left,
          top + mMarginRect.top + (int) (mPaint.getStrokeWidth() / 2), right - mMarginRect.right,
          bottom - mMarginRect.bottom - (int) (mPaint.getStrokeWidth() / 2));
      } else {
        mRect.set(left + mMarginRect.top + (int) (mPaint.getStrokeWidth() / 2),
          top + mMarginRect.right, right - mMarginRect.bottom - (int) (mPaint.getStrokeWidth() / 2),
          bottom - mMarginRect.left);
      }
      mDrawable.setBounds(mRect);
      mDrawable.draw(canvas);
    }

    if (mPaint.getColor() == Color.TRANSPARENT) return;

    if (isVertical) {
      canvas.drawLine(left + mMarginRect.left, top + mMarginRect.top, right - mMarginRect.right,
        top + mMarginRect.top, mPaint);
      canvas.drawLine(left + mMarginRect.left, bottom - mMarginRect.bottom,
        right - mMarginRect.right, bottom - mMarginRect.bottom, mPaint);
    } else {
      // 水平方向认为。left=topmargin,top为rightmargin,right=botommargin,botom=leftmargin
      canvas.drawLine(left + mMarginRect.top, top + mMarginRect.right, left + mMarginRect.top,
        bottom - mMarginRect.left, mPaint);
      canvas.drawLine(right - mMarginRect.bottom, top + mMarginRect.right,
        right - mMarginRect.bottom, bottom - mMarginRect.left, mPaint);
    }
  }
}
