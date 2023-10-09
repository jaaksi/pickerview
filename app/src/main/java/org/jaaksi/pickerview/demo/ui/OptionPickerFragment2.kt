package org.jaaksi.pickerview.demo.ui

import android.os.Bundle
import android.view.View
import org.jaaksi.pickerview.dataset.OptionDataSet
import org.jaaksi.pickerview.demo.base.BaseFragment
import org.jaaksi.pickerview.demo.databinding.FragmentOptionPickerBinding
import org.jaaksi.pickerview.demo.picker.PickerDialog
import org.jaaksi.pickerview.picker.OptionPicker
import org.jaaksi.pickerview.picker.OptionPicker.OnOptionSelectListener

/**
 * OptionPicker 实现 [非联动多级列表]
 */
class OptionPickerFragment2 : BaseFragment<FragmentOptionPickerBinding>(), View.OnClickListener,
    OnOptionSelectListener {
    private lateinit var mPicker: OptionPicker

    private var selectedOption1 = "PM"
    private var selectedOption2 = "10"

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.btnShow.setOnClickListener(this)
        mPicker = OptionPicker.Builder(requireContext(), 2, this).create()
        (mPicker.dialog() as? PickerDialog)?.binding?.run {
            tvTitle.text = "请选择时间"
        }
        val list1 = listOf("AM", "PM").map { OptionsInfo(it) }
        val list2 = (0..11).map { OptionsInfo("$it") }
        mPicker.setData(list1, list2)
        binding.btnShow.text = "$selectedOption1 $selectedOption2"

    }

    override fun onClick(v: View) {
        // 直接传入选中的值
        mPicker.setSelectedWithValues(selectedOption1, selectedOption2)
        mPicker.show()
    }

    override fun onOptionSelect(
        picker: OptionPicker, selectedPosition: IntArray,
        selectedOptions: Array<OptionDataSet?>
    ) {
        selectedOption1 = selectedOptions[0]?.getValue() ?: ""
        selectedOption2 = selectedOptions[1]?.getValue() ?: ""
        binding.btnShow.text = "$selectedOption1 $selectedOption2"
    }

    data class OptionsInfo(val name: String) : OptionDataSet {
        // 非关联的只有一级
        override fun getSubs() = null

        override fun getCharSequence() = name

        override fun getValue() = name
    }

}