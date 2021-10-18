package org.jaaksi.pickerview.picker;

import android.content.Context;
import androidx.annotation.Nullable;
//import android.support.annotation.Nullable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jaaksi.pickerview.adapter.ArrayWheelAdapter;
import org.jaaksi.pickerview.adapter.NumericWheelAdapter;
import org.jaaksi.pickerview.dialog.IPickerDialog;
import org.jaaksi.pickerview.util.DateUtil;
import org.jaaksi.pickerview.widget.BasePickerView;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * 创建时间：2018年08月02日15:42 <br>
 * 作者：fuchaoyang <br>
 * 描述：时间选择器
 * 强大点：
 * 1.type的设计，自由组合
 * 2.支持时间区间设置以及选中联动
 * 3.支持混合模式，支持日期，时间混合
 * 4.支持自定义日期、时间格式
 * 5.time支持设置时间间隔，如30分钟。也就是00:00 00:30 01:00 01:30
 * ，无法被60整除的，如设置13分钟，认为是无效设置，会被忽略。如13 26 39 52,只能选到52
 */

public class TimePicker extends BasePicker
  implements BasePickerView.OnSelectedListener, BasePickerView.Formatter {

  public static final int TYPE_YEAR = 0x01;
  public static final int TYPE_MONTH = 0x02;
  public static final int TYPE_DAY = 0x04;
  public static final int TYPE_HOUR = 0x08;
  public static final int TYPE_MINUTE = 0x10;
  /** 日期聚合 */
  public static final int TYPE_MIXED_DATE = 0x20;
  /** 时间聚合 */
  public static final int TYPE_MIXED_TIME = 0x40;
  /** 上午、下午（12小时制,默认24小时制，不显示上午，下午） */
  public static final int TYPE_12_HOUR = 0x80;

  // 日期：年月日
  public static final int TYPE_DATE = TYPE_YEAR | TYPE_MONTH | TYPE_DAY;
  // 时间：小时、分钟
  public static final int TYPE_TIME = TYPE_HOUR | TYPE_MINUTE;
  // 全部
  public static final int TYPE_ALL = TYPE_DATE | TYPE_TIME;

  public static final DateFormat DEFAULT_DATE_FORMAT = DateUtil.create("yyyy年MM月dd日");
  public static final DateFormat DEFAULT_TIME_FORMAT = DateUtil.create("HH:mm");

  private int mType;

  private PickerView<Integer> mDatePicker, mYearPicker, mMonthPicker, mDayPicker, mTimePicker,
    mHourPicker, mNoonPicker, mMinutePicker;

  // 初始设置选中的时间，如果不设置为startDate
  private Calendar mSelectedDate;
  private Calendar mStartDate;//开始时间
  //private Calendar mStartDate = Calendar.getInstance();//开始时间
  private Calendar mEndDate;//终止时间
  // 聚合的日期模式
  private int mDayOffset = -1;

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

  /**
   * @param calendar 指定时间
   * @param isStart 是否是起始时间
   * @return 指定时间的有效分钟偏移量
   */
  private int getValidTimeOffset(/*int timeMinutes,*/Calendar calendar, boolean isStart) {
    int timeMinutes = calendar.get(Calendar.MINUTE);
    int validOffset;
    int offset = timeMinutes % mTimeMinuteOffset;
    if (offset == 0) {
      validOffset = 0;
    } else {
      validOffset = -offset;
      if (isStart) {
        if (!mContainsStarDate) {
          validOffset += mTimeMinuteOffset;
        }
      } else {
        if (mContainsEndDate) {
          validOffset += mTimeMinuteOffset;
        }
      }
    }
    return validOffset;
  }

  private void ignoreSecond(Calendar calendar) {
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }

  private void setRangDate(long startDate, long endDate) {
    //  重新计算时间区间，bugfix：由于由于起始时间没有考虑时间间隔而导致可能会引起bug
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(startDate);
    ignoreSecond(calendar);
    calendar.add(Calendar.MINUTE, getValidTimeOffset(calendar, true));
    this.mStartDate = calendar;

    calendar = Calendar.getInstance();
    calendar.setTimeInMillis(endDate);
    ignoreSecond(calendar);
    calendar.add(Calendar.MINUTE, getValidTimeOffset(calendar, false));
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
    ignoreSecond(mSelectedDate);
  }

  /**
   * @return 是否有上下午，并且是下午
   */
  private boolean isAfterNoon() {
    return hasType(TYPE_12_HOUR) && mNoonPicker.getSelectedItem() == 1;
  }

  private Date getSelectedDates() {
    Calendar calendar = Calendar.getInstance();
    if (hasType(TYPE_MIXED_DATE)) {
      calendar.setTimeInMillis(mStartDate.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, mDatePicker.getSelectedPosition());
    } else {
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
    }
    if (hasType(TYPE_MIXED_TIME)) {
      int hour = mTimePicker.getSelectedItem() * mTimeMinuteOffset / 60;
      if (isAfterNoon()) { // 下午
        hour += 12;
      }
      calendar.set(Calendar.HOUR_OF_DAY, hour);
      int minute = mTimePicker.getSelectedItem() * mTimeMinuteOffset % 60;
      calendar.set(Calendar.MINUTE, minute);
    } else {
      if (hasType(TYPE_HOUR)) {
        int hour =
          isAfterNoon() ? mHourPicker.getSelectedItem() + 12 : mHourPicker.getSelectedItem();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
      }
      if (hasType(TYPE_MINUTE)) {
        calendar.set(Calendar.MINUTE, getRealMinute(mMinutePicker.getSelectedPosition()));
      }
    }
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
  @SuppressWarnings("unchecked")
  private void initPicker() {
    if (hasType(TYPE_MIXED_DATE)) {
      mDatePicker = createPickerView(TYPE_MIXED_DATE, 2.5f);
      mDatePicker.setOnSelectedListener(this);
      mDatePicker.setFormatter(this);
    } else {
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
    }
    if (hasType(TYPE_12_HOUR)) { // 上下午
      mNoonPicker = createPickerView(TYPE_12_HOUR, 1);
      mNoonPicker.setOnSelectedListener(this);
      mNoonPicker.setFormatter(this);
    }

    if (hasType(TYPE_MIXED_TIME)) { // 包含Time
      mTimePicker = createPickerView(TYPE_MIXED_TIME, 2);
      mTimePicker.setFormatter(this);
    } else {
      if (hasType(TYPE_HOUR)) {
        mHourPicker = createPickerView(TYPE_HOUR, 1);
        mHourPicker.setOnSelectedListener(this);
        mHourPicker.setFormatter(this);
        if (hasType(TYPE_12_HOUR)) { // 如果是12小时制，将小时设置为循环的
          mHourPicker.setIsCirculation(true);
        }
      }
      if (hasType(TYPE_MINUTE)) {
        mMinutePicker = createPickerView(TYPE_MINUTE, 1);
        mMinutePicker.setFormatter(this);
      }
    }
  }

  private void handleData() {
    if (mSelectedDate == null || mSelectedDate.getTimeInMillis() < mStartDate.getTimeInMillis()) {
      updateSelectedDate(mStartDate.getTimeInMillis());
    } else if (mSelectedDate.getTimeInMillis() > mEndDate.getTimeInMillis()) {
      updateSelectedDate(mEndDate.getTimeInMillis());
    }

    if (mTimeMinuteOffset < 1) {
      mTimeMinuteOffset = 1;
    }
    // 因为区间不能改变，所以这里只进行一次初始化操作
    if (mDayOffset == -1 || mStartYear == 0) {
      if (hasType(TYPE_MIXED_DATE)) {
        mDayOffset = offsetStart(mEndDate);
      } else {
        mStartYear = mStartDate.get(Calendar.YEAR);
        mEndYear = mEndDate.get(Calendar.YEAR);
        mStartMonth = mStartDate.get(Calendar.MONTH) + 1;
        mEndMonth = mEndDate.get(Calendar.MONTH) + 1;
        mStartDay = mStartDate.get(Calendar.DAY_OF_MONTH);
        mEndDay = mEndDate.get(Calendar.DAY_OF_MONTH);
      }

      mStartHour = mStartDate.get(Calendar.HOUR_OF_DAY);
      mEndHour = mEndDate.get(Calendar.HOUR_OF_DAY);
      mStartMinute = mStartDate.get(Calendar.MINUTE);
      mEndMinute = mEndDate.get(Calendar.MINUTE);
    }
  }

  private void reset() {
    handleData();
    // 处理数据，根据当前选中的时间及设置的日期范围处理数据
    if (hasType(TYPE_MIXED_DATE)) {
      if (mDatePicker.getAdapter() == null) {
        mDatePicker.setAdapter(new NumericWheelAdapter(0, mDayOffset));
      }
      mDatePicker.setSelectedPosition(offsetStart(mSelectedDate), false);

      if (hasType(TYPE_12_HOUR)){
        resetNoonAdapter(true);
      }
      if (hasType(TYPE_MIXED_TIME)) {
        // 时间需要考虑起始日期对应的起始时间
        resetTimeAdapter(true);
      } else {
        resetHourAdapter(true);
      }
    } else {
      if (hasType(TYPE_YEAR)) {
        if (mYearPicker.getAdapter() == null) { // 年不会发生变化，不需要重复设置
          mYearPicker.setAdapter(
            new NumericWheelAdapter(mStartDate.get(Calendar.YEAR), mEndDate.get(Calendar.YEAR)));
        }
        mYearPicker
          .setSelectedPosition(
            mSelectedDate.get(Calendar.YEAR) - mYearPicker.getAdapter().getItem(0),
            false);
      }
      resetMonthAdapter(true);
    }
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

    resetNoonAdapter(isInit);
  }

  /**
   * 选择上下午的时候，如果选中的是起止时间，要重置下一级（hour or timeadapter)
   * 比如起始时间是 9:30，如果是上午，则hour是9-11，如果是下午则是0-11，非起止时间都是0-11
   * 如果结束时间
   */
  private void resetNoonAdapter(boolean isInit) {
    if (hasType(TYPE_12_HOUR)) {
      boolean isSameStartDay = isSameDay(true);
      boolean isSameEndDay = isSameDay(false);

      List<Integer> noons = new ArrayList<>();
      if (!isSameStartDay || mStartHour < 12) { // 如果是起始的那天，且时间>11点，则不包含上午
        noons.add(0);
      }
      if (!isSameEndDay || mEndHour >= 12) {// 如果是结束的那天，且时间<12点，则不包含下午
        noons.add(1);
      }
      int last;
      if (isInit) {
        last = mSelectedDate.get(Calendar.HOUR_OF_DAY) < 12 ? 0 : 1;
      } else {
        last = mNoonPicker.getSelectedItem();
      }
      mNoonPicker.setAdapter(new ArrayWheelAdapter<>(noons));
      mNoonPicker.setSelectedPosition(last, false);
    }

    if (hasType(TYPE_MIXED_TIME)) {
      resetTimeAdapter(isInit);
    } else {
      // 日联动小时
      resetHourAdapter(isInit);
    }
  }

  private void resetTimeAdapter(boolean isInit) { // 如果是聚合日期+12小时制+聚合时间就crash了。因为没有初始化上下午adapter
    boolean isSameStartDay = isSameDay(true);
    boolean isSameEndDay = isSameDay(false);
    int start;
    int end;
    if (!hasType(TYPE_12_HOUR)) {
      start = isSameStartDay ? getValidTimeMinutes(mStartDate, true) : 0;
      end = isSameEndDay ? getValidTimeMinutes(mEndDate, false)
        : getValidTimeMinutes(24 * 60 - mTimeMinuteOffset, false);
    } else {
      if (isSameStartDay) {
        // 如果起始时间是上午，并且选择的是下午，start=0,否则start=get12Hour(start)
        if (mStartHour < 12 && mNoonPicker.getSelectedItem() == 1) {
          start = 0;
        } else {
          start = mStartHour >= 12 ? getValidTimeMinutes(mStartDate, true) - 12 * 60
            : getValidTimeMinutes(mStartDate, true);
        }
        if (isSameEndDay && mEndHour >= 12 && mNoonPicker.getSelectedItem() == 1) {
          // 如果 > 12 需要减去12小时
          end = mEndHour >= 12 ? getValidTimeMinutes(mEndDate, false) - 12 * 60
            : getValidTimeMinutes(mEndDate, false);
        } else {
          end = getValidTimeMinutes(12 * 60 - mTimeMinuteOffset, false);
        }
      } else if (isSameEndDay) {
        start = 0;
        if (mEndHour >= 12 && mNoonPicker.getSelectedItem() == 1) {
          // 如果 > 12 需要减去12小时
          end = mEndHour >= 12 ? getValidTimeMinutes(mEndDate, false) - 12 * 60
            : getValidTimeMinutes(mEndDate, false);
        } else {
          end = getValidTimeMinutes(12 * 60 - mTimeMinuteOffset, false);
        }
      } else {
        start = 0;
        end = getValidTimeMinutes(12 * 60 - mTimeMinuteOffset, false);
      }
    }
    int last;
    if (isInit) {
      if (hasType(TYPE_12_HOUR)) {
        int timeMinutes = getValidTimeMinutes(mSelectedDate, true);
        last = timeMinutes >= 12 * 60 ? getValidTimeMinutes(mSelectedDate, true) - 12 * 60
          : getValidTimeMinutes(mSelectedDate, true);
      } else {
        last = getValidTimeMinutes(mSelectedDate, true);
      }
    } else {
      last = mTimePicker.getSelectedItem() * mTimeMinuteOffset;
    }
    //  adapter 的item设置的是 有效分钟数/mTimeMinuteOffset
    mTimePicker
      .setAdapter(new NumericWheelAdapter(getValidTimesValue(start), getValidTimesValue(end)));
    mTimePicker.setSelectedPosition(findPositionByValidTimes(last), false);
  }

  private void resetHourAdapter(boolean isInit) {
    if (hasType(TYPE_HOUR)) {
      boolean isSameStartDay = isSameDay(true);
      boolean isSameEndDay = isSameDay(false);
      int start, end, last;
      if (!hasType(TYPE_12_HOUR)) {
        start = isSameStartDay ? mStartHour : 0;
        end = isSameEndDay ? mEndHour : 23;
      } else {
        if (isSameStartDay) {
          // 如果起始时间是上午，并且选择的是下午，start=0,否则start=get12Hour(start)
          if (mStartHour < 12 && mNoonPicker.getSelectedItem() == 1) {
            start = 0;
          } else {
            start = get12Hour(mStartHour);
          }
          if (isSameEndDay
            && mEndHour >= 12
            && mNoonPicker.getSelectedItem() == 1) { // 如果开始和结束时间是同一天
            end = get12Hour(mEndHour);
          } else {
            end = 11;
          }
        } else if (isSameEndDay) {
          start = 0;
          // 如果截止时间是下午，如果选择的是上午，end=11，如果选择的下午，end=get12Hour(mEndHour)
          // 如果截止时间是上午，选择的是上午，end=get12Hour
          if (mEndHour >= 12 && mNoonPicker.getSelectedItem() == 1) {
            end = get12Hour(mEndHour);
          } else {
            end = 11;
          }
        } else {
          start = 0;
          end = 11;
        }
      }
      if (isInit) {
        if (hasType(TYPE_12_HOUR)) {
          last = get12Hour(mSelectedDate.get(Calendar.HOUR_OF_DAY));
        } else {
          last = mSelectedDate.get(Calendar.HOUR_OF_DAY);
        }
      } else {
        last = mHourPicker.getSelectedItem();
      }

      mHourPicker.setAdapter(new NumericWheelAdapter(start, end));
      mHourPicker.setSelectedPosition(last - mHourPicker.getAdapter().getItem(0), false);
    }
    resetMinuteAdapter(isInit);
  }

  private int get12Hour(int hour) {
    if (hour >= 12) {
      return hour - 12;
    }
    return hour;
  }

  private boolean isSameDay(boolean isStart) {
    boolean isSameDay;
    if (hasType(TYPE_MIXED_DATE)) {
      if (isStart) {
        isSameDay =
          DateUtil.getDayOffset(getSelectedDate().getTime(), mStartDate.getTimeInMillis()) == 0;
      } else {
        isSameDay =
          DateUtil.getDayOffset(getSelectedDate().getTime(), mEndDate.getTimeInMillis()) == 0;
      }
    } else {
      int year =
        hasType(TYPE_YEAR) ? mYearPicker.getSelectedItem() : mSelectedDate.get(Calendar.YEAR);
      int month = hasType(TYPE_MONTH) ? mMonthPicker.getSelectedItem()
        : mSelectedDate.get(Calendar.MONTH) + 1;
      int day = hasType(TYPE_DAY) ? mDayPicker.getSelectedItem()
        : mSelectedDate.get(Calendar.DAY_OF_MONTH);
      if (isStart) {
        isSameDay = year == mStartYear && month == mStartMonth && day == mStartDay;
      } else {
        isSameDay = year == mEndYear && month == mEndMonth && day == mEndDay;
      }
    }
    return isSameDay;
  }

  private void resetMinuteAdapter(boolean isInit) {
    if (hasType(TYPE_MINUTE)) {
      boolean isSameStartDay;
      boolean isSameEndDay;

      if (hasType(TYPE_MIXED_DATE)) {
        isSameStartDay =
          DateUtil.getDayOffset(getSelectedDate().getTime(), mStartDate.getTimeInMillis()) == 0;
        isSameEndDay =
          DateUtil.getDayOffset(getSelectedDate().getTime(), mEndDate.getTimeInMillis()) == 0;
      } else {
        int year =
          hasType(TYPE_YEAR) ? mYearPicker.getSelectedItem() : mSelectedDate.get(Calendar.YEAR);
        int month = hasType(TYPE_MONTH) ? mMonthPicker.getSelectedItem()
          : mSelectedDate.get(Calendar.MONTH) + 1;
        int day = hasType(TYPE_DAY) ? mDayPicker.getSelectedItem()
          : mSelectedDate.get(Calendar.DAY_OF_MONTH);
        isSameStartDay = year == mStartYear && month == mStartMonth && day == mStartDay;
        isSameEndDay = year == mEndYear && month == mEndMonth && day == mEndDay;
      }
      int hour;
      if (hasType(TYPE_HOUR)) {
        if (hasType(TYPE_12_HOUR) && mNoonPicker.getSelectedItem() == 1) {
          hour = mHourPicker.getSelectedItem() + 12;
        } else {
          hour = mHourPicker.getSelectedItem();
        }
      } else {
        hour = mSelectedDate.get(Calendar.HOUR_OF_DAY);
      }
      int last = isInit ? mSelectedDate.get(Calendar.MINUTE)
        : getRealMinute(mMinutePicker.getSelectedPosition());
      int start = isSameStartDay && hour == mStartHour ? mStartMinute : 0;
      int end = isSameEndDay && hour == mEndHour ? mEndMinute : 60 - mTimeMinuteOffset;
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
    if (mMinutePicker != null) {

      return timesValue - mMinutePicker.getAdapter().getItem(0);
    }
    return timesValue - mTimePicker.getAdapter().getItem(0);
  }

  /**
   * 获取对应position的真实分钟数，注意这里必须使用position
   *
   * @param position {@link BasePickerView#getSelectedPosition}
   */
  // 获指定position的分钟item对应的真实的分钟数
  private int getRealMinute(int position) {
    // bugfix:这个position是下标，要拿对应item的数值来计算
    return mMinutePicker.getAdapter().getItem(position) * mTimeMinuteOffset;
  }

  // 获取指定position对应的有效的分钟数
  private int getPositionValidMinutes(int position) {
    return mTimePicker.getAdapter().getItem(position) * mTimeMinuteOffset;
  }

  // 获取有效分钟数对应的item的数值
  private int getValidTimesValue(int validTimeMinutes) {
    return validTimeMinutes / mTimeMinuteOffset;
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

  /**
   * 获取时间的分钟数
   */
  private int getValidTimeMinutes(@Nullable Calendar calendar, boolean isStart) {
    if (calendar == null) return 0;

    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    int minutes = hour * 60 + minute;
    return getValidTimeMinutes(minutes, isStart);
  }

  /**
   * 获取指定日期距离第0个的offset
   */
  private int offsetStart(Calendar calendar) {
    return DateUtil.getDayOffset(calendar.getTimeInMillis(), mStartDate.getTimeInMillis());
  }

  // 只有日期是准确的
  private Date getSelectedDate() {
    return getPositionDate(mDatePicker.getSelectedPosition());
  }

  // 获取对应position的日期
  private Date getPositionDate(int position) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(mStartDate.getTimeInMillis());
    calendar.add(Calendar.DAY_OF_YEAR, position);
    return calendar.getTime();
  }

  // 获取对应position的时间
  private Date getPositionTime(int position) {
    Calendar calendar = Calendar.getInstance();
    // 计算出position对应的hour & minute
    int minutes = mTimePicker.getAdapter().getItem(position) * mTimeMinuteOffset;
    int hour = minutes / 60;
    int minute = minutes % 60;
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minute);
    return calendar.getTime();
  }

  @Override
  public void onSelected(BasePickerView pickerView, int position) {
    // 联动，年份、月份是固定的，使用日历，获取指定指定某年某月的日期
    switch ((int) pickerView.getTag()) {
      case TYPE_YEAR:
        resetMonthAdapter(false);
        break;
      case TYPE_MONTH:
        resetDayAdapter(false);
        break;
      case TYPE_MIXED_DATE:
      case TYPE_DAY:
        resetNoonAdapter(false);
        break;
      case TYPE_12_HOUR:
        if (hasType(TYPE_MIXED_TIME)) {
          resetTimeAdapter(false);
        } else {
          resetHourAdapter(false);
        }
        break;
      case TYPE_HOUR:
        resetMinuteAdapter(false);
        break;
    }
  }

  @Override
  public void onConfirm() {
    if (mOnTimeSelectListener != null) {
      Date date = getSelectedDates();
      if (date != null) mOnTimeSelectListener.onTimeSelect(this, date);
    }
  }

  /**
   * @param position 这个是adapter的position，但是起始时间如果不从0开始就不对了
   */
  @Override
  public CharSequence format(BasePickerView pickerView, int position, CharSequence charSequence) {
    if (mFormatter == null) return charSequence;
    int type = (int) pickerView.getTag();
    long value;
    if (type == TYPE_MIXED_DATE) {
      value = getPositionDate(position).getTime();
    } else if (type == TYPE_MIXED_TIME) {
      value = getPositionTime(position).getTime();
    } else if (type == TYPE_MINUTE) {
      value = getRealMinute(position);
    } else {
      value = Integer.parseInt(charSequence.toString());
    }
    return mFormatter.format(this, type, position, value);
  }

  public static class Builder {
    private Context mContext;
    private int mType;
    // 都应该设置起止时间的，哪怕是只有时间格式，因为真实回调的是时间戳
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

    private boolean needDialog = true;
    private IPickerDialog iPickerDialog;

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
      mEndDate = endDate;
      if (endDate < startDate){
        mStartDate = endDate;
      } else {
        mStartDate = startDate;
      }
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

    /**
     * 自定义弹窗
     *
     * @param iPickerDialog 如果为null表示不需要弹窗
     */
    public Builder dialog(@Nullable IPickerDialog iPickerDialog) {
      needDialog = iPickerDialog != null;
      this.iPickerDialog = iPickerDialog;
      return this;
    }

    public TimePicker create() {
      TimePicker picker = new TimePicker(mContext, mType, mOnTimeSelectListener);
      // 不支持重复设置的，都在builder中控制，一次性行为
      picker.needDialog = needDialog;
      picker.iPickerDialog = iPickerDialog;
      picker.initPickerView();
      picker.setInterceptor(mInterceptor);
      picker.mTimeMinuteOffset = mTimeMinuteOffset;
      picker.mContainsStarDate = mContainsStarDate;
      picker.mContainsEndDate = mContainsEndDate;
      picker.setRangDate(mStartDate, mEndDate);
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
    @Override
    public CharSequence format(TimePicker picker, int type, int position, long value) {
      if (type == TimePicker.TYPE_YEAR) {
        return value + "年";
      } else if (type == TimePicker.TYPE_MONTH) {
        return String.format("%02d月", value);
      } else if (type == TimePicker.TYPE_DAY) {
        return String.format("%02d日", value);
      } else if (type == TimePicker.TYPE_12_HOUR) {
        return value == 0 ? "上午" : "下午";
      } else if (type == TimePicker.TYPE_HOUR) {
        if (picker.hasType(TimePicker.TYPE_12_HOUR)) {
          if (value == 0) {
            return "12时";
          }
        }
        return String.format("%2d时", value);
      } else if (type == TimePicker.TYPE_MINUTE) {
        return String.format("%2d分", value);
      } else if (type == TimePicker.TYPE_MIXED_DATE) {
        // 如果是TYPE_MIXED_,则value表示时间戳
        return DEFAULT_DATE_FORMAT.format(new Date(value));
      } else if (type == TimePicker.TYPE_MIXED_TIME) {
        String time = DEFAULT_TIME_FORMAT.format(new Date(value));
        if (picker.hasType(TimePicker.TYPE_12_HOUR)) {
          return time.replace("00:", "12:"); // 12小时
        } else {
          return time;
        }
      }
      return String.valueOf(value);
    }
  }

  public interface Formatter {
    /**
     * 根据type和num格式化时间
     *
     * @param picker picker
     * @param type 并不是模式，而是当前item所属的type，如年，时
     * @param position position
     * @param value position item对应的value，如果是TYPE_MIXED_DATE表示日期时间戳，否则表示显示的数字
     */
    CharSequence format(TimePicker picker, int type, int position,
        long value);
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
