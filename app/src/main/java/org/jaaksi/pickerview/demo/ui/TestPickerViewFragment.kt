package org.jaaksi.pickerview.demo.ui

import android.graphics.Color
import android.os.Bundle
import androidx.core.view.postDelayed
import org.jaaksi.pickerview.adapter.NumericWheelAdapter
import org.jaaksi.pickerview.demo.base.BaseFragment
import org.jaaksi.pickerview.demo.databinding.FragmentTestPickerBinding
import org.jaaksi.pickerview.util.Util
import org.jaaksi.pickerview.widget.BasePickerView
import org.jaaksi.pickerview.widget.DefaultCenterDecoration
import org.jaaksi.pickerview.widget.PickerView

/**
 * Created by fuchaoyang on 2018/2/5.<br></br>
 * description：
 */
class TestPickerViewFragment : BaseFragment<FragmentTestPickerBinding>() {
    private lateinit var mPickerView: PickerView<Int>

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.pickerview.adapter = NumericWheelAdapter(1, 10)
        binding.pickerview.apply {
            // 覆盖xml中的水平方向
            isHorizontal = false
            setTextSize(15, 22)
            setIsCirculation(true)
            //setAlignment(Layout.Alignment.ALIGN_CENTER);
            isCanTap = false
            isDisallowInterceptTouch = false
            // 覆盖xml设置的7
            visibleItemCount = 5
            itemSize = 50
            // 格式化内容
            formatter =
                BasePickerView.Formatter { pickerView, position, charSequence -> charSequence.toString() + "万年" }

            val margin = Util.dip2px(requireContext(), 5f)
            val centerDecoration =
                DefaultCenterDecoration(requireContext()).setLineColor(Color.GREEN)
                    .setMargin(margin, -margin, margin, -margin).setLineWidth(3f)
                    .setDrawable(Color.RED)
            setCenterDecoration(centerDecoration)
            // 设置centerPosition
            postDelayed(1000) {
                centerPosition = 1
            }
        }

    }

}