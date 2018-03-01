package org.jaaksi.pickerview.demo;

import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import org.jaaksi.pickerview.adapter.NumericWheelAdapter;
import org.jaaksi.pickerview.util.Util;
import org.jaaksi.pickerview.widget.BasePickerView;
import org.jaaksi.pickerview.widget.DefaultCenterDecoration;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * Created by fuchaoyang on 2018/2/5.<br/>
 * description：
 */

public class TestPickerViewFragment extends BaseFragment implements BasePickerView.Formatter {
  PickerView<Integer> mPickerView;

  @Override protected int getLayoutId() {
    return R.layout.fragment_test_picker;
  }

  @Override protected void initView(View view) {
    mPickerView = view.findViewById(R.id.pickerview);
    mPickerView.setAdapter(new NumericWheelAdapter(1, 10));
    // 覆盖xml中的水平方向
    mPickerView.setHorizontal(false);
    mPickerView.setTextSize(15, 22);
    mPickerView.setIsCirculation(true);
    //mPickerView.setAlignment(Layout.Alignment.ALIGN_CENTER);
    mPickerView.setCanTap(false);
    mPickerView.setDisallowInterceptTouch(false);
    // 覆盖xml设置的7
    mPickerView.setVisibleItemCount(5);
    mPickerView.setFormatter(this);
    //mPickerView.setItemSize(60);
    DefaultCenterDecoration centerDecoration = new DefaultCenterDecoration(getActivity());
    centerDecoration.setLineColor(Color.GREEN);
    int margin = Util.dip2px(mActivity, 2);
    centerDecoration.setMargin(new Rect(0, -margin, 0, -margin));
    centerDecoration.setLineWidth(1);
    //centerDecoration.setDrawable(Color.RED);
    mPickerView.setCenterDecoration(centerDecoration);
    //mPickerView.setSelectedPosition(1);
    // 设置centerPosition
    mPickerView.postDelayed(new Runnable() {
      @Override public void run() {
        mPickerView.setCenterPosition(1);
      }
    }, 1000);
  }

  @Override
  public CharSequence handle(BasePickerView pickerView, int position, CharSequence charSequence) {
    return charSequence + "万年";
  }
}
