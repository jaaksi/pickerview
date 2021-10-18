package org.jaaksi.pickerview.dialog;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
//import android.support.annotation.CallSuper;
//import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.jaaksi.pickerview.R;
import org.jaaksi.pickerview.picker.BasePicker;

/**
 * picker.dialog(IPickerDialog接口)自定义Dialog。提供DefaultPickerDialog，支持全局设定
 */
public class DefaultPickerDialog extends Dialog implements IPickerDialog, View.OnClickListener {
  /** Canceled dialog OnTouch Outside */
  public static boolean sDefaultCanceledOnTouchOutside = true;

  private BasePicker picker;
  protected OnPickerChooseListener mOnPickerChooseListener;
  private TextView mBtnCancel, mBtnConfirm, mTvTitle;

  public DefaultPickerDialog(@NonNull Context context) {
    super(context, R.style.dialog_pickerview);
  }

  public void setOnPickerChooseListener(OnPickerChooseListener onPickerChooseListener) {
    this.mOnPickerChooseListener = onPickerChooseListener;
  }

  /**
   * 先于onCreate(Bundle savedInstanceState)执行
   */
  @Override
  public void onCreate(BasePicker picker) {
    this.picker = picker;
    //初始化Dialog,将pickerContainer添加到dialog跟布局中
    LinearLayout pickerContainer = picker.view();
    Context context = pickerContainer.getContext();

    /** Dialog View */
    LinearLayout rootLayout =
      (LinearLayout) LayoutInflater.from(context)
        .inflate(R.layout.dialog_pickerview_default, null);
    mBtnCancel = rootLayout.findViewById(R.id.btn_cancel);
    mBtnConfirm = rootLayout.findViewById(R.id.btn_confirm);
    mTvTitle = rootLayout.findViewById(R.id.tv_title);

    mBtnCancel.setOnClickListener(this);
    mBtnConfirm.setOnClickListener(this);
    rootLayout.addView(picker.view());

    setCanceledOnTouchOutside(sDefaultCanceledOnTouchOutside);
    setContentView(rootLayout);
    // 不能在onCreate中设置，onCreate回调的晚，会覆盖外面设置的属性导致失效
    Window window = getWindow();
    if (window != null) {
      window.setWindowAnimations(R.style.picker_dialog_anim);
      // 要在setContentView之后调用
      // 要在onCreate里设置，否则如果style设置了windowIsFloating=true，会变成-2，-2？
      window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT);
      window.setGravity(Gravity.BOTTOM);
    }
  }

  @Override
  public void showDialog() {
    show();
  }

  @CallSuper
  @Override
  public void onClick(View v) {
    if (!picker.canSelected()) return;//  滑动未停止不响应点击事件
    if (v == getBtnConfirm()) {
      // 给用户拦截
      if (mOnPickerChooseListener == null || mOnPickerChooseListener.onConfirm()) {
        // 抛给picker去处理
        dismiss();
        picker.onConfirm();
      }
    } else if (v == getBtnCancel()) {
      dismiss();
      if (mOnPickerChooseListener != null) {
        mOnPickerChooseListener.onCancel();
      }
    }
  }

  public View getBtnCancel() {
    return mBtnCancel;
  }

  public View getBtnConfirm() {
    return mBtnConfirm;
  }

  public TextView getTitleView() {
    return mTvTitle;
  }
}
