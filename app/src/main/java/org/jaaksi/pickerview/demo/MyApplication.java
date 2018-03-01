package org.jaaksi.pickerview.demo;

import android.app.Application;
import android.graphics.Color;
import org.jaaksi.pickerview.picker.BasePicker;
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
    initDefaultPicker();
  }

  private void initDefaultPicker() {
    // PickerView
    //PickerView.sCenterColor = Color.BLUE;
    //PickerView.sOutColor = Color.RED;
    PickerView.sMinTextSize = 18;
    PickerView.sMaxTextSize = 22;
    //PickerView.sDefaultItemSize = 50;

    // BasePicker
    BasePicker.sDefaultPickerBackgroundColor = Color.WHITE;
    // picker padding
    //int padding = Util.dip2px(this, 20);
    //BasePicker.sPaddingRect = new Rect(padding, padding, padding, padding);

    // DefaultCenterDecoration
    DefaultCenterDecoration.sDefaultLineWidth = 1;
    //DefaultCenterDecoration.sDefaultLineColor = Color.RED;

  }
}
