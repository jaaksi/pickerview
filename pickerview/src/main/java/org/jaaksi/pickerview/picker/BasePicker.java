package org.jaaksi.pickerview.picker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
  /** pickerView父容器的 default padding */
  public static Rect sDefaultPaddingRect;
  /** default picker background color */
  public static int sDefaultPickerBackgroundColor = Color.WHITE;
  /** Canceled dialog OnTouch Outside */
  public static boolean sDefaultCanceledOnTouchOutside = true;

  protected Context mContext;
  protected LayoutInflater mInflater;

  private Dialog mPickerDialog;
  private LinearLayout mRootLayout;
  /** 用于构建defaultTopBar的接口 */
  public static IDefaultTopBarCreator sDefaultTopBarCreator;
  // topbar的设置，title，确定按钮等都通过这个控制，picker自身不处理
  private ITopBar mITopBar;
  protected OnPickerChooseListener mOnPickerChooseListener;
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

  /**
   * 设置picker取消，确定按钮监听。可用于拦截选中操作。
   *
   * @param onPickerChooseListener listener
   */
  public BasePicker setOnPickerChooseListener(OnPickerChooseListener onPickerChooseListener) {
    mOnPickerChooseListener = onPickerChooseListener;
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

  /**
   * 获取TopBar
   */
  public ITopBar getTopBar() {
    return mITopBar;
  }

  /**
   * 获取pickerview的父容器，创建{@link DefaultTopBar#DefaultTopBar(ViewGroup)}时必须指定parent
   *
   * @return pickerview的父容器
   */
  public LinearLayout getRootLayout() {
    return mRootLayout;
  }

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

  private void initPickerDialog() {
    mRootLayout = new LinearLayout(mContext);
    mRootLayout.setOrientation(LinearLayout.VERTICAL);
    mRootLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

    if (sDefaultTopBarCreator != null) {
      // 这里采用静态接口，避免静态持有view造成泄漏以及可以传递parent
      mITopBar = sDefaultTopBarCreator.createDefaultTopBar(mRootLayout);
    } else {
      mITopBar = new DefaultTopBar(mRootLayout);
    }
    addTopBar();

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
    mRootLayout.addView(mPickerContainer);

    mPickerDialog = new Dialog(mContext, R.style.dialog_pickerview) {
      // 要在onCreate里设置，否则如果style设置了windowIsFloating=true，会变成-2，-2
      @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = mPickerDialog.getWindow();
        if (window != null) {
          window.setWindowAnimations(R.style.picker_dialog_anim);
          // 默认是match_parent的
          window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
              WindowManager.LayoutParams.WRAP_CONTENT);
          window.setGravity(Gravity.BOTTOM);
        }
      }
    };

    mPickerDialog.setCanceledOnTouchOutside(sDefaultCanceledOnTouchOutside);
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
   * 获取Picker弹窗。可以在new之后设置dialog属性
   */
  public Dialog getPickerDialog() {
    return mPickerDialog;
  }

  /**
   * 显示picker弹窗
   */
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
      if (mOnPickerChooseListener == null || mOnPickerChooseListener.onConfirm()) {
        onConfirm();
        mPickerDialog.dismiss();
      }
    } else if (id == R.id.btn_cancel) {
      onCancel();
      if (mOnPickerChooseListener != null) {
        mOnPickerChooseListener.onCancel();
      }
    }
  }

  /**
   * 点击确定按钮的回调
   */
  protected abstract void onConfirm();

  public void onCancel() {
    mPickerDialog.dismiss();
  }

  public interface IDefaultTopBarCreator {
    /**
     * 创建defaulttopbar
     *
     * @param parent parent
     * @return defaultTopBar
     */
    ITopBar createDefaultTopBar(LinearLayout parent);
  }

  /**
   * 用于子类修改设置PickerView属性
   */
  public interface Interceptor {
    /**
     * 拦截
     *
     * @param pickerView pickerView
     */
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
