package org.jaaksi.pickerview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import org.jaaksi.pickerview.R;
import org.jaaksi.pickerview.dataset.PickerDataSet;
import org.jaaksi.pickerview.util.Util;

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
public class PickerView<T> extends BasePickerView<T> {

  private TextPaint mPaint; //
  /** default out text size 18dp */
  public static int sOutTextSize = 18; // dp
  /** default center text size 22dp */
  public static int sCenterTextSize = 22; // dp
  private int mOutTextSize; // 最小的字体
  private int mCenterTextSize; // 最大的字体
  /** default center text color */
  public static int sCenterColor = Color.BLUE;
  /** default out text color */
  public static int sOutColor = Color.GRAY;
  // 字体渐变颜色
  private int mCenterColor = sCenterColor; // 中间选中item的颜色
  private int mOutColor = sOutColor; // 上下两边的颜色
  private Layout.Alignment mAlignment = Layout.Alignment.ALIGN_CENTER; // 对齐方式,默认居中

  public PickerView(Context context) {
    this(context, null);
  }

  public PickerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setStyle(Paint.Style.FILL);
    mPaint.setColor(Color.BLACK);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    if (attrs != null) {
      TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PickerView);
      mOutTextSize = typedArray.getDimensionPixelSize(R.styleable.PickerView_pv_out_text_size, 0);
      mCenterTextSize =
        typedArray.getDimensionPixelSize(R.styleable.PickerView_pv_center_text_size, 0);
      mCenterColor = typedArray.getColor(R.styleable.PickerView_pv_start_color, mCenterColor);
      mOutColor = typedArray.getColor(R.styleable.PickerView_pv_end_color, mOutColor);
      int align = typedArray.getInt(R.styleable.PickerView_pv_alignment, 1);
      if (align == 2) {
        mAlignment = Layout.Alignment.ALIGN_NORMAL;
      } else if (align == 3) {
        mAlignment = Layout.Alignment.ALIGN_OPPOSITE;
      } else {
        mAlignment = Layout.Alignment.ALIGN_CENTER;
      }
      typedArray.recycle();
    }
    if (mOutTextSize <= 0) {
      mOutTextSize = Util.dip2px(getContext(), sOutTextSize);
    }
    if (mCenterTextSize <= 0) {
      mCenterTextSize = Util.dip2px(getContext(), sCenterTextSize);
    }
  }

  /**
   * 设置center out 文字 color
   *
   * @param centerColor 正中间的颜色
   * @param outColor 上下两边的颜色
   */
  public void setColor(@ColorInt int centerColor, @ColorInt int outColor) {
    mCenterColor = centerColor;
    mOutColor = outColor;
    invalidate();
  }

  /**
   * 设置item文字大小，单位dp
   *
   * @param minText 沒有被选中时的最小文字
   * @param maxText 被选中时的最大文字
   */
  public void setTextSize(int minText, int maxText) {
    mOutTextSize = Util.dip2px(getContext(), minText);
    mCenterTextSize = Util.dip2px(getContext(), maxText);
    invalidate();
  }

  public int getCenterColor() {
    return mCenterColor;
  }

  public int getOutColor() {
    return mOutColor;
  }

  public int getOutTextSize() {
    return mOutTextSize;
  }

  public int getCenterTextSize() {
    return mCenterTextSize;
  }

  /**
   * 设置对其方式
   *
   * @param alignment 对齐方式
   */
  public void setAlignment(Layout.Alignment alignment) {
    mAlignment = alignment;
  }

  public Layout.Alignment getAlignment() {
    return mAlignment;
  }

  @Override
  public void drawItem(Canvas canvas, T data, int position, int relative, float moveLength,
    float top) {
    //  添加一层装饰器
    CharSequence text;
    if (data instanceof PickerDataSet) {
      text = ((PickerDataSet) data).getCharSequence();
    } else {
      text = data.toString();
    }
    text = getFormatter() == null ? text : getFormatter().format(this, position, text);
    if (text == null) return;
    int itemSize = getItemSize();

    // 设置文字大小
    if (relative == -1) { // 上一个
      if (moveLength < 0) { // 向上滑动
        mPaint.setTextSize(mOutTextSize);
      } else { // 向下滑动
        mPaint.setTextSize(mOutTextSize + (mCenterTextSize - mOutTextSize) * moveLength / itemSize);
      }
    } else if (relative == 0) { // 中间item,当前选中
      mPaint.setTextSize(mOutTextSize
        + (mCenterTextSize - mOutTextSize) * (itemSize - Math.abs(moveLength)) / itemSize);
    } else if (relative == 1) { // 下一个
      if (moveLength > 0) { // 向下滑动
        mPaint.setTextSize(mOutTextSize);
      } else { // 向上滑动
        mPaint.setTextSize(
          mOutTextSize + (mCenterTextSize - mOutTextSize) * -moveLength / itemSize);
      }
    } else { // 其他
      mPaint.setTextSize(mOutTextSize);
    }

    // 不换行
    StaticLayout layout =
      new StaticLayout(text, 0, text.length(), mPaint, Util.dip2px(getContext(), 1000), mAlignment,
        1.0F, 0.0F, true, null, 0);
    float x = 0;
    float y = 0;
    float lineWidth = layout.getWidth();

    if (isHorizontal()) { // 水平滚动
      x = top + (getItemWidth() - lineWidth) / 2;
      y = (getItemHeight() - layout.getHeight()) / 2;
    } else { // 垂直滚动
      x = (getItemWidth() - lineWidth) / 2;
      y = top + (getItemHeight() - layout.getHeight()) / 2;
    }
    // 计算渐变颜色
    computeColor(relative, itemSize, moveLength);

    canvas.save();
    canvas.translate(x, y);
    layout.draw(canvas);
    canvas.restore();
  }

  /**
   * 计算字体颜色，渐变
   * 1.中间区域为 centerColor，其他未 outColor 参考AndroidPickers
   * 2.如果再当前位置松开手后，应该选中的那个item的文字颜色为centerColor,其他为outColor
   * 把这个做成接口，提供默认实现
   *
   * @param relative 　相对中间item的位置
   */
  private void computeColor(int relative, int itemSize, float moveLength) {
    int color = mOutColor; // 　其他默认为 mOutColor

    if (relative == -1 || relative == 1) { // 上一个或下一个
      // 处理上一个item且向上滑动　或者　处理下一个item且向下滑动　，颜色为 mOutColor
      if ((relative == -1 && moveLength < 0) || (relative == 1 && moveLength > 0)) {
        color = mOutColor;
      } else { // 计算渐变的颜色
        float rate = (itemSize - Math.abs(moveLength)) / itemSize;
        color = Util.computeGradientColor(mCenterColor, mOutColor, rate);
      }
    } else if (relative == 0) { // 中间item
      float rate = Math.abs(moveLength) / itemSize;
      color = Util.computeGradientColor(mCenterColor, mOutColor, rate);
    }

    mPaint.setColor(color);
  }
}
