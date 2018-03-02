package org.jaaksi.pickerview.demo;

import android.app.Application;
import android.graphics.Color;
import android.graphics.Rect;
import android.widget.LinearLayout;
import org.jaaksi.pickerview.picker.BasePicker;
import org.jaaksi.pickerview.topbar.ITopBar;
import org.jaaksi.pickerview.util.Util;
import org.jaaksi.pickerview.widget.DefaultCenterDecoration;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * 创建时间：2018年02月28日17:45 <br>
 * 作者：fuchaoyang <br>
 * 描述：
 */

public class MyApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    // 建议在application中初始化picker 默认属性实现全局设置
    //initDefaultPicker();
  }

  private void initDefaultPicker() {
    // 利用修改静态默认属性值，快速定制一套满足自己app样式需求的Picker.
    // BasePickerView
    PickerView.sDefaultVisibleItemCount = 3;
    PickerView.sDefaultItemSize = 50;
    PickerView.sDefaultIsCirculation = true;

    // PickerView
    PickerView.sOutTextSize = 18;
    PickerView.sCenterTextSize = 18;
    PickerView.sCenterColor = Color.RED;
    PickerView.sOutColor = Color.GRAY;

    // BasePicker
    int padding = Util.dip2px(this, 20);
    BasePicker.sDefaultPaddingRect = new Rect(padding, padding, padding, padding);
    BasePicker.sDefaultPickerBackgroundColor = Color.WHITE;
    BasePicker.sDefaultCanceledOnTouchOutside = false;
    // 自定义 TopBar
    BasePicker.sDefaultTopBarCreator = new BasePicker.IDefaultTopBarCreator() {
      @Override public ITopBar createDefaultTopBar(LinearLayout parent) {
        return new CustomTopBar(parent);
      }
    };

    // DefaultCenterDecoration
    DefaultCenterDecoration.sDefaultLineWidth = 1;
    DefaultCenterDecoration.sDefaultLineColor = Color.RED;
    //DefaultCenterDecoration.sDefaultDrawable = new ColorDrawable(Color.WHITE);
    int leftMargin = Util.dip2px(this, 10);
    int topMargin = Util.dip2px(this, 2);
    DefaultCenterDecoration.sDefaultMarginRect =
      new Rect(leftMargin, -topMargin, leftMargin, -topMargin);
  }
}
