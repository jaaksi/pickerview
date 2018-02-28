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
    //PickerView.sCENTER_COLOR = Color.BLUE;
    //PickerView.sOUT_COLOR = Color.RED;
    PickerView.sMIN_TEXTSIZE = 18;
    PickerView.sMAX_TEXTSIZE = 22;
    //PickerView.sDEFAULT_ITEM_SIZE = 50;

    // BasePicker
    BasePicker.sDefaultPickerBackgroundColor = Color.WHITE;

    // DefaultCenterDecoration
    DefaultCenterDecoration.sDEFAULT_LINE_WIDTH = 1;
    //DefaultCenterDecoration.sDEFAULT_LINE_COLOR = Color.RED;

  }
}
