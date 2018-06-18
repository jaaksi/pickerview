package org.jaaksi.pickerview.demo;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.jaaksi.pickerview.picker.BasePicker;
import org.jaaksi.pickerview.picker.TimePicker;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * 创建时间：2018年01月31日15:49 <br>
 * 作者：fuchaoyang <br>
 * 描述：强大的type模式自由组合（当然应该是有意义的）
 */

public class TimePickerFragment extends BaseFragment
  implements View.OnClickListener, TimePicker.OnTimeSelectListener {
  private Button mBtnShow;
  private CheckBox mCbYear, mCbMonth, mCbDay, mCbHour, mCbMinute;
  private TimePicker mTimePicker;
  public static final DateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private int mCurrYear;
  private long mLoveTimes;

  @Override protected int getLayoutId() {
    return R.layout.fragment_timepicker;
  }

  @Override protected void initView(View view) {
    Calendar calendar = Calendar.getInstance();
    mCurrYear = calendar.get(Calendar.YEAR);
    mBtnShow = view.findViewById(R.id.btn_show);
    mBtnShow.setOnClickListener(this);
    view.findViewById(R.id.btn_choose_type).setOnClickListener(this);
    mCbYear = view.findViewById(R.id.cb_year);
    mCbMonth = view.findViewById(R.id.cb_month);
    mCbDay = view.findViewById(R.id.cb_day);
    mCbHour = view.findViewById(R.id.cb_hour);
    mCbMinute = view.findViewById(R.id.cb_minute);
    Calendar love = Calendar.getInstance();
    love.set(love.get(Calendar.YEAR), 4, 20, 13, 14);
    mLoveTimes = love.getTimeInMillis();
  }

  @Override public void onTimeSelect(TimePicker picker, Date date) {
    mBtnShow.setText(sSimpleDateFormat.format(date));
  }

  private void reset() {
    int type = 0;
    // 设置type
    if (mCbYear.isChecked()) type = type | TimePicker.TYPE_YEAR;
    if (mCbMonth.isChecked()) type = type | TimePicker.TYPE_MONTH;
    if (mCbDay.isChecked()) type = type | TimePicker.TYPE_DAY;
    if (mCbHour.isChecked()) type = type | TimePicker.TYPE_HOUR;
    if (mCbMinute.isChecked()) type = type | TimePicker.TYPE_MINUTE;
    // 2018/5/15 13:14:00 - 2030/1/2 13:51:0
    mTimePicker = new TimePicker.Builder(mActivity, type, this)
      // 设置时间区间
      .setRangDate(1526361240000L, 1893563460000L)
      .setTimeMinuteOffset(10)
      // 设置选中时间
      //.setSelectedDate()
      // 设置pickerview样式
      .setInterceptor(new BasePicker.Interceptor() {
        @Override public void intercept(PickerView pickerView) {
          pickerView.setVisibleItemCount(3);
          // 将年月设置为循环的
          int type = (int) pickerView.getTag();
          if (type == TimePicker.TYPE_YEAR || type == TimePicker.TYPE_MONTH) {
            pickerView.setIsCirculation(true);
          }
        }
      })
      // 设置 Formatter
      .setFormatter(new TimePicker.DefaultFormatter() {
        // 自定义Formatter显示去年，今年，明年
        @Override public CharSequence format(TimePicker picker, int type, int position, int num) {
          if (type == TimePicker.TYPE_YEAR) {
            int offset = num - mCurrYear;
            if (offset == -1) return "去年";
            if (offset == 0) return "今年";
            if (offset == 1) return "明年";
            return num + "年";
          } else if (type == TimePicker.TYPE_MONTH) {
            return String.format("%d月", num);
          }

          return super.format(picker, type, position, num);
        }
      }).create();
    // 2019/2/5 14:57:23
    //mTimePicker.setSelectedDate(1549349843000L);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_choose_type:
        reset();
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
          mTimePicker.setSelectedDate(mLoveTimes);
        }
        mTimePicker.show();
        break;
    }
  }
}
