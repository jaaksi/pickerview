package org.jaaksi.pickerview.demo.ui

import android.os.Bundle
import androidx.core.graphics.toColorInt
import org.jaaksi.pickerview.adapter.NumericWheelAdapter
import org.jaaksi.pickerview.demo.base.BaseFragment
import org.jaaksi.pickerview.demo.databinding.FragmentTestPickerBinding
import org.jaaksi.pickerview.picker.TimePicker
import org.jaaksi.pickerview.util.DateUtil
import org.jaaksi.pickerview.util.Util
import org.jaaksi.pickerview.widget.BasePickerView
import org.jaaksi.pickerview.widget.DefaultCenterDecoration

/**
 * Created by fuchaoyang on 2018/2/5.<br></br>
 * description：
 */
class TestPickerViewFragment : BaseFragment<FragmentTestPickerBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.pickerview.adapter = NumericWheelAdapter(1, 9)
        binding.pickerview.apply {
            // 覆盖xml中的水平方向
            isHorizontal = false
            setTextSize(18, 20)
            setIsCirculation(true)
            //setAlignment(Layout.Alignment.ALIGN_CENTER);
            isCanTap = false
            isDisallowInterceptTouch = false
            // 覆盖xml设置的7
            visibleItemCount = 5
            // 格式化内容
            formatter =
                BasePickerView.Formatter { pickerView, position, charSequence -> charSequence.toString() + "years" }

            val margin = Util.dip2px(requireContext(), 5f)
            val centerDecoration =
                DefaultCenterDecoration(requireContext())
                    .setLineColor("#1A0000FF".toColorInt())
                    //.setMargin(margin, -margin, margin, -margin)
                    .setLineWidth(3f)
                    .setDrawable("#1AF84055".toColorInt())
            setCenterDecoration(centerDecoration)
            // 设置centerPosition
            //postDelayed(1000) {
            //    centerPosition = 1
            //}

            // TimePicker用在View中
            TimePicker.Builder(
                requireContext(), TimePicker.TYPE_ALL
            ) { picker, date ->
                binding.tvTime.text = DateUtil.createDateFormat("yyyy年MM月dd日 HH:mm").format(date)
            } // 设置时间区间
                .dialog(null)
                .setRangDate(
                    1540361760000L,
                    System.currentTimeMillis()
                ) //.setContainsStarDate(true)
                //.setTimeMinuteOffset(10)
                // 设置选中时间
                .setSelectedDate(System.currentTimeMillis())
                // 设置pickerview样式
                .setInterceptor { pickerView, params ->
                    pickerView.visibleItemCount = 5
                    // 将年月设置为循环的
                    val type = pickerView.tag as Int
                    //if (type == TimePicker.TYPE_YEAR || type == TimePicker.TYPE_MONTH) {
                    //  pickerView.setIsCirculation(true);
                    //}
                }.create().view().let {
                    binding.timepickerLayout.addView(it)
                }
        }

    }

}