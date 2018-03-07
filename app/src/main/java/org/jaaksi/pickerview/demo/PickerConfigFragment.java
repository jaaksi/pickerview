package org.jaaksi.pickerview.demo;

import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jaaksi.pickerview.dataset.OptionDataSet;
import org.jaaksi.pickerview.demo.model.City;
import org.jaaksi.pickerview.demo.model.County;
import org.jaaksi.pickerview.demo.model.Province;
import org.jaaksi.pickerview.picker.BasePicker;
import org.jaaksi.pickerview.picker.OptionPicker;
import org.jaaksi.pickerview.util.Util;
import org.jaaksi.pickerview.widget.DefaultCenterDecoration;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * 演示topbar,CenterDecoration,interceptor,
 */
public class PickerConfigFragment extends BaseFragment
  implements View.OnClickListener, OptionPicker.OnOptionSelectListener {

  private OptionPicker mPicker;
  private Button mBtnShow;
  // 2-3-县4
  private String provinceId = "200", cityId = "230", countyId = "234";

  @Override protected int getLayoutId() {
    return R.layout.fragment_picker_configs;
  }

  @Override protected void initView(View view) {
    mBtnShow = view.findViewById(R.id.btn_show);
    mBtnShow.setOnClickListener(this);

    // 设置CenterDecoration
    final DefaultCenterDecoration decoration = new DefaultCenterDecoration(mActivity);
    decoration.setLineColor(Color.RED)
      //.setDrawable(Color.parseColor("#999999"))
      .setLineWidth(1)
      .setMargin(Util.dip2px(mActivity, 10), Util.dip2px(mActivity, -3), Util.dip2px(mActivity, 10),
        Util.dip2px(mActivity, -3));

    mPicker =
      new OptionPicker.Builder(mActivity, 3, this).setInterceptor(new BasePicker.Interceptor() {
        @Override public void intercept(PickerView pickerView) {
          int level = (int) pickerView.getTag();
          pickerView.setVisibleItemCount(3);
          // setInterceptor 可以根据level区分设置pickerview属性
          pickerView.setCenterDecoration(decoration);
          pickerView.setTextSize(15, 20);
        }
      }).create();
    // 设置padding
    int padding = Util.dip2px(mActivity, 20);
    mPicker.setPadding(0, padding, 0, padding);
    //mPicker.setPickerBackgroundColor(Color.parseColor("#eeeeee"));

    // 设置弹窗
    Dialog dialog = mPicker.getPickerDialog();
    dialog.setCanceledOnTouchOutside(true);
    //dialog.getWindow().setGravity(Gravity.BOTTOM);

    // 自定义topbar
    CustomTopBar topBar = new CustomTopBar(mPicker.getRootLayout());
    topBar.getTitleView().setText("请选择城市");
    topBar.setDividerHeight(1).setDividerColor(Color.parseColor("#eeeeee"));
    mPicker.setTopBar(topBar);

    // 拦截缺点按钮的事件
    mPicker.setOnPickerChooseListener(new BasePicker.OnPickerChooseListener() {
      @Override public boolean onConfirm() {
        Toast.makeText(mActivity, "拦截确定按钮", 1).show();
        //mPicker.onCancel();
        // 返回false表示拦截
        return false;
      }

      @Override public void onCancel() {
        Toast.makeText(mActivity, "取消", 1).show();
      }
    });

    List<Province> data = createData();
    mPicker.setDataWithValues(data);
  }

  private List<Province> createData() {
    List<Province> list = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Province province = new Province();
      province.id = 100 * i;
      province.name = "省" + i;
      province.citys = new ArrayList<>();
      for (int j = 0; j < (i == 1 ? 1 : 10); j++) {
        City city = new City();
        city.id = province.id + 10 * j;
        city.name = i + "-市" + j;
        city.counties = new ArrayList<>();
        for (int k = 0; k < (i == 0 && j == 0 ? 0 : 10); k++) {
          County county = new County();
          county.id = city.id + k;
          county.name = i + "-" + j + "-县" + k;
          city.counties.add(county);
        }
        province.citys.add(city);
      }
      list.add(province);
    }
    return list;
  }

  @Override public void onClick(View v) {
    mPicker.setSelectedWithValues(provinceId, cityId, countyId);
    mPicker.show();
  }

  @Override public void onOptionSelect(OptionPicker picker, int[] selectedPosition,
    OptionDataSet[] selectedOptions) {
    System.out.println("selectedPosition = " + Arrays.toString(selectedPosition));
    String text;
    Province province = (Province) selectedOptions[0];
    provinceId = province.getValue();
    City city = (City) selectedOptions[1];
    County county = (County) selectedOptions[2];
    if (city == null) {
      cityId = null;
      countyId = null;
      text = province.name;
    } else {
      cityId = city.getValue();
      if (county == null) {
        countyId = null;
        text = city.name;
      } else {
        countyId = county.getValue();
        text = county.name;
      }
    }

    mBtnShow.setText(text);
  }
}
