package org.jaaksi.pickerview.picker;

import android.content.Context;
import java.util.Calendar;
import java.util.Date;
import org.jaaksi.pickerview.adapter.NumericWheelAdapter;
import org.jaaksi.pickerview.util.DateUtil;
import org.jaaksi.pickerview.widget.BasePickerView;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * 创建时间：2018年01月31日15:51 <br>
 * 作者：fuchaoyang <br>
 * 描述：时间选择器
 * 强大点：
 * 1.type的设计，自由组合
 * 2.支持时间区间设置以及选中联动
 */

public class TimePicker extends BasePicker
  implements BasePickerView.OnSelectedListener, BasePickerView.Formatter {

  public static final int TYPE_YEAR = 0x01;
  public static final int TYPE_MONTH = 0x02;
  public static final int TYPE_DAY = 0x04;
  public static final int TYPE_HOUR = 0x08;
  public static final int TYPE_MINUTE = 0x10;

  // 日期：年月日
  public static final int TYPE_DATE = TYPE_YEAR | TYPE_MONTH | TYPE_DAY;
  // 时间：小时、分钟
  public static final int TYPE_TIME = TYPE_HOUR | TYPE_MINUTE;
  // 全部
  public static final int TYPE_ALL = TYPE_DATE | TYPE_TIME;

  private int mType = TYPE_DATE;

  private PickerView<Integer> mYearPicker, mMonthPicker, mDayPicker, mHourPicker, mMinutePicker;

  // 初始设置选中的时间，如果不设置为startDate
  private Calendar mSelectedDate;
  private Calendar mStartDate;//开始时间
  //private Calendar mStartDate = Calendar.getInstance();//开始时间
  private Calendar mEndDate;//终止时间

  private int mStartYear;
  private int mEndYear;
  private int mStartMonth;
  private int mEndMonth;
  private int mStartDay;
  private int mEndDay;
  private int mStartHour;
  private int mEndHour;
  private int mStartMinute;
  private int mEndMinute;

  // 时间分钟间隔
  private int mTimeMinuteOffset;
  // 设置offset时，是否包含起止时间
  private boolean mContainsStarDate;
  private boolean mContainsEndDate;

  private Formatter mFormatter;
  private OnTimeSelectListener mOnTimeSelectListener;

  private TimePicker(Context context, int type, OnTimeSelectListener listener) {
    super(context);
    mType = type;
    mOnTimeSelectListener = listener;
  }

  public void setFormatter(Formatter formatter) {
    mFormatter = formatter;
  }

  private void setRangDate(long startDate, long endDate) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(startDate);
    this.mStartDate = calendar;
    calendar = Calendar.getInstance();
    calendar.setTimeInMillis(endDate);
    this.mEndDate = calendar;
  }

  /**
   * 设置选中的时间，如果不设置默认为起始时间，应该伴随着show方法使用
   *
   * @param millis 选中的时间戳 单位ms
   */
  public void setSelectedDate(long millis) {
    updateSelectedDate(millis);
    reset();
  }

  private void updateSelectedDate(long millis) {
    if (mSelectedDate == null) {
      mSelectedDate = Calendar.getInstance();
    }
    mSelectedDate.setTimeInMillis(millis);
  }

  private Date getSelectedDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(mSelectedDate.getTime());
    if (hasType(TYPE_YEAR)) {
      calendar.set(Calendar.YEAR, mYearPicker.getSelectedItem());
    }
    if (hasType(TYPE_MONTH)) {
      calendar.set(Calendar.MONTH, mMonthPicker.getSelectedItem() - 1);
    }
    if (hasType(TYPE_DAY)) {
      calendar.set(Calendar.DAY_OF_MONTH, mDayPicker.getSelectedItem());
    }
    if (hasType(TYPE_HOUR)) {
      calendar.set(Calendar.HOUR_OF_DAY, mHourPicker.getSelectedItem());
    }
    if (hasType(TYPE_MINUTE)) {
      calendar.set(Calendar.MINUTE, getRealMinute(mMinutePicker.getSelectedItem()));
    }
    // 选中时间取设置的时间，没有的，不做任何更改
    //calendar.set(Calendar.SECOND, 0);
    //calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  public int getType() {
    return mType;
  }

  /**
   * @param type type
   * @return 是否包含类型 type
   */
  public boolean hasType(int type) {
    return (mType & type) == type;
  }

  /**
   * createPickerView在init中执行，那么{@link #setInterceptor(Interceptor)}就必须在构造该方法之前执行才有效。采用Builder
   */
  @SuppressWarnings("unchecked") private void initPicker() {
    if (hasType(TYPE_YEAR)) {
      mYearPicker = createPickerView(TYPE_YEAR, 1.2f);
      mYearPicker.setOnSelectedListener(this);
      mYearPicker.setFormatter(this);
    }
    if (hasType(TYPE_MONTH)) {
      mMonthPicker = createPickerView(TYPE_MONTH, 1);
      mMonthPicker.setOnSelectedListener(this);
      mMonthPicker.setFormatter(this);
    }
    if (hasType(TYPE_DAY)) {
      mDayPicker = createPickerView(TYPE_DAY, 1);
      mDayPicker.setOnSelectedListener(this);
      mDayPicker.setFormatter(this);
    }
    if (hasType(TYPE_HOUR)) {
      mHourPicker = createPickerView(TYPE_HOUR, 1);
      mHourPicker.setOnSelectedListener(this);
      mHourPicker.setFormatter(this);
    }
    if (hasType(TYPE_MINUTE)) {
      mMinutePicker = createPickerView(TYPE_MINUTE, 1);
      mMinutePicker.setFormatter(this);
    }
  }

  private void handleData() {
    if (mSelectedDate == null
      || mSelectedDate.getTimeInMillis() < mStartDate.getTimeInMillis()
      || mSelectedDate.getTimeInMillis() > mEndDate.getTimeInMillis()) {
      updateSelectedDate(mStartDate.getTimeInMillis());
    }
    if (mTimeMinuteOffset < 1) {
      mTimeMinuteOffset = 1;
    }
    mStartYear = mStartDate.get(Calendar.YEAR);
    mEndYear = mEndDate.get(Calendar.YEAR);
    mStartMonth = mStartDate.get(Calendar.MONTH) + 1;
    mEndMonth = mEndDate.get(Calendar.MONTH) + 1;
    mStartDay = mStartDate.get(Calendar.DAY_OF_MONTH);
    mEndDay = mEndDate.get(Calendar.DAY_OF_MONTH);
    mStartHour = mStartDate.get(Calendar.HOUR_OF_DAY);
    mEndHour = mEndDate.get(Calendar.HOUR_OF_DAY);
    mStartMinute = getValidTimeMinutes(mStartDate.get(Calendar.MINUTE), true);
    mEndMinute = getValidTimeMinutes(mEndDate.get(Calendar.MINUTE), false);
  }

  private void reset() {
    handleData();
    // 处理数据，根据当前选中的时间及设置的日期范围处理数据
    if (hasType(TYPE_YEAR)) {
      if (mYearPicker.getAdapter() == null) { // 年不会发生变化，不需要重复设置
        mYearPicker.setAdapter(
          new NumericWheelAdapter(mStartDate.get(Calendar.YEAR), mEndDate.get(Calendar.YEAR)));
      }
      mYearPicker
        .setSelectedPosition(mSelectedDate.get(Calendar.YEAR) - mYearPicker.getAdapter().getItem(0),
          false);
    }

    resetMonthAdapter(true);
  }

  private void resetMonthAdapter(boolean isInit) {
    // 1.根据当前选中的年份，以及起止时间，设置对应的月份。然后再设置对应的日
    if (hasType(TYPE_MONTH)) {
      int year =
        hasType(TYPE_YEAR) ? mYearPicker.getSelectedItem() : mSelectedDate.get(Calendar.YEAR);
      int start, end;
      // 这里要计算 selectedItem 而不是selectedPosition
      int last = isInit ? mSelectedDate.get(Calendar.MONTH) + 1 : mMonthPicker.getSelectedItem();
      start = year == mStartYear ? mStartMonth : 1;
      end = year == mEndYear ? mEndMonth : 12;

      mMonthPicker.setAdapter(new NumericWheelAdapter(start, end));
      // 2.设置选中的月份
      mMonthPicker.setSelectedPosition(last - mMonthPicker.getAdapter().getItem(0), false);
    }

    // 3.月份要联动日
    resetDayAdapter(isInit);
  }

  private void resetDayAdapter(boolean isInit) {
    if (hasType(TYPE_DAY)) {
      int year =
        hasType(TYPE_YEAR) ? mYearPicker.getSelectedItem() : mSelectedDate.get(Calendar.YEAR);
      // 3.根据当前选中的年月设置日期。联动同月份。如果和起始或截止时同一年月，则比较对应日期
      // 有年，有日，则强制认为有月。
      int month = hasType(TYPE_MONTH) ? mMonthPicker.getSelectedItem()
        : mSelectedDate.get(Calendar.MONTH) + 1;
      int last = isInit ? mSelectedDate.get(Calendar.DAY_OF_MONTH) : mDayPicker.getSelectedItem();
      int start = year == mStartYear && month == mStartMonth ? mStartDay : 1;
      int end =
        year == mEndYear && month == mEndMonth ? mEndDay : DateUtil.getDayOfMonth(year, month);
      mDayPicker.setAdapter(new NumericWheelAdapter(start, end));
      mDayPicker.setSelectedPosition(last - mDayPicker.getAdapter().getItem(0), false);
    }

    // 日联动小时
    resetHourAdapter(isInit);
  }

  private void resetHourAdapter(boolean isInit) {
    if (hasType(TYPE_HOUR)) {
      int year =
        hasType(TYPE_YEAR) ? mYearPicker.getSelectedItem() : mSelectedDate.get(Calendar.YEAR);
      int month = hasType(TYPE_MONTH) ? mMonthPicker.getSelectedItem()
        : mSelectedDate.get(Calendar.MONTH) + 1;
      int day =
        hasType(TYPE_DAY) ? mDayPicker.getSelectedItem() : mSelectedDate.get(Calendar.DAY_OF_MONTH);
      int last = isInit ? mSelectedDate.get(Calendar.HOUR_OF_DAY) : mHourPicker.getSelectedItem();
      int start = year == mStartYear && month == mStartMonth && day == mStartDay ? mStartHour : 0;
      int end = year == mEndYear && month == mEndMonth && day == mEndDay ? mEndHour : 23;
      mHourPicker.setAdapter(new NumericWheelAdapter(start, end));
      mHourPicker.setSelectedPosition(last - mHourPicker.getAdapter().getItem(0), false);
    }

    resetMinuteAdapter(isInit);
  }

  private void resetMinuteAdapter(boolean isInit) {
    if (hasType(TYPE_MINUTE)) {
      int year =
        hasType(TYPE_YEAR) ? mYearPicker.getSelectedItem() : mSelectedDate.get(Calendar.YEAR);
      int month = hasType(TYPE_MONTH) ? mMonthPicker.getSelectedItem()
        : mSelectedDate.get(Calendar.MONTH) + 1;
      int day =
        hasType(TYPE_DAY) ? mDayPicker.getSelectedItem() : mSelectedDate.get(Calendar.DAY_OF_MONTH);
      int hour = hasType(TYPE_HOUR) ? mHourPicker.getSelectedItem()
        : mSelectedDate.get(Calendar.HOUR_OF_DAY);
      int last = isInit ? mStartMinute : getRealMinute(mMinutePicker.getSelectedItem());
      int start =
        year == mStartYear && month == mStartMonth && day == mStartDay && hour == mStartHour
          ? mStartMinute : 0;
      int end =
        year == mEndYear && month == mEndMonth && day == mEndDay && hour == mEndHour ? mEndMinute
          : 60 - mTimeMinuteOffset;
      mMinutePicker
        .setAdapter(new NumericWheelAdapter(getValidMinuteValue(start), getValidMinuteValue(end)));
      mMinutePicker.setSelectedPosition(findPositionByValidTimes(last), false);
    }
  }

  // 获取有效分钟数对应的item的数值
  private int getValidMinuteValue(int validTimeMinutes) {
    return validTimeMinutes / mTimeMinuteOffset;
  }

  // 通过有效分钟数找到在adapter中的position
  private int findPositionByValidTimes(int validTimeMinutes) {
    int timesValue = getValidMinuteValue(validTimeMinutes);
    return timesValue - mMinutePicker.getAdapter().getItem(0);
  }

  // 获指定position的分钟item对应的真实的分钟数
  private int getRealMinute(int position) {
    return position * mTimeMinuteOffset;
  }

  /**
   * 获取根据mTimeMinuteOffset处理后的有效分钟数
   * 默认为 start <= X <= end 即都不包含在内
   */
  private int getValidTimeMinutes(int timeMinutes, boolean isStart) {
    int validTimeMinutes;
    int offset = timeMinutes % mTimeMinuteOffset;
    if (offset == 0) {
      validTimeMinutes = timeMinutes;
    } else {
      if (isStart) {
        validTimeMinutes = timeMinutes - offset;
        if (!mContainsStarDate) {
          validTimeMinutes += mTimeMinuteOffset;
        }
      } else {
        validTimeMinutes = timeMinutes - offset;
        if (mContainsEndDate) {
          validTimeMinutes += mTimeMinuteOffset;
        }
      }
    }
    return validTimeMinutes;
  }

  @Override public void onSelected(BasePickerView pickerView, int position) {
    // 联动，年份、月份是固定的，使用日历，获取指定指定某年某月的日期
    switch ((int) pickerView.getTag()) {
      case TYPE_YEAR:
        resetMonthAdapter(false);
        break;
      case TYPE_MONTH:
        resetDayAdapter(false);
        break;
      case TYPE_DAY:
        resetHourAdapter(false);
        break;
      case TYPE_HOUR:
        resetMinuteAdapter(false);
        break;
    }
  }

  @Override protected void onConfirm() {
    if (mOnTimeSelectListener != null) {
      Date date = getSelectedDate();
      if (date != null) mOnTimeSelectListener.onTimeSelect(this, date);
    }
  }

  @Override
  public CharSequence format(BasePickerView pickerView, int position, CharSequence charSequence) {
    if (mFormatter == null) return charSequence;
    int type = (int) pickerView.getTag();
    int value = Integer.parseInt(charSequence.toString());
    int num = type == TYPE_MINUTE ? getRealMinute(value) : value;
    return mFormatter.format(this, type, position, num);
  }

  public static class Builder {
    private Context mContext;
    private int mType;
    private long mStartDate = 0; // 默认起始为1970/1/1 8:0:0
    private long mEndDate = 4133865600000L; // 默认截止为2100/12/31 0:0:0
    private long mSelectedDate = -1;

    private Formatter mFormatter;
    private OnTimeSelectListener mOnTimeSelectListener;
    private Interceptor mInterceptor;

    // 时间分钟间隔
    private int mTimeMinuteOffset = 1;
    // 设置mTimeMinuteOffset时，是否包含起止时间
    private boolean mContainsStarDate = false;
    private boolean mContainsEndDate = false;

    /**
     * 强制设置的属性直接在构造方法中设置
     *
     * @param listener listener
     */
    public Builder(Context context, int type, OnTimeSelectListener listener) {
      mContext = context;
      mType = type;
      mOnTimeSelectListener = listener;
    }

    /**
     * 设置起止时间
     *
     * @param startDate 起始时间
     * @param endDate 截止时间
     */
    public Builder setRangDate(long startDate, long endDate) {
      mStartDate = startDate;
      mEndDate = endDate;
      return this;
    }

    /**
     * 设置选中时间戳
     *
     * @param millis 选中时间戳
     */
    public Builder setSelectedDate(long millis) {
      mSelectedDate = millis;
      return this;
    }

    /**
     * 设置时间间隔分钟数，以0为起始边界
     *
     * @param timeMinuteOffset 60%offset==0才有效
     */
    public Builder setTimeMinuteOffset(int timeMinuteOffset) {
      mTimeMinuteOffset = timeMinuteOffset;
      return this;
    }

    /**
     * 设置mTimeMinuteOffset作用时，是否包含超出的startDate
     *
     * @param containsStarDate 是否包含startDate
     */
    public Builder setContainsStarDate(boolean containsStarDate) {
      mContainsStarDate = containsStarDate;
      return this;
    }

    /**
     * 设置mTimeMinuteOffset作用时，是否包含超出的endDate
     *
     * @param containsEndDate 是否包含endDate
     */
    public Builder setContainsEndDate(boolean containsEndDate) {
      mContainsEndDate = containsEndDate;
      return this;
    }

    public Builder setFormatter(Formatter formatter) {
      mFormatter = formatter;
      return this;
    }

    public Builder setInterceptor(Interceptor interceptor) {
      mInterceptor = interceptor;
      return this;
    }

    public TimePicker create() {
      TimePicker picker = new TimePicker(mContext, mType, mOnTimeSelectListener);
      // 不支持重复设置的，都在builder中控制，一次性行为
      picker.setInterceptor(mInterceptor);
      picker.setRangDate(mStartDate, mEndDate);
      picker.mTimeMinuteOffset = mTimeMinuteOffset;
      picker.mContainsStarDate = mContainsStarDate;
      picker.mContainsEndDate = mContainsEndDate;
      if (mFormatter == null) {
        mFormatter = new DefaultFormatter();
      }
      picker.setFormatter(mFormatter);
      picker.initPicker();
      if (mSelectedDate < 0) {
        picker.reset();
      } else {
        picker.setSelectedDate(mSelectedDate);
      }
      return picker;
    }
  }

  public static class DefaultFormatter implements Formatter {
    @Override public CharSequence format(TimePicker picker, int type, int position, int num) {
      if (type == TimePicker.TYPE_YEAR) {
        return num + "年";
      } else if (type == TimePicker.TYPE_MONTH) {
        return String.format("%02d月", num);
      } else if (type == TimePicker.TYPE_DAY) {
        return String.format("%02d日", num);
      } else if (type == TimePicker.TYPE_HOUR) {
        return String.format("%2d时", num);
      } else if (type == TimePicker.TYPE_MINUTE) {
        return String.format("%2d分", num);
      }
      return String.valueOf(num);
    }
  }

  public interface Formatter {
    /**
     * 根据type和num格式化时间
     *
     * @param picker picker
     * @param type 并不是模式，而是当前item所属的type，如年，时
     * @param position position
     * @param num position item显示的数字
     */
    CharSequence format(TimePicker picker, int type, int position, int num);
  }

  public interface OnTimeSelectListener {
    /**
     * 点击确定按钮选择时间后回调
     *
     * @param date 选择的时间
     */
    void onTimeSelect(TimePicker picker, Date date);
  }
}
