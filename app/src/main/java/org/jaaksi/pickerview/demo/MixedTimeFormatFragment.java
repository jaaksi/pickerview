package org.jaaksi.pickerview.demo;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.jaaksi.pickerview.picker.MixedTimePicker;
import org.jaaksi.pickerview.util.DateUtil;

/**
 * 创建时间：2018年01月31日15:49 <br>
 * 作者：fuchaoyang <br>
 * 描述：自定义日期格式 如显示星期几
 */

public class MixedTimeFormatFragment extends BaseFragment
  implements View.OnClickListener, MixedTimePicker.OnTimeSelectListener {
  private Button mBtnShow;
  private MixedTimePicker mTimePicker;
  public static final DateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
  public static final SimpleDateFormat mDateFormat =
    new SimpleDateFormat("MM月dd日  E", Locale.CHINA);

  @Override protected int getLayoutId() {
    return R.layout.fragment_mixedtime_format;
  }

  @Override protected void initView(View view) {
    mBtnShow = view.findViewById(R.id.btn_show);
    mBtnShow.setOnClickListener(this);
    mTimePicker = new MixedTimePicker.Builder(mActivity, MixedTimePicker.TYPE_ALL, this)
      // 设置不包含超出的结束时间<=
      .setContainsEndDate(false)
      // 设置时间间隔为30分钟
      .setTimeMinuteOffset(30)
      .setRangDate(1517771651000L, 1577976666000L)
      .setFormatter(new MixedTimePicker.DefaultFormatter() {
        @Override
        public CharSequence format(MixedTimePicker picker, int type, Date date, int position) {
          if (type == MixedTimePicker.TYPE_DATE) {
            CharSequence text;
            int dayOffset = DateUtil.getDayOffset(date.getTime(), System.currentTimeMillis());
            if (dayOffset == 0) {
              text = "今天";
            } else if (dayOffset == 1) {
              text = "明天";
            } else { // xx月xx日 星期 x
              text = mDateFormat.format(date);
            }
            return text;
          }
          return super.format(picker, type, date, position);
        }
      })
      .create();
    // 2018/2/5 03:14:11 - 2020/1/2 22:51:6
    Dialog pickerDialog = mTimePicker.getPickerDialog();
    pickerDialog.setCanceledOnTouchOutside(true);
    mTimePicker.getTopBar().getTitleView().setText("请选择时间");
  }

  @Override public void onTimeSelect(MixedTimePicker picker, Date date) {
    mBtnShow.setText(sSimpleDateFormat.format(date));
  }

  @Override public void onClick(View v) {
    try {
      Date date = sSimpleDateFormat.parse(mBtnShow.getText().toString());
      mTimePicker.setSelectedDate(date.getTime());
    } catch (ParseException e) {
      mTimePicker.setSelectedDate(System.currentTimeMillis());
      e.printStackTrace();
    }
    mTimePicker.show();
  }
}
