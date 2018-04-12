package org.jaaksi.pickerview.picker;

import android.content.Context;
import android.support.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.jaaksi.pickerview.adapter.NumericWheelAdapter;
import org.jaaksi.pickerview.util.DateUtil;
import org.jaaksi.pickerview.widget.BasePickerView;
import org.jaaksi.pickerview.widget.PickerView;

/**
 * Created by fuchaoyang on 2018/2/6.<br/>
 * description：不同于TimePicker 混合的日期-时间 picker，日期混合，时间混合
 * time支持设置时间间隔，如30分钟。也就是00:00 00:30 01:00 01:30
 * ，无法被60整除的，如设置13分钟，认为是无效设置，会被忽略。如13 26 39 52,只能选到52
 * 强大点：
 * 1.混合模式
 * 2.支持自定义日期格式，时间格式
 * 3.支持设置时间间隔
 * 4.支持设置区间以及选中联动
 */

public class MixedTimePicker extends BasePicker
    implements BasePickerView.OnSelectedListener, BasePickerView.Formatter {

  public static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日");
  public static final DateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat("HH:mm");

  public static final int TYPE_DATE = 0x01;
  public static final int TYPE_TIME = 0x02;
  public static final int TYPE_ALL = TYPE_DATE | TYPE_TIME;

  private int mType = TYPE_ALL;

  private PickerView<Integer> mDatePicker;
  // item 数值getSelectedItem 对应的是 （有效）分钟数 /  mTimeMinuteOffset
  private PickerView<Integer> mTimePicker;

  // 只要包含日期，就一定不会为null，纯时间模式，如果没有设置起始时间则为null
  // 初始设置选中的时间，如果不设置为startDate
  private Calendar mSelectedDate;
  // 起止时间可以不设置，会赋默认值。如果包日期模式，则强制设置star and end
  // 纯时间模式，如果设置了star和end（最好star设置为有效的，回调的日期取得这个）逻辑不变，如果没有设置，那么就认为!star and !end
  private Calendar mStartDate;//开始时间
  private Calendar mEndDate;//终止时间
  private int mDayOffset;
  // 时间分钟间隔
  private int mTimeMinuteOffset;
  // 设置mTimeMinuteOffset时，是否包含起止时间
  private boolean mContainsStarDate;
  private boolean mContainsEndDate;

  private Formatter mFormatter;
  private OnTimeSelectListener mOnTimeSelectListener;

  private MixedTimePicker(Context context, int type, OnTimeSelectListener listener) {
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

  /**
   * @return type
   */
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

  @SuppressWarnings("unchecked") private void initPicker() {
    if (hasType(TYPE_DATE)) { // 如果包含Date
      mDatePicker = createPickerView(TYPE_DATE, 1.5f);
      mDatePicker.setFormatter(this);
      if (hasType(TYPE_TIME)) {
        mDatePicker.setOnSelectedListener(this);
      }
    }
    if (hasType(TYPE_TIME)) { // 包含Time
      mTimePicker = createPickerView(TYPE_TIME, 1);
      mTimePicker.setFormatter(this);
    }
  }

  private void handleData() {
    if (mStartDate != null) {
      if (mSelectedDate == null
          || mSelectedDate.getTimeInMillis() < mStartDate.getTimeInMillis()
          || mSelectedDate.getTimeInMillis() > mEndDate.getTimeInMillis()) {
        updateSelectedDate(mStartDate.getTimeInMillis());
      }
    }
    if (mTimeMinuteOffset < 1) {
      mTimeMinuteOffset = 1;
    }
    if (hasType(TYPE_DATE)) {
      mDayOffset = offsetStart(mEndDate);
    }
  }

  private void reset() {
    handleData();
    if (hasType(TYPE_DATE)) {
      // 处理数据，根据当前选中的时间及设置的日期范围处理数据
      if (mDatePicker.getAdapter() == null) {
        mDatePicker.setAdapter(new NumericWheelAdapter(0, mDayOffset));
      }
      mDatePicker.setSelectedPosition(offsetStart(mSelectedDate), false);
    }
    if (hasType(TYPE_TIME)) {
      // 时间需要考虑起始日期对应的起始时间
      resetTimeAdapter(true);
    }
  }

  private void resetTimeAdapter(boolean isInit) {
    int start, end, last; // 这里start, end, last都是分钟数，而不是position
    // mTimePicker.getSelectedItem()返回的是 有效分钟数/mTimeMinuteOffset
    if (mType == TYPE_TIME) {
      // 纯时间模式 如果设置了star and end 就直接认为是当天（只考虑时间），否则直接start=0,end=结束
      last = isInit ? getValidTimeMinutes(mSelectedDate, true)
          : mTimePicker.getSelectedItem() * mTimeMinuteOffset;
      if (mStartDate != null && mEndDate != null) {
        start = getValidTimeMinutes(mStartDate, true);
        end = getValidTimeMinutes(mEndDate, false);
      } else {
        start = 0;
        end = getValidTimeMinutes(24 * 60 - mTimeMinuteOffset, false);
      }
    } else {
      // 日期&时间
      Calendar selectCalendar = Calendar.getInstance();
      if (isInit) {
        selectCalendar.setTimeInMillis(mSelectedDate.getTimeInMillis());
        last = getValidTimeMinutes(mSelectedDate, true);
      } else {
        selectCalendar.setTimeInMillis(getSelectedDate().getTime());
        last = mTimePicker.getSelectedItem() * mTimeMinuteOffset;
      }

      start = offset(selectCalendar, mStartDate) == 0 ? getValidTimeMinutes(mStartDate, true) : 0;
      end = offset(selectCalendar, mEndDate) == 0 ? getValidTimeMinutes(mEndDate, false)
          : getValidTimeMinutes(24 * 60 - mTimeMinuteOffset, false);
    }

    //  adapter 的item设置的是 有效分钟数/mTimeMinuteOffset
    mTimePicker.setAdapter(
        new NumericWheelAdapter(getValidTimesValue(start), getValidTimesValue(end)));
    mTimePicker.setSelectedPosition(findPositionByValidTimes(last), false);
  }

  // 获取指定position对应的有效的分钟数
  private int getPositionValidMinutes(int position) {
    return mTimePicker.getAdapter().getItem(position) * mTimeMinuteOffset;
  }

  // 获取有效分钟数对应的item的数值
  private int getValidTimesValue(int validTimeMinutes) {
    return validTimeMinutes / mTimeMinuteOffset;
  }

  // 通过有效分钟数找到在adapter中的position
  private int findPositionByValidTimes(int validTimeMinutes) {
    int timesValue = getValidTimesValue(validTimeMinutes);
    return timesValue - mTimePicker.getAdapter().getItem(0);
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

  private int offset(Calendar calendar1, Calendar calendar2) {
    return DateUtil.getDayOffset(calendar1.getTimeInMillis(), calendar2.getTimeInMillis());
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

  // 获取选中的日期和时间
  private Date getSelectedDates() {
    Calendar calendar = Calendar.getInstance();
    if (hasType(TYPE_DATE)) {
      calendar.setTimeInMillis(mStartDate.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, mDatePicker.getSelectedPosition());
    } else if (mSelectedDate != null) { // 如果没有日期，则取选中时间的起始日期
      calendar.setTimeInMillis(mSelectedDate.getTimeInMillis());
    }
    if (hasType(TYPE_TIME)) {
      int hour = mTimePicker.getSelectedItem() * mTimeMinuteOffset / 60;
      calendar.set(Calendar.HOUR_OF_DAY, hour);
      int minute = mTimePicker.getSelectedItem() * mTimeMinuteOffset % 60;
      calendar.set(Calendar.MINUTE, minute);
    }

    return calendar.getTime();
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

  @Override public void onSelected(BasePickerView pickerView, int position) {
    // 日期联动时间
    resetTimeAdapter(false);
  }

  @Override protected void onConfirm() {
    if (mOnTimeSelectListener != null) {
      Date date = getSelectedDates();
      if (date != null) mOnTimeSelectListener.onTimeSelect(this, date);
    }
  }

  @Override
  public CharSequence format(BasePickerView pickerView, int position, CharSequence charSequence) {
    if (mFormatter == null) return charSequence;

    int tag = (int) pickerView.getTag();
    if (tag == TYPE_DATE) {
      // 根据起始及偏移量，计算出当前position对应的day
      return mFormatter.format(MixedTimePicker.this, TYPE_DATE, getPositionDate(position),
          position);
    } else if (tag == TYPE_TIME) {
      return mFormatter.format(MixedTimePicker.this, TYPE_TIME, getPositionTime(position),
          position);
    }
    return charSequence;
  }

  public static class Builder {
    private Context mContext;
    private Interceptor mInterceptor;

    private int mType;
    private long mStartDate = -1;
    private long mEndDate = -1;
    private long mSelectedDate = -1;
    private Formatter mFormatter;
    private OnTimeSelectListener mOnTimeSelectListener;
    // 时间分钟间隔
    private int mTimeMinuteOffset = 1;
    // 设置mTimeMinuteOffset时，是否包含起止时间
    private boolean mContainsStarDate = false;
    private boolean mContainsEndDate = false;

    public Builder(Context context, int type, OnTimeSelectListener onTimeSelectListener) {
      mContext = context;
      mType = type;
      mOnTimeSelectListener = onTimeSelectListener;
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

    /**
     * 设置Formatter
     *
     * @param formatter formatter
     */
    public Builder setFormatter(Formatter formatter) {
      mFormatter = formatter;
      return this;
    }

    /**
     * 设置拦截器
     *
     * @param interceptor 拦截器
     */
    public Builder setInterceptor(Interceptor interceptor) {
      mInterceptor = interceptor;
      return this;
    }

    /**
     * 通过Builder构建 MixedTimePicker
     *
     * @return MixedTimePicker
     */
    public MixedTimePicker create() {
      // 如果包含日期，却没有设置起止时间就认为错误
      if ((mType & TYPE_DATE) == TYPE_DATE && (mStartDate < 0 || mEndDate < 0)) {
        throw new RuntimeException("must set start and end date when contains date type.");
      }

      MixedTimePicker picker = new MixedTimePicker(mContext, mType, mOnTimeSelectListener);
      picker.setInterceptor(mInterceptor);
      if (mStartDate > -1 && mEndDate > -1) {
        picker.setRangDate(mStartDate, mEndDate);
      }
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

    @Override
    public CharSequence format(MixedTimePicker picker, int type, Date date, int position) {
      if (type == TYPE_DATE) {
        return DEFAULT_DATE_FORMAT.format(date);
      } else {
        return DEFAULT_TIME_FORMAT.format(date);
      }
    }
  }

  public interface Formatter {
    /**
     * 用户可以自定义日期格式和时间格式
     *
     * @param picker picker
     * @param type 并不是模式，而是当前item所属的type，如日期，时间
     * @param date 当前状态对应的日期或者时间
     * @param position 当前type所在的position
     */
    CharSequence format(MixedTimePicker picker, int type, Date date, int position);
  }

  public interface OnTimeSelectListener {
    /**
     * 选中回调
     *
     * @param picker MixedTimePicker
     * @param date date
     */
    void onTimeSelect(MixedTimePicker picker, Date date);
  }
}
