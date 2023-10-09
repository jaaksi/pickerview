package org.jaaksi.pickerview.demo.ui

import android.os.Bundle
import org.jaaksi.pickerview.demo.base.BaseFragment
import org.jaaksi.pickerview.demo.databinding.FragmentMixedtimeFormatBinding
import org.jaaksi.pickerview.demo.picker.PickerDialog
import org.jaaksi.pickerview.picker.TimePicker
import org.jaaksi.pickerview.picker.TimePicker.OnTimeSelectListener
import org.jaaksi.pickerview.util.DateUtil
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 创建时间：2018年01月31日15:49 <br></br>
 * 作者：fuchaoyang <br></br>
 * 描述：自定义日期格式 如显示星期几
 */
class MixedTimeFormatFragment : BaseFragment<FragmentMixedtimeFormatBinding>(),
    OnTimeSelectListener {
    private lateinit var mTimePicker: TimePicker

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.btnShow.setOnClickListener {
            try {
                val date = sSimpleDateFormat.parse(
                    binding.btnShow.text.toString()
                )
                mTimePicker.setSelectedDate(date.time)
            } catch (e: ParseException) {
                mTimePicker.setSelectedDate(System.currentTimeMillis())
                e.printStackTrace()
            }
            mTimePicker.show()
        }

        mTimePicker = TimePicker.Builder(
            requireActivity(), TimePicker.TYPE_MIXED_DATE or TimePicker.TYPE_MIXED_TIME,
            this
        ) // 设置不包含超出的结束时间<=
            .setContainsEndDate(false) // 设置时间间隔为30分钟
            .setTimeMinuteOffset(30).setRangDate(1517771651000L, 1577976666000L)
            .setFormatter(object : TimePicker.DefaultFormatter() {
                override fun format(
                    picker: TimePicker,
                    type: Int,
                    position: Int,
                    value: Long
                ): CharSequence {
                    if (type == TimePicker.TYPE_MIXED_DATE) {
                        val text: CharSequence
                        val dayOffset = DateUtil.getDayOffset(value, System.currentTimeMillis())
                        text = when (dayOffset) {
                            0 -> "今天"

                            1 -> "明天"

                            else -> { // xx月xx日 星期 x
                                mDateFormat.format(value)
                            }
                        }
                        return text
                    }
                    return super.format(picker, type, position, value)
                }
            })
            .create()
        val dialog = mTimePicker.dialog() as PickerDialog?
        dialog?.binding?.tvTitle?.text = "请选择时间"
    }

    override fun onTimeSelect(picker: TimePicker, date: Date) {
        binding.btnShow.text = sSimpleDateFormat.format(date)
    }


    companion object {
        val sSimpleDateFormat: DateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
        val mDateFormat = SimpleDateFormat("MM月dd日  E", Locale.CHINA)
    }
}