package org.jaaksi.pickerview.demo;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jaaksi.pickerview.picker.MixedTimePicker;

/**
 * 创建时间：2018年02月27日17:57 <br>
 * 作者：fuchaoyang <br>
 */

public class MixedTimePickerFragment extends BaseFragment
  implements View.OnClickListener, MixedTimePicker.OnTimeSelectListener {
  private Button mBtnShow, mBtnFormat;
  private CheckBox mCbDate, mCbTime;
  private MixedTimePicker mTimePicker;
  public static final DateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Override protected int getLayoutId() {
    return R.layout.fragment_mixedtimepicker;
  }

  @Override protected void initView(View view) {
    mBtnShow = view.findViewById(R.id.btn_show);
    mBtnFormat = view.findViewById(R.id.btn_format);
    mBtnShow.setOnClickListener(this);
    mBtnFormat.setOnClickListener(this);
    view.findViewById(R.id.btn_choose_type).setOnClickListener(this);
    mCbDate = view.findViewById(R.id.cb_date);
    mCbTime = view.findViewById(R.id.cb_time);
  }

  private void reset() {
    int type = 0;
    // 设置type
    if (mCbDate.isChecked()) type = type | MixedTimePicker.TYPE_DATE;
    if (mCbTime.isChecked()) type = type | MixedTimePicker.TYPE_TIME;
    // 2018/5/15 13:14:00 - 2030/1/2 13:51:0
    mTimePicker = new MixedTimePicker.Builder(mActivity, type, this)
      // 设置 Formatter
      //.setFormatter(this)
      //.setTimeMinuteOffset(30)
      // 设置时间区间 2018/2/5 3:14:0 - 2020/1/2 22:51:0
      .setRangDate(1517771640000L, 1577976660000L)
      // 设置选中时间
      //.setSelectedDate()
      // 设置pickerview样式
      //.setInterceptor()
      .create();
    // 2018/2/28 3:14:00
    //mTimePicker.setSelectedDate(1519758840000L);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_choose_type:
        reset();
        break;
      case R.id.btn_format:
        openFragment(new MixedTimeFormatFragment());
        break;
      case R.id.btn_show:
        if (mTimePicker == null) {
          Toast.makeText(mActivity, "请先选择type", 1).show();
          return;
        }

        try {
          // 设置选中时间
          Date date = sSimpleDateFormat.parse(mBtnShow.getText().toString());
          mTimePicker.setSelectedDate(date.getTime());
        } catch (ParseException e) {
          // 如果没有设置选中时间，则取起始时间
          e.printStackTrace();
        }
        mTimePicker.show();
        break;
    }
  }

  @Override public void onTimeSelect(MixedTimePicker picker, Date date) {
    mBtnShow.setText(sSimpleDateFormat.format(date));
  }
}
