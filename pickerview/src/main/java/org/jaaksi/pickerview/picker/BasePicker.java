package org.jaaksi.pickerview.picker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;
import org.jaaksi.pickerview.R;
import org.jaaksi.pickerview.topbar.DefaultTopBar;
import org.jaaksi.pickerview.topbar.ITopBar;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * 创建时间：2018年01月31日18:28 <br>
 * 作者：fuchaoyang <br>
 * BasePicker中并不提供对pickerview的设置方法，而是通过接口PickerHandler转交PickerView处理
 * 三个picker的的思路有部分是不一样的，如reset调用地方，看看是不是可以优化
 */

public abstract class BasePicker implements View.OnClickListener {
  public static int sDefaultPickerBackgroundColor = Color.WHITE;

  protected Context mContext;
  protected LayoutInflater mInflater;

  private Dialog mPickerDialog;
  private LinearLayout mRootLayout;
  public static ITopBar DEFAULT_TOPBAR_HANDLER = null;
  // topbar的设置，title，确定按钮等都通过这个控制，picker自身不处理
  private ITopBar mITopBar;
  protected OnPickerChooseListener mPickerChooseListener;
  // 构造方法中就会初始化
  protected LinearLayout mPickerContainer;
  private Interceptor mInterceptor;

  private Object mTag;
  private SparseArray<Object> mKeyedTags;
  private final List<PickerView> mPickerViews = new ArrayList<>();

  public BasePicker(Context context) {
    mContext = context;
    mInflater = LayoutInflater.from(context);
    initPickerDialog();
  }

  public BasePicker setPickerChooseListener(OnPickerChooseListener pickerChooseListener) {
    mPickerChooseListener = pickerChooseListener;
    return this;
  }

  /**
   * 自定义TopBar，parent写{@link #getRootLayout()}
   */
  public void setTopBar(ITopBar ITopBar) {
    mITopBar = ITopBar;
    mRootLayout.removeViewAt(0);
    addTopBar();
  }

  public ITopBar getTopBar() {
    return mITopBar;
  }

  public LinearLayout getRootLayout() {
    return mRootLayout;
  }

  /**
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

  private void initPickerDialog() {
    mRootLayout = new LinearLayout(mContext);
    mRootLayout.setOrientation(LinearLayout.VERTICAL);
    mRootLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

    //if (mITopBar == null) {
    if (DEFAULT_TOPBAR_HANDLER != null) {
      mITopBar = DEFAULT_TOPBAR_HANDLER;
    } else {
      mITopBar = new DefaultTopBar(mRootLayout);
    }
    //}
    addTopBar();

    mPickerContainer = new LinearLayout(mContext);
    mPickerContainer.setOrientation(LinearLayout.HORIZONTAL);
    mPickerContainer.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
    if (sDefaultPickerBackgroundColor != Color.TRANSPARENT) {
      mPickerContainer.setBackgroundColor(sDefaultPickerBackgroundColor);
    }
    mRootLayout.addView(mPickerContainer);

    mPickerDialog = new Dialog(mContext, R.style.dialog_pickerview);
    Window window = mPickerDialog.getWindow();
    if (window != null) {
      window.setWindowAnimations(R.style.picker_dialog_anim);
      // 默认是match_parent的
      window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT);
      window.setGravity(Gravity.BOTTOM);
    }

    mPickerDialog.setCanceledOnTouchOutside(false);
    mPickerDialog.setContentView(mRootLayout);
  }

  private void addTopBar() {
    mRootLayout.addView(mITopBar.getTopBarView(), 0);
    mITopBar.getBtnCancel().setOnClickListener(this);
    mITopBar.getBtnConfirm().setOnClickListener(this);
  }

  /**
   * {@link #createPickerView(Object, float)}
   *
   * @return 调用
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
    pickerView.setLayoutParams(params);
    if (mInterceptor != null) {
      mInterceptor.intercept(pickerView);
    }
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
  @Nullable public PickerView findPickerViewByTag(@NonNull Object tag) {
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
  public boolean isScrolling() {
    for (int i = mPickerViews.size() - 1; i >= 0; i--) {
      PickerView pickerView = mPickerViews.get(i);
      if (pickerView.isScrolling()) {
        return true;
      }
    }
    return false;
  }

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
   * 可以在new之后设置dialog属性
   */
  public Dialog getPickerDialog() {
    return mPickerDialog;
  }

  public void show() {
    mPickerDialog.show();
  }

  protected <T extends View> T findViewById(int id) {
    return mPickerDialog.findViewById(id);
  }

  @CallSuper @Override public void onClick(View v) {
    if (isScrolling()) return;//  滑动未停止不响应点击事件
    int id = v.getId();
    if (id == R.id.btn_confirm) {
      // 给用户拦截
      if (mPickerChooseListener == null || mPickerChooseListener.onConfirm()) {
        onConfirm();
        mPickerDialog.dismiss();
      }
    } else if (id == R.id.btn_cancel) {
      onCancel();
      if (mPickerChooseListener != null) {
        mPickerChooseListener.onCancel();
      }
    }
  }

  protected abstract void onConfirm();

  protected void onCancel() {
    mPickerDialog.dismiss();
  }

  /**
   * 用于子类修改设置PickerView属性
   */
  public interface Interceptor {
    void intercept(PickerView pickerView);
  }

  public interface OnPickerChooseListener {

    /**
     * @return 是否回调选中关闭dialog
     */
    boolean onConfirm();

    void onCancel();
  }
}
