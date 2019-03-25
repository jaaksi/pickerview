package org.jaaksi.pickerview.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by fuchaoyang on 2018/2/10.<br/>
 * description：
 */

public class MainActivity extends BaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.btn_timepicker:
        openFragment(new TimePickerFragment());
        break;
      case R.id.btn_mixedtimepicker:
        openFragment(new MixedTimeFormatFragment());
        break;
      case R.id.btn_optionpicker:
        openFragment(new OptionPickerFragment());
        break;
      case R.id.btn_test_pickerview:
        openFragment(new TestPickerViewFragment());
        break;
      case R.id.btn_picker_configs:
        openFragment(new PickerConfigFragment());
        break;
    }
  }
}
