package org.jaaksi.pickerview.dialog;

public interface OnPickerChooseListener {

  /**
   * @return 是否回调选中关闭dialog
   */
  boolean onConfirm();

  void onCancel();
}