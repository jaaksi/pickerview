package org.jaaksi.pickerview.demo.ui

import android.os.Bundle
import android.view.View
import org.jaaksi.pickerview.dataset.OptionDataSet
import org.jaaksi.pickerview.demo.base.BaseFragment
import org.jaaksi.pickerview.demo.databinding.FragmentOptionPickerBinding
import org.jaaksi.pickerview.demo.model.City
import org.jaaksi.pickerview.demo.model.Province
import org.jaaksi.pickerview.demo.picker.PickerDialog
import org.jaaksi.pickerview.demo.util.DataParseUtil
import org.jaaksi.pickerview.picker.BasePicker
import org.jaaksi.pickerview.picker.OptionPicker
import org.jaaksi.pickerview.picker.OptionPicker.OnOptionSelectListener
import org.jaaksi.pickerview.widget.DefaultCenterDecoration

/**
 * OptionPicker 实现 [省、市、区/县 三级联动]
 */
class OptionPickerFragment : BaseFragment<FragmentOptionPickerBinding>(), View.OnClickListener,
    OnOptionSelectListener {
    private lateinit var mPicker: OptionPicker

    private var selectedProvince = "河南省"
    private var selectedCity = "郑州市"
    private var selectedDistrict = "金水区"

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.btnShow.setOnClickListener(this)
        val centerDecoration = DefaultCenterDecoration(requireContext()).apply {
            setMargin(null)
        }
        mPicker = OptionPicker.Builder(requireContext(), 3, this)
            .setInterceptor { pickerView, params ->
                // 自定义装饰线
                pickerView.setCenterDecoration(centerDecoration)
            }
            .create()
        (mPicker.dialog() as? PickerDialog)?.binding?.run {
            tvTitle.text = "选择地区"
        }
        mPicker.setData(createData())
        binding.btnShow.text = "$selectedProvince $selectedCity $selectedDistrict"

    }

    private fun createData(): List<Province> {
        val json =
            requireContext().assets.open("citys2.json").bufferedReader().use { it.readText() }
        return DataParseUtil.fromJsonArray<List<Province>>(json)!!
    }

    override fun onClick(v: View) {
        // 直接传入选中的值
        mPicker.setSelectedWithValues(selectedProvince, selectedCity, selectedDistrict)
        mPicker.show()
    }

    override fun onOptionSelect(
        picker: OptionPicker, selectedPosition: IntArray,
        selectedOptions: Array<OptionDataSet?>
    ) {
        selectedProvince = (selectedOptions[0] as Province).name
        selectedCity = (selectedOptions[1] as City).name
        selectedDistrict = selectedOptions[2]!!.getCharSequence().toString()
        binding.btnShow.text = "$selectedProvince $selectedCity $selectedDistrict"
    }

}