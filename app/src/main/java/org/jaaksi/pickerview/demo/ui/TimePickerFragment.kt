package org.jaaksi.pickerview.demo.ui

import android.os.Bundle
import android.widget.CompoundButton
import org.jaaksi.pickerview.demo.base.BaseFragment
import org.jaaksi.pickerview.demo.databinding.FragmentTimepickerBinding
import org.jaaksi.pickerview.demo.picker.PickerDialog
import org.jaaksi.pickerview.picker.TimePicker
import org.jaaksi.pickerview.picker.TimePicker.OnTimeSelectListener
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 创建时间：2018年01月31日15:49 <br></br>
 * 作者：fuchaoyang <br></br>
 * 描述：强大的type模式自由组合（当然应该是有意义的）
 */
class TimePickerFragment : BaseFragment<FragmentTimepickerBinding>(), OnTimeSelectListener,
    CompoundButton.OnCheckedChangeListener {

    companion object {
        val sSimpleDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    }

    private var timePicker: TimePicker? = null
    private var currYear = 0
    private var selectedTime = System.currentTimeMillis()
    private val cbList by lazy {
        listOf(
            binding.cbDate,
            binding.cbTime,
            binding.cbYear,
            binding.cbMonth,
            binding.cbDay,
            binding.cbHour,
            binding.cbMinute,
            binding.cbNoon
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val calendar = Calendar.getInstance()
        currYear = calendar[Calendar.YEAR]
        binding.run {
            cbList.forEach {
                it.setOnCheckedChangeListener(this@TimePickerFragment)
            }
            btnShow.text = sSimpleDateFormat.format(selectedTime)

            btnShow.setOnClickListener {
                if (timePicker == null) {
                    reset()
                }
                try {
                    // 设置选中时间
                    val date = sSimpleDateFormat.parse(
                        btnShow.text.toString()
                    )
                    timePicker!!.setSelectedDate(date.time)
                } catch (e: ParseException) {
                    // 如果没有设置选中时间，则取起始时间
                    e.printStackTrace()
                    //mTimePicker.setSelectedDate(mLoveTimes);
                }
                timePicker!!.show()
            }
        }
    }

    override fun onTimeSelect(picker: TimePicker, date: Date) {
        selectedTime = date.time
        binding.btnShow.text = sSimpleDateFormat.format(date)
    }

    private fun reset() {
        var type = 0
        // 设置type
        if (binding.cbYear.isChecked || binding.cbMonth.isChecked || binding.cbDay.isChecked) {
            binding.cbDate.isChecked = false
        }
        if (binding.cbHour.isChecked || binding.cbMinute.isChecked) {
            binding.cbTime.isChecked = false
        }
        if (binding.cbDate.isChecked) {
            type = type or TimePicker.TYPE_MIXED_DATE
        } else {
            if (binding.cbYear.isChecked) type = type or TimePicker.TYPE_YEAR
            if (binding.cbMonth.isChecked) type = type or TimePicker.TYPE_MONTH
            if (binding.cbDay.isChecked) type = type or TimePicker.TYPE_DAY
        }
        if (binding.cbNoon.isChecked) type = type or TimePicker.TYPE_12_HOUR
        if (binding.cbTime.isChecked) {
            type = type or TimePicker.TYPE_MIXED_TIME
            binding.cbHour.isChecked = false
            binding.cbMinute.isChecked = false
        } else {
            if (binding.cbHour.isChecked) type = type or TimePicker.TYPE_HOUR
            if (binding.cbMinute.isChecked) type = type or TimePicker.TYPE_MINUTE
        }

        // 2018/10/24 14:16:0 - 2030/1/2 13:51:0
        timePicker = TimePicker.Builder(requireContext(), type, this) // 设置时间区间
            .setRangDate(1540361760000L, System.currentTimeMillis()) //.setContainsStarDate(true)
            //.setTimeMinuteOffset(10)
            // 设置选中时间
            .setSelectedDate(selectedTime)
            // 设置pickerview样式
            .setInterceptor { pickerView, params ->
                pickerView.visibleItemCount = 5
                // 将年月设置为循环的
                val type = pickerView.tag as Int
                //if (type == TimePicker.TYPE_YEAR || type == TimePicker.TYPE_MONTH) {
                //  pickerView.setIsCirculation(true);
                //}
            }
            .setFormatter(object : TimePicker.DefaultFormatter() { // 设置 Formatter
                // 自定义Formatter显示去年，今年，明年
                override fun format(
                    picker: TimePicker,
                    type: Int,
                    position: Int,
                    value: Long
                ): CharSequence {
                    if (type == TimePicker.TYPE_YEAR) {
                        val offset = value - currYear
                        if (offset == -1L) return "去年"
                        if (offset == 0L) return "今年"
                        return if (offset == 1L) "明年" else value.toString() + "年"
                    } else if (type == TimePicker.TYPE_MONTH) {
                        return String.format("%d月", value)
                    }
                    return super.format(picker, type, position, value)
                }
            }).create()
        val dialog = timePicker?.dialog() as PickerDialog?
        dialog?.binding?.tvTitle?.text = "请选择时间"
        // 2019/2/5 14:57:23
        //mTimePicker.setSelectedDate(1526449500000L);
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView == binding.cbDate && buttonView.isChecked) {
            binding.cbYear.isChecked = false
            binding.cbMonth.isChecked = false
            binding.cbDay.isChecked = false
        }

        if (buttonView == binding.cbTime && buttonView.isChecked) {
            binding.cbHour.isChecked = false
            binding.cbMinute.isChecked = false
        }
        if (buttonView.isPressed) {
            reset()
        }

    }

}