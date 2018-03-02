package org.jaaksi.pickerview.demo;

import android.view.View;
import android.widget.Button;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jaaksi.pickerview.dataset.OptionDataSet;
import org.jaaksi.pickerview.demo.model.City;
import org.jaaksi.pickerview.demo.model.County;
import org.jaaksi.pickerview.demo.model.Province;
import org.jaaksi.pickerview.picker.OptionPicker;

/**
 * 演示topbar,CenterDecoration,padding，interceptor,
 */
public class OptionPickerFragment extends BaseFragment
  implements View.OnClickListener, OptionPicker.OnOptionSelectListener {

  private OptionPicker mPicker;
  private Button mBtnShow;
  // 2-3-县4
  private String provinceId /*= "200"*/, cityId /*= "230"*/, countyId/* = "234"*/;

  @Override protected int getLayoutId() {
    return R.layout.fragment_option_picker;
  }

  @Override protected void initView(View view) {
    mBtnShow = view.findViewById(R.id.btn_show);
    mBtnShow.setOnClickListener(this);
    mPicker = new OptionPicker.Builder(mActivity, 3, this).create();
    // 设置 Formatter
    /*mPicker.setFormatter(new OptionPicker.Formatter() {
      @Override public CharSequence format(OptionPicker picker, int level, int position,
        CharSequence charSequence) {
        if (level == 0) {
          charSequence = charSequence + "省";
        } else if (level == 1) {
          charSequence = charSequence + "市";
        } else if (level == 2) {
          charSequence = charSequence + "县";
        }
        return charSequence;
      }
    });*/
    // 设置标题，这里调用getTopBar来设置标题
    //DefaultTopBar topBar = (DefaultTopBar) mPicker.getTopBar();
    mPicker.getTopBar().getTitleView().setText("请选择城市");
    List<Province> data = createData();
    mPicker.setDataWithValues(data);
    //mPicker.setDataWithValues(data, provinceId, cityId, countyId);
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
    // 直接传入选中的值
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
