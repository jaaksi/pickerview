package org.jaaksi.pickerview.picker

import android.content.Context
import org.jaaksi.pickerview.adapter.ArrayWheelAdapter
import org.jaaksi.pickerview.adapter.NumericWheelAdapter
import org.jaaksi.pickerview.dialog.IPickerDialog
import org.jaaksi.pickerview.util.DateUtil.createDateFormat
import org.jaaksi.pickerview.util.DateUtil.getDayOfMonth
import org.jaaksi.pickerview.util.DateUtil.getDayOffset
import org.jaaksi.pickerview.widget.BasePickerView
import org.jaaksi.pickerview.widget.BasePickerView.OnSelectedListener
import org.jaaksi.pickerview.widget.PickerView
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

/**
 * 创建时间：2018年08月02日15:42 <br></br>
 * 作者：fuchaoyang <br></br>
 * 描述：时间选择器
 * 强大点：
 * 1.type的设计，自由组合
 * 2.支持时间区间设置以及选中联动
 * 3.支持混合模式，支持日期，时间混合
 * 4.支持自定义日期、时间格式
 * 5.time支持设置时间间隔，如30分钟。也就是00:00 00:30 01:00 01:30
 * ，无法被60整除的，如设置13分钟，认为是无效设置，会被忽略。如13 26 39 52,只能选到52
 */
class TimePicker private constructor(
    context: Context,
    private val type: Int,
    private val onTimeSelectListener: OnTimeSelectListener
) : BasePicker(context), OnSelectedListener, BasePickerView.Formatter {
    private var mDatePicker: PickerView<Int>? = null
    private var mYearPicker: PickerView<Int>? = null
    private var mMonthPicker: PickerView<Int>? = null
    private var mDayPicker: PickerView<Int>? = null
    private var mTimePicker: PickerView<Int>? = null
    private var mHourPicker: PickerView<Int>? = null
    private var mNoonPicker: PickerView<Int>? = null
    private var mMinutePicker: PickerView<Int>? = null

    // 初始设置选中的时间，如果不设置为startDate
    private var mSelectedDate: Calendar? = null
    private lateinit var mStartDate: Calendar //开始时间
    private lateinit var mEndDate: Calendar //终止时间

    // 聚合的日期模式
    private var mDayOffset = -1
    private var mStartYear = 0
    private var mEndYear = 0
    private var mStartMonth = 0
    private var mEndMonth = 0
    private var mStartDay = 0
    private var mEndDay = 0
    private var mStartHour = 0
    private var mEndHour = 0
    private var mStartMinute = 0
    private var mEndMinute = 0

    // 时间分钟间隔
    private var mTimeMinuteOffset = 0

    // 设置offset时，是否包含起止时间
    private var mContainsStarDate = false
    private var mContainsEndDate = false
    var formatter: Formatter? = null

    /**
     * @param calendar 指定时间
     * @param isStart 是否是起始时间
     * @return 指定时间的有效分钟偏移量
     */
    private fun getValidTimeOffset(
        calendar: Calendar, isStart: Boolean
    ): Int {
        val timeMinutes = calendar[Calendar.MINUTE]
        var validOffset: Int
        val offset = timeMinutes % mTimeMinuteOffset
        if (offset == 0) {
            validOffset = 0
        } else {
            validOffset = -offset
            if (isStart) {
                if (!mContainsStarDate) {
                    validOffset += mTimeMinuteOffset
                }
            } else {
                if (mContainsEndDate) {
                    validOffset += mTimeMinuteOffset
                }
            }
        }
        return validOffset
    }

    private fun ignoreSecond(calendar: Calendar) {
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
    }

    private fun setRangDate(startDate: Long, endDate: Long) {
        //  重新计算时间区间，bugfix：由于由于起始时间没有考虑时间间隔而导致可能会引起bug
        var calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        ignoreSecond(calendar)
        calendar.add(Calendar.MINUTE, getValidTimeOffset(calendar, true))
        mStartDate = calendar
        calendar = Calendar.getInstance()
        calendar.timeInMillis = endDate
        ignoreSecond(calendar)
        calendar.add(Calendar.MINUTE, getValidTimeOffset(calendar, false))
        mEndDate = calendar
    }

    /**
     * 设置选中的时间，如果不设置默认为起始时间，应该伴随着show方法使用
     *
     * @param millis 选中的时间戳 单位ms
     */
    fun setSelectedDate(millis: Long) {
        updateSelectedDate(millis)
        reset()
    }

    private fun updateSelectedDate(millis: Long) {
        if (mSelectedDate == null) {
            mSelectedDate = Calendar.getInstance()
        }
        mSelectedDate!!.timeInMillis = millis
        ignoreSecond(mSelectedDate!!)
    }

    /**
     * @return 是否有上下午，并且是下午
     */
    private val isAfterNoon: Boolean
        private get() = hasType(TYPE_12_HOUR) && mNoonPicker!!.selectedItem == 1

    val selectedDates: Date
        get() {
            val calendar = Calendar.getInstance()
            if (hasType(TYPE_MIXED_DATE)) {
                calendar.timeInMillis = mStartDate.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, mDatePicker!!.selectedPosition)
            } else {
                calendar.time = mSelectedDate!!.time
                if (hasType(TYPE_YEAR)) {
                    calendar[Calendar.YEAR] = mYearPicker!!.selectedItem!!
                }
                if (hasType(TYPE_MONTH)) {
                    calendar[Calendar.MONTH] = mMonthPicker!!.selectedItem!! - 1
                }
                if (hasType(TYPE_DAY)) {
                    calendar[Calendar.DAY_OF_MONTH] = mDayPicker!!.selectedItem!!
                }
            }
            if (hasType(TYPE_MIXED_TIME)) {
                var hour = mTimePicker!!.selectedItem!! * mTimeMinuteOffset / 60
                if (isAfterNoon) { // 下午
                    hour += 12
                }
                calendar[Calendar.HOUR_OF_DAY] = hour
                val minute = mTimePicker!!.selectedItem!! * mTimeMinuteOffset % 60
                calendar[Calendar.MINUTE] = minute
            } else {
                if (hasType(TYPE_HOUR)) {
                    val hour =
                        if (isAfterNoon) mHourPicker!!.selectedItem!! + 12 else mHourPicker!!.selectedItem!!
                    calendar[Calendar.HOUR_OF_DAY] = hour
                }
                if (hasType(TYPE_MINUTE)) {
                    calendar[Calendar.MINUTE] = getRealMinute(mMinutePicker!!.selectedPosition)
                }
            }
            return calendar.time
        }

    /**
     * @param type type
     * @return 是否包含类型 type
     */
    fun hasType(type: Int): Boolean {
        return this.type and type == type
    }

    /**
     * createPickerView在init中执行，那么[.setInterceptor]就必须在构造该方法之前执行才有效。采用Builder
     */
    private fun initPicker() {
        if (hasType(TYPE_MIXED_DATE)) {
            mDatePicker = createPickerView(TYPE_MIXED_DATE, 2.5f)
            mDatePicker!!.setOnSelectedListener(this)
            mDatePicker!!.formatter = this
        } else {
            if (hasType(TYPE_YEAR)) {
                mYearPicker = createPickerView(TYPE_YEAR, 1.2f)
                mYearPicker!!.setOnSelectedListener(this)
                mYearPicker!!.formatter = this
            }
            if (hasType(TYPE_MONTH)) {
                mMonthPicker = createPickerView(TYPE_MONTH, 1f)
                mMonthPicker!!.setOnSelectedListener(this)
                mMonthPicker!!.formatter = this
            }
            if (hasType(TYPE_DAY)) {
                mDayPicker = createPickerView(TYPE_DAY, 1f)
                mDayPicker!!.setOnSelectedListener(this)
                mDayPicker!!.formatter = this
            }
        }
        if (hasType(TYPE_12_HOUR)) { // 上下午
            mNoonPicker = createPickerView(TYPE_12_HOUR, 1f)
            mNoonPicker!!.setOnSelectedListener(this)
            mNoonPicker!!.formatter = this
        }
        if (hasType(TYPE_MIXED_TIME)) { // 包含Time
            mTimePicker = createPickerView(TYPE_MIXED_TIME, 2f)
            mTimePicker!!.formatter = this
            mTimePicker!!.setOnSelectedListener(this)
        } else {
            if (hasType(TYPE_HOUR)) {
                mHourPicker = createPickerView(TYPE_HOUR, 1f)
                mHourPicker!!.setOnSelectedListener(this)
                mHourPicker!!.formatter = this
                if (hasType(TYPE_12_HOUR)) { // 如果是12小时制，将小时设置为循环的
                    mHourPicker!!.setIsCirculation(true)
                }
            }
            if (hasType(TYPE_MINUTE)) {
                mMinutePicker = createPickerView(TYPE_MINUTE, 1f)
                mMinutePicker!!.formatter = this
                mMinutePicker!!.setOnSelectedListener(this)
            }
        }
    }

    private fun handleData() {
        if (mSelectedDate == null || mSelectedDate!!.timeInMillis < mStartDate.timeInMillis) {
            updateSelectedDate(mStartDate.timeInMillis)
        } else if (mSelectedDate!!.timeInMillis > mEndDate.timeInMillis) {
            updateSelectedDate(mEndDate.timeInMillis)
        }
        if (mTimeMinuteOffset < 1) {
            mTimeMinuteOffset = 1
        }
        // 因为区间不能改变，所以这里只进行一次初始化操作
        if (mDayOffset == -1 || mStartYear == 0) {
            if (hasType(TYPE_MIXED_DATE)) {
                mDayOffset = offsetStart(mEndDate)
            } else {
                mStartYear = mStartDate[Calendar.YEAR]
                mEndYear = mEndDate[Calendar.YEAR]
                mStartMonth = mStartDate[Calendar.MONTH] + 1
                mEndMonth = mEndDate[Calendar.MONTH] + 1
                mStartDay = mStartDate[Calendar.DAY_OF_MONTH]
                mEndDay = mEndDate[Calendar.DAY_OF_MONTH]
            }
            mStartHour = mStartDate[Calendar.HOUR_OF_DAY]
            mEndHour = mEndDate[Calendar.HOUR_OF_DAY]
            mStartMinute = mStartDate[Calendar.MINUTE]
            mEndMinute = mEndDate[Calendar.MINUTE]
        }
    }

    private fun reset() {
        handleData()
        // 处理数据，根据当前选中的时间及设置的日期范围处理数据
        if (hasType(TYPE_MIXED_DATE)) {
            if (mDatePicker!!.adapter == null) {
                mDatePicker!!.adapter = NumericWheelAdapter(0, mDayOffset)
            }
            mDatePicker!!.setSelectedPosition(offsetStart(mSelectedDate!!), false)
            if (hasType(TYPE_12_HOUR)) {
                resetNoonAdapter(true)
            }
            if (hasType(TYPE_MIXED_TIME)) {
                // 时间需要考虑起始日期对应的起始时间
                resetTimeAdapter(true)
            } else {
                resetHourAdapter(true)
            }
        } else {
            if (hasType(TYPE_YEAR)) {
                if (mYearPicker!!.adapter == null) { // 年不会发生变化，不需要重复设置
                    mYearPicker!!.adapter =
                        NumericWheelAdapter(mStartDate[Calendar.YEAR], mEndDate[Calendar.YEAR])
                }
                mYearPicker!!.setSelectedPosition(
                    mSelectedDate!![Calendar.YEAR] - mYearPicker!!.adapter!!.getItem(0)!!, false
                )
            }
            resetMonthAdapter(true)
        }
    }

    private fun resetMonthAdapter(isInit: Boolean) {
        // 1.根据当前选中的年份，以及起止时间，设置对应的月份。然后再设置对应的日
        if (hasType(TYPE_MONTH)) {
            val year =
                if (hasType(TYPE_YEAR)) mYearPicker!!.selectedItem!! else mSelectedDate!![Calendar.YEAR]
            val start: Int
            val end: Int
            // 这里要计算 selectedItem 而不是selectedPosition
            val last =
                if (isInit) mSelectedDate!![Calendar.MONTH] + 1 else mMonthPicker!!.selectedItem!!
            start = if (year == mStartYear) mStartMonth else 1
            end = if (year == mEndYear) mEndMonth else 12
            mMonthPicker!!.adapter = NumericWheelAdapter(start, end)
            // 2.设置选中的月份
            mMonthPicker!!.setSelectedPosition(last - mMonthPicker!!.adapter!!.getItem(0)!!, false)
        }

        // 3.月份要联动日
        resetDayAdapter(isInit)
    }

    private fun resetDayAdapter(isInit: Boolean) {
        if (hasType(TYPE_DAY)) {
            val year =
                if (hasType(TYPE_YEAR)) mYearPicker!!.selectedItem!! else mSelectedDate!![Calendar.YEAR]
            // 3.根据当前选中的年月设置日期。联动同月份。如果和起始或截止时同一年月，则比较对应日期
            // 有年，有日，则强制认为有月。
            val month =
                if (hasType(TYPE_MONTH)) mMonthPicker!!.selectedItem!! else mSelectedDate!![Calendar.MONTH] + 1
            val last =
                if (isInit) mSelectedDate!![Calendar.DAY_OF_MONTH] else mDayPicker!!.selectedItem!!
            val start = if (year == mStartYear && month == mStartMonth) mStartDay else 1
            val end =
                if (year == mEndYear && month == mEndMonth) mEndDay else getDayOfMonth(year, month)
            mDayPicker!!.adapter = NumericWheelAdapter(start, end)
            mDayPicker!!.setSelectedPosition(last - mDayPicker!!.adapter!!.getItem(0)!!, false)
        }
        resetNoonAdapter(isInit)
    }

    /**
     * 选择上下午的时候，如果选中的是起止时间，要重置下一级（hour or timeadapter)
     * 比如起始时间是 9:30，如果是上午，则hour是9-11，如果是下午则是0-11，非起止时间都是0-11
     * 如果结束时间
     */
    private fun resetNoonAdapter(isInit: Boolean) {
        if (hasType(TYPE_12_HOUR)) {
            val isSameStartDay = isSameDay(true)
            val isSameEndDay = isSameDay(false)
            val noons: MutableList<Int> = ArrayList()
            if (!isSameStartDay || mStartHour < 12) { // 如果是起始的那天，且时间>11点，则不包含上午
                noons.add(0)
            }
            if (!isSameEndDay || mEndHour >= 12) { // 如果是结束的那天，且时间<12点，则不包含下午
                noons.add(1)
            }
            val last: Int = if (isInit) {
                if (mSelectedDate!![Calendar.HOUR_OF_DAY] < 12) 0 else 1
            } else {
                mNoonPicker!!.selectedItem!!
            }
            mNoonPicker!!.adapter = ArrayWheelAdapter(noons)
            mNoonPicker!!.setSelectedPosition(last, false)
        }
        if (hasType(TYPE_MIXED_TIME)) {
            resetTimeAdapter(isInit)
        } else {
            // 日联动小时
            resetHourAdapter(isInit)
        }
    }

    private fun resetTimeAdapter(isInit: Boolean) { // 如果是聚合日期+12小时制+聚合时间就crash了。因为没有初始化上下午adapter
        val isSameStartDay = isSameDay(true)
        val isSameEndDay = isSameDay(false)
        val start: Int
        val end: Int
        if (!hasType(TYPE_12_HOUR)) {
            start = if (isSameStartDay) getValidTimeMinutes(mStartDate, true) else 0
            end = if (isSameEndDay) getValidTimeMinutes(
                mEndDate, false
            ) else getValidTimeMinutes(24 * 60 - mTimeMinuteOffset, false)
        } else {
            if (isSameStartDay) {
                // 如果起始时间是上午，并且选择的是下午，start=0,否则start=get12Hour(start)
                start = if (mStartHour < 12 && mNoonPicker!!.selectedItem == 1) {
                    0
                } else {
                    if (mStartHour >= 12) getValidTimeMinutes(
                        mStartDate, true
                    ) - 12 * 60 else getValidTimeMinutes(mStartDate, true)
                }
                end = if (isSameEndDay && mEndHour >= 12 && mNoonPicker!!.selectedItem == 1) {
                    // 如果 > 12 需要减去12小时
                    if (mEndHour >= 12) getValidTimeMinutes(
                        mEndDate, false
                    ) - 12 * 60 else getValidTimeMinutes(
                        mEndDate, false
                    )
                } else {
                    getValidTimeMinutes(12 * 60 - mTimeMinuteOffset, false)
                }
            } else if (isSameEndDay) {
                start = 0
                end = if (mEndHour >= 12 && mNoonPicker!!.selectedItem == 1) {
                    // 如果 > 12 需要减去12小时
                    if (mEndHour >= 12) getValidTimeMinutes(
                        mEndDate, false
                    ) - 12 * 60 else getValidTimeMinutes(
                        mEndDate, false
                    )
                } else {
                    getValidTimeMinutes(12 * 60 - mTimeMinuteOffset, false)
                }
            } else {
                start = 0
                end = getValidTimeMinutes(12 * 60 - mTimeMinuteOffset, false)
            }
        }
        val last: Int = if (isInit) {
            if (hasType(TYPE_12_HOUR)) {
                val timeMinutes = getValidTimeMinutes(mSelectedDate, true)
                if (timeMinutes >= 12 * 60) getValidTimeMinutes(
                    mSelectedDate, true
                ) - 12 * 60 else getValidTimeMinutes(mSelectedDate, true)
            } else {
                getValidTimeMinutes(mSelectedDate, true)
            }
        } else {
            mTimePicker!!.selectedItem!! * mTimeMinuteOffset
        }
        //  adapter 的item设置的是 有效分钟数/mTimeMinuteOffset
        mTimePicker!!.adapter =
            NumericWheelAdapter(getValidTimesValue(start), getValidTimesValue(end))
        mTimePicker!!.setSelectedPosition(findPositionByValidTimes(last), false)
    }

    private fun resetHourAdapter(isInit: Boolean) {
        if (hasType(TYPE_HOUR)) {
            val isSameStartDay = isSameDay(true)
            val isSameEndDay = isSameDay(false)
            val start: Int
            val end: Int
            if (!hasType(TYPE_12_HOUR)) {
                start = if (isSameStartDay) mStartHour else 0
                end = if (isSameEndDay) mEndHour else 23
            } else {
                if (isSameStartDay) {
                    // 如果起始时间是上午，并且选择的是下午，start=0,否则start=get12Hour(start)
                    start = if (mStartHour < 12 && mNoonPicker!!.selectedItem == 1) {
                        0
                    } else {
                        get12Hour(mStartHour)
                    }
                    end =
                        if (isSameEndDay && mEndHour >= 12 && mNoonPicker!!.selectedItem == 1) { // 如果开始和结束时间是同一天
                            get12Hour(mEndHour)
                        } else {
                            11
                        }
                } else if (isSameEndDay) {
                    start = 0
                    // 如果截止时间是下午，如果选择的是上午，end=11，如果选择的下午，end=get12Hour(mEndHour)
                    // 如果截止时间是上午，选择的是上午，end=get12Hour
                    end = if (mEndHour >= 12 && mNoonPicker!!.selectedItem == 1) {
                        get12Hour(mEndHour)
                    } else {
                        11
                    }
                } else {
                    start = 0
                    end = 11
                }
            }
            val last: Int = if (isInit) {
                if (hasType(TYPE_12_HOUR)) {
                    get12Hour(mSelectedDate!![Calendar.HOUR_OF_DAY])
                } else {
                    mSelectedDate!![Calendar.HOUR_OF_DAY]
                }
            } else {
                mHourPicker!!.selectedItem!!
            }
            mHourPicker!!.adapter = NumericWheelAdapter(start, end)
            mHourPicker!!.setSelectedPosition(last - mHourPicker!!.adapter!!.getItem(0)!!, false)
        }
        resetMinuteAdapter(isInit)
    }

    private fun get12Hour(hour: Int): Int {
        return if (hour >= 12) {
            hour - 12
        } else hour
    }

    private fun isSameDay(isStart: Boolean): Boolean {
        return if (hasType(TYPE_MIXED_DATE)) {
            if (isStart) {
                getDayOffset(selectedDate.time, mStartDate.timeInMillis) == 0
            } else {
                getDayOffset(selectedDate.time, mEndDate.timeInMillis) == 0
            }
        } else {
            val year =
                if (hasType(TYPE_YEAR)) mYearPicker!!.selectedItem!! else mSelectedDate!![Calendar.YEAR]
            val month =
                if (hasType(TYPE_MONTH)) mMonthPicker!!.selectedItem!! else mSelectedDate!![Calendar.MONTH] + 1
            val day =
                if (hasType(TYPE_DAY)) mDayPicker!!.selectedItem!! else mSelectedDate!![Calendar.DAY_OF_MONTH]
            if (isStart) {
                year == mStartYear && month == mStartMonth && day == mStartDay
            } else {
                year == mEndYear && month == mEndMonth && day == mEndDay
            }
        }
    }

    private fun resetMinuteAdapter(isInit: Boolean) {
        if (hasType(TYPE_MINUTE)) {
            val isSameStartDay: Boolean
            val isSameEndDay: Boolean
            if (hasType(TYPE_MIXED_DATE)) {
                isSameStartDay = getDayOffset(selectedDate.time, mStartDate.timeInMillis) == 0
                isSameEndDay = getDayOffset(selectedDate.time, mEndDate.timeInMillis) == 0
            } else {
                val year =
                    if (hasType(TYPE_YEAR)) mYearPicker!!.selectedItem!! else mSelectedDate!![Calendar.YEAR]
                val month =
                    if (hasType(TYPE_MONTH)) mMonthPicker!!.selectedItem!! else mSelectedDate!![Calendar.MONTH] + 1
                val day =
                    if (hasType(TYPE_DAY)) mDayPicker!!.selectedItem!! else mSelectedDate!![Calendar.DAY_OF_MONTH]
                isSameStartDay = year == mStartYear && month == mStartMonth && day == mStartDay
                isSameEndDay = year == mEndYear && month == mEndMonth && day == mEndDay
            }
            val hour: Int = if (hasType(TYPE_HOUR)) {
                if (hasType(TYPE_12_HOUR) && mNoonPicker!!.selectedItem == 1) {
                    mHourPicker!!.selectedItem!! + 12
                } else {
                    mHourPicker!!.selectedItem!!
                }
            } else {
                mSelectedDate!![Calendar.HOUR_OF_DAY]
            }
            val last = if (isInit) mSelectedDate!![Calendar.MINUTE] else getRealMinute(
                mMinutePicker!!.selectedPosition
            )
            val start = if (isSameStartDay && hour == mStartHour) mStartMinute else 0
            val end = if (isSameEndDay && hour == mEndHour) mEndMinute else 60 - mTimeMinuteOffset
            mMinutePicker!!.adapter =
                NumericWheelAdapter(getValidMinuteValue(start), getValidMinuteValue(end))
            mMinutePicker!!.setSelectedPosition(findPositionByValidTimes(last), false)
        }
    }

    // 获取有效分钟数对应的item的数值
    private fun getValidMinuteValue(validTimeMinutes: Int): Int {
        return validTimeMinutes / mTimeMinuteOffset
    }

    // 通过有效分钟数找到在adapter中的position
    private fun findPositionByValidTimes(validTimeMinutes: Int): Int {
        val timesValue = getValidMinuteValue(validTimeMinutes)
        return if (mMinutePicker != null) {
            timesValue - mMinutePicker!!.adapter!!.getItem(0)!!
        } else timesValue - mTimePicker!!.adapter!!.getItem(0)!!
    }

    /**
     * 获取对应position的真实分钟数，注意这里必须使用position
     *
     * @param position [BasePickerView.getSelectedPosition]
     */
    // 获指定position的分钟item对应的真实的分钟数
    private fun getRealMinute(position: Int): Int {
        // bugfix:这个position是下标，要拿对应item的数值来计算
        return mMinutePicker!!.adapter!!.getItem(position)!! * mTimeMinuteOffset
    }

    // 获取指定position对应的有效的分钟数
    private fun getPositionValidMinutes(position: Int): Int {
        return mTimePicker!!.adapter!!.getItem(position)!! * mTimeMinuteOffset
    }

    // 获取有效分钟数对应的item的数值
    private fun getValidTimesValue(validTimeMinutes: Int): Int {
        return validTimeMinutes / mTimeMinuteOffset
    }

    /**
     * 获取根据mTimeMinuteOffset处理后的有效分钟数
     * 默认为 start <= X <= end 即都不包含在内
     */
    private fun getValidTimeMinutes(timeMinutes: Int, isStart: Boolean): Int {
        var validTimeMinutes: Int
        val offset = timeMinutes % mTimeMinuteOffset
        if (offset == 0) {
            validTimeMinutes = timeMinutes
        } else {
            if (isStart) {
                validTimeMinutes = timeMinutes - offset
                if (!mContainsStarDate) {
                    validTimeMinutes += mTimeMinuteOffset
                }
            } else {
                validTimeMinutes = timeMinutes - offset
                if (mContainsEndDate) {
                    validTimeMinutes += mTimeMinuteOffset
                }
            }
        }
        return validTimeMinutes
    }

    /**
     * 获取时间的分钟数
     */
    private fun getValidTimeMinutes(calendar: Calendar?, isStart: Boolean): Int {
        if (calendar == null) return 0
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val minutes = hour * 60 + minute
        return getValidTimeMinutes(minutes, isStart)
    }

    /**
     * 获取指定日期距离第0个的offset
     */
    private fun offsetStart(calendar: Calendar): Int {
        return getDayOffset(calendar.timeInMillis, mStartDate.timeInMillis)
    }

    private val selectedDate: Date
        get() = getPositionDate(mDatePicker!!.selectedPosition)

    // 获取对应position的日期
    private fun getPositionDate(position: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = mStartDate.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, position)
        return calendar.time
    }

    // 获取对应position的时间
    private fun getPositionTime(position: Int): Date {
        val calendar = Calendar.getInstance()
        // 计算出position对应的hour & minute
        val minutes = mTimePicker!!.adapter!!.getItem(position)!! * mTimeMinuteOffset
        val hour = minutes / 60
        val minute = minutes % 60
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = minute
        return calendar.time
    }

    override fun onSelected(pickerView: BasePickerView<*>, position: Int) {
        // 联动，年份、月份是固定的，使用日历，获取指定指定某年某月的日期
        when (pickerView.tag as Int) {
            TYPE_YEAR -> resetMonthAdapter(false)
            TYPE_MONTH -> resetDayAdapter(false)
            TYPE_MIXED_DATE, TYPE_DAY -> resetNoonAdapter(false)
            TYPE_12_HOUR -> if (hasType(TYPE_MIXED_TIME)) {
                resetTimeAdapter(false)
            } else {
                resetHourAdapter(false)
            }

            TYPE_HOUR -> resetMinuteAdapter(false)
        }
        if (!needDialog){
            onConfirm()
        }
    }

    override fun onConfirm() {
        onTimeSelectListener.onTimeSelect(this, selectedDates)
    }

    /**
     * @param position 这个是adapter的position，但是起始时间如果不从0开始就不对了
     */
    override fun format(
        pickerView: BasePickerView<*>, position: Int, charSequence: CharSequence?
    ): CharSequence? {
        if (formatter == null) return charSequence
        val type = pickerView.tag as Int
        val value: Long = when (type) {
            TYPE_MIXED_DATE -> {
                getPositionDate(position).time
            }

            TYPE_MIXED_TIME -> {
                getPositionTime(position).time
            }

            TYPE_MINUTE -> {
                getRealMinute(position).toLong()
            }

            else -> {
                charSequence.toString().toInt().toLong()
            }
        }
        return formatter!!.format(this, type, position, value)
    }

    /**
     * 强制设置的属性直接在构造方法中设置
     *
     * @param listener listener
     */
    class Builder(
        private val context: Context,
        private val type: Int,
        private val onTimeSelectListener: OnTimeSelectListener
    ) {
        // 都应该设置起止时间的，哪怕是只有时间格式，因为真实回调的是时间戳
        private var mStartDate: Long = 0 // 默认起始为1970/1/1 8:0:0
        private var mEndDate = 4133865600000L // 默认截止为2100/12/31 0:0:0
        private var mSelectedDate: Long = -1
        private var mFormatter: Formatter? = null
        private var mInterceptor: Interceptor? = null

        // 时间分钟间隔
        private var mTimeMinuteOffset = 1

        // 设置mTimeMinuteOffset时，是否包含起止时间
        private var mContainsStarDate = false
        private var mContainsEndDate = false
        private var needDialog = true
        private var iPickerDialog: IPickerDialog? = null

        /**
         * 设置起止时间
         *
         * @param startDate 起始时间
         * @param endDate 截止时间
         */
        fun setRangDate(startDate: Long, endDate: Long): Builder {
            mEndDate = endDate
            mStartDate = if (endDate < startDate) {
                endDate
            } else {
                startDate
            }
            return this
        }

        /**
         * 设置选中时间戳
         *
         * @param millis 选中时间戳
         */
        fun setSelectedDate(millis: Long): Builder {
            mSelectedDate = millis
            return this
        }

        /**
         * 设置时间间隔分钟数，以0为起始边界
         *
         * @param timeMinuteOffset 60%offset==0才有效
         */
        fun setTimeMinuteOffset(timeMinuteOffset: Int): Builder {
            mTimeMinuteOffset = timeMinuteOffset
            return this
        }

        /**
         * 设置mTimeMinuteOffset作用时，是否包含超出的startDate
         *
         * @param containsStarDate 是否包含startDate
         */
        fun setContainsStarDate(containsStarDate: Boolean): Builder {
            mContainsStarDate = containsStarDate
            return this
        }

        /**
         * 设置mTimeMinuteOffset作用时，是否包含超出的endDate
         *
         * @param containsEndDate 是否包含endDate
         */
        fun setContainsEndDate(containsEndDate: Boolean): Builder {
            mContainsEndDate = containsEndDate
            return this
        }

        fun setFormatter(formatter: Formatter?): Builder {
            mFormatter = formatter
            return this
        }

        fun setInterceptor(interceptor: Interceptor?): Builder {
            mInterceptor = interceptor
            return this
        }

        /**
         * 自定义弹窗
         *
         * @param iPickerDialog 如果为null表示不需要弹窗
         */
        fun dialog(iPickerDialog: IPickerDialog?): Builder {
            needDialog = iPickerDialog != null
            this.iPickerDialog = iPickerDialog
            return this
        }

        fun create(): TimePicker {
            val picker = TimePicker(context, type, onTimeSelectListener)
            // 不支持重复设置的，都在builder中控制，一次性行为
            picker.needDialog = needDialog
            picker.iPickerDialog = iPickerDialog
            picker.initPickerView()
            picker.setInterceptor(mInterceptor)
            picker.mTimeMinuteOffset = mTimeMinuteOffset
            picker.mContainsStarDate = mContainsStarDate
            picker.mContainsEndDate = mContainsEndDate
            picker.setRangDate(mStartDate, mEndDate)
            if (mFormatter == null) {
                mFormatter = DefaultFormatter()
            }
            picker.formatter = mFormatter
            picker.initPicker()
            if (mSelectedDate < 0) {
                picker.reset()
            } else {
                picker.setSelectedDate(mSelectedDate)
            }
            return picker
        }
    }

    open class DefaultFormatter : Formatter {
        override fun format(
            picker: TimePicker, type: Int, position: Int, value: Long
        ): CharSequence {
            when (type) {
                TYPE_YEAR -> {
                    return value.toString() + "年"
                }

                TYPE_MONTH -> {
                    return String.format("%02d月", value)
                }

                TYPE_DAY -> {
                    return String.format("%02d日", value)
                }

                TYPE_12_HOUR -> {
                    return if (value == 0L) "上午" else "下午"
                }

                TYPE_HOUR -> {
                    if (picker.hasType(TYPE_12_HOUR)) {
                        if (value == 0L) {
                            return "12时"
                        }
                    }
                    return String.format("%2d时", value)
                }

                TYPE_MINUTE -> {
                    return String.format("%2d分", value)
                }

                TYPE_MIXED_DATE -> {
                    // 如果是TYPE_MIXED_,则value表示时间戳
                    return sDefaultDateFormat.format(Date(value))
                }

                TYPE_MIXED_TIME -> {
                    val time = sDefaultTimeFormat.format(Date(value))
                    return if (picker.hasType(TYPE_12_HOUR)) {
                        time.replace("00:", "12:") // 12小时
                    } else {
                        time
                    }
                }

                else -> return value.toString()
            }
        }
    }

    fun interface Formatter {
        /**
         * 根据type和num格式化时间
         *
         * @param picker picker
         * @param type 并不是模式，而是当前item所属的type，如年，时
         * @param position position
         * @param value position item对应的value，如果是TYPE_MIXED_DATE表示日期时间戳，否则表示显示的数字
         */
        fun format(
            picker: TimePicker, type: Int, position: Int, value: Long
        ): CharSequence
    }

    fun interface OnTimeSelectListener {
        /**
         * 点击确定按钮选择时间后回调
         *
         * @param date 选择的时间
         */
        fun onTimeSelect(picker: TimePicker, date: Date)
    }

    companion object {
        const val TYPE_YEAR = 0x01
        const val TYPE_MONTH = 0x02
        const val TYPE_DAY = 0x04
        const val TYPE_HOUR = 0x08
        const val TYPE_MINUTE = 0x10

        /** 日期聚合  */
        const val TYPE_MIXED_DATE = 0x20

        /** 时间聚合  */
        const val TYPE_MIXED_TIME = 0x40

        /** 上午、下午（12小时制,默认24小时制，不显示上午，下午）  */
        const val TYPE_12_HOUR = 0x80

        // 日期：年月日
        const val TYPE_DATE = TYPE_YEAR or TYPE_MONTH or TYPE_DAY

        // 时间：小时、分钟
        const val TYPE_TIME = TYPE_HOUR or TYPE_MINUTE

        // 全部
        const val TYPE_ALL = TYPE_DATE or TYPE_TIME

        var sDefaultDateFormat: DateFormat = createDateFormat("yyyy年MM月dd日")
        var sDefaultTimeFormat: DateFormat = createDateFormat("HH:mm")
    }
}