package org.jaaksi.pickerview.picker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//import android.support.annotation.ColorInt;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;
import org.jaaksi.pickerview.dialog.DefaultPickerDialog;
import org.jaaksi.pickerview.dialog.IGlobalDialogCreator;
import org.jaaksi.pickerview.dialog.IPickerDialog;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * 创建时间：2018年01月31日18:28 <br>
 * 作者：fuchaoyang <br>
 * BasePicker中并不提供对pickerview的设置方法，而是通过接口PickerHandler转交PickerView处理
 * 三个picker的的思路有部分是不一样的，如reset调用地方，看看是不是可以优化
 */

public abstract class BasePicker {
  /** pickerView父容器的 default padding */
  public static Rect sDefaultPaddingRect;
  /** default picker background color */
  public static int sDefaultPickerBackgroundColor = Color.WHITE;
  /** Canceled dialog OnTouch Outside */
  public static boolean sDefaultCanceledOnTouchOutside = true;

  protected Context mContext;
  protected LayoutInflater mInflater;

  /** 是否启用dialog */
  protected boolean needDialog = true;
  protected IPickerDialog iPickerDialog;
  /** 用于构建全局的DefaultDialog的接口 */
  public static IGlobalDialogCreator sDefaultDialogCreator;
  protected LinearLayout mPickerContainer;
  private Interceptor mInterceptor;

  private Object mTag;
  private SparseArray<Object> mKeyedTags;
  private final List<PickerView> mPickerViews = new ArrayList<>();

  public BasePicker(Context context) {
    mContext = context;
    mInflater = LayoutInflater.from(context);
  }

  //public LinearLayout getPickerContainer() {
  //  return mPickerContainer;
  //}

  /**
   * 设置拦截器，用于用于在pickerview创建时拦截，设置pickerview的属性。Picker内部并不提供对PickerView的设置方法，
   * 而是通过Interceptor实现，实现Picker和PickerView的属性设置解耦。
   * 必须在调用 {@link #createPickerView(Object, float)}之前设置。
   * 子类应该在Builder中提供该方法。
   */
  protected void setInterceptor(Interceptor interceptor) {
    mInterceptor = interceptor;
  }

  /**
   * 设置picker背景
   *
   * @param color color
   */
  public void setPickerBackgroundColor(@ColorInt int color) {
    mPickerContainer.setBackgroundColor(color);
  }

  /**
   * 设置pickerview父容器padding 单位:px
   */
  public void setPadding(int left, int top, int right, int bottom) {
    mPickerContainer.setPadding(left, top, right, bottom);
  }

  /**
   * setTag用法同{@link View#setTag(Object)}
   *
   * @param tag tag
   */
  public void setTag(Object tag) {
    mTag = tag;
  }

  public Object getTag() {
    return mTag;
  }

  public Object getTag(int key) {
    if (mKeyedTags != null) return mKeyedTags.get(key);
    return null;
  }

  protected void initPickerView() {
    mPickerContainer = new LinearLayout(mContext);
    mPickerContainer.setOrientation(LinearLayout.HORIZONTAL);
    mPickerContainer.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
    if (sDefaultPaddingRect != null) {
      setPadding(sDefaultPaddingRect.left, sDefaultPaddingRect.top, sDefaultPaddingRect.right,
        sDefaultPaddingRect.bottom);
    }
    if (sDefaultPickerBackgroundColor != Color.TRANSPARENT) {
      setPickerBackgroundColor(sDefaultPickerBackgroundColor);
    }

    if (needDialog) { // 是否使用弹窗
      // 弹窗优先级：自定义的 > 全局的 > 默认的
      if (iPickerDialog == null) { // 如果没有自定义dialog
        if (sDefaultDialogCreator != null) { // 如果定义了全局的dialog
          iPickerDialog = sDefaultDialogCreator.create(mContext);
        } else { // 使用默认的
          iPickerDialog = new DefaultPickerDialog(mContext);
        }
      }

      if (iPickerDialog != null) {
        iPickerDialog.onCreate(this);
      }
    }
  }

  /**
   * {@link #createPickerView(Object, float)}
   *
   * @return Picker中所有的pickerview集合
   */
  public List<PickerView> getPickerViews() {
    return mPickerViews;
  }

  /**
   * 如果使用{@link #createPickerView(Object, float)}创建pickerview，就不需要手动添加
   *
   * @param pickerView pickerView
   */
  protected void addPicker(PickerView pickerView) {
    mPickerViews.add(pickerView);
  }

  /**
   * 创建pickerview
   *
   * @param tag settag
   * @param weight 权重
   */
  protected PickerView createPickerView(Object tag, float weight) {
    PickerView pickerView = new PickerView(mContext);
    pickerView.setTag(tag);
    // 这里是竖直方向的，如果要设置横向的，则自己再设置LayoutParams
    LinearLayout.LayoutParams params =
      new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
    params.weight = weight;
    // do it
    if (mInterceptor != null) {
      mInterceptor.intercept(pickerView, params);
    }
    pickerView.setLayoutParams(params);
    mPickerContainer.addView(pickerView);
    addPicker(pickerView);
    return pickerView;
  }

  /**
   * 通过tag找到对应的pickerview
   *
   * @param tag tag
   * @return 对应tag的pickerview，找不到返回null
   */
  @Nullable
  public PickerView findPickerViewByTag(@NonNull Object tag) {
    for (PickerView pickerView : mPickerViews) {
      if (checkIsSamePickerView(tag, pickerView.getTag())) return pickerView;
    }
    return null;
  }

  /**
   * 通过两个tag判断是否是同一个pickerview
   */
  protected boolean checkIsSamePickerView(@NonNull Object tag, Object pickerViewTag) {
    return tag.equals(pickerViewTag);
  }

  /**
   * 是否滚动未停止
   */
  public boolean canSelected() {
    for (int i = mPickerViews.size() - 1; i >= 0; i--) {
      PickerView pickerView = mPickerViews.get(i);
      if (!pickerView.canSelected()) {
        return false;
      }
    }
    return true;
  }

  /**
   * setTag 用法同{@link View#setTag(int, Object)}
   *
   * @param key key R.id.xxx
   * @param tag tag
   */
  public void setTag(int key, final Object tag) {
    // If the package id is 0x00 or 0x01, it's either an undefined package
    // or a framework id
    if ((key >>> 24) < 2) {
      throw new IllegalArgumentException(
        "The key must be an application-specific " + "resource id.");
    }

    setKeyedTag(key, tag);
  }

  private void setKeyedTag(int key, Object tag) {
    if (mKeyedTags == null) {
      mKeyedTags = new SparseArray<Object>(2);
    }

    mKeyedTags.put(key, tag);
  }

  /**
   * @return 获取IPickerDialog
   */
  public IPickerDialog dialog() {
    return iPickerDialog;
  }

  /**
   * @return 获取picker的view，用于非弹窗情况
   */
  public LinearLayout view() {
    return mPickerContainer;
  }

  /**
   * 显示picker弹窗
   */
  public void show() {
    if (/*!needDialog || */iPickerDialog == null) return;
    iPickerDialog.showDialog();
  }

  /**
   * 点击确定按钮的回调
   */
  public abstract void onConfirm();

  /**
   * 用于子类修改设置PickerView属性
   */
  public interface Interceptor {
    /**
     * 拦截pickerview的创建，我们可以自定义
     *
     * @param pickerView 增加layoutparams参数，方便设置weight
     */
    void intercept(PickerView pickerView, LinearLayout.LayoutParams params);
  }
}
