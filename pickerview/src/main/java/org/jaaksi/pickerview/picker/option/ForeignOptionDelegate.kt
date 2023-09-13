package org.jaaksi.pickerview.picker.option

import org.jaaksi.pickerview.adapter.ArrayWheelAdapter
import org.jaaksi.pickerview.dataset.OptionDataSet
import org.jaaksi.pickerview.picker.OptionPicker

/**
 * Created by fuchaoyang on 2018/7/6.<br></br>
 * description：无关联的 OptionPicker Delegate
 */
class ForeignOptionDelegate : IOptionDelegate {
    private var mDelegate: OptionPicker.Delegate? = null
    private var mOptions: Array<out List<OptionDataSet>>? = null
    override fun init(delegate: OptionPicker.Delegate) {
        mDelegate = delegate
    }

    override fun setData(vararg options: List<OptionDataSet>) {
        mOptions = options
        for (i in 0 until mDelegate!!.hierarchy) {
            val pickerView = mDelegate!!.getPickerViews()[i]
            pickerView.adapter = ArrayWheelAdapter(mOptions!![i])
        }
    }

    override fun setSelectedWithValues(vararg values: String?) {
        for (i in 0 until mDelegate!!.hierarchy) {
            if (mOptions == null || mOptions!!.isEmpty()) { // 数据源无效
                mDelegate!!.selectedPosition[i] = -1
            } else if (values.size <= i || values[i] == null) { // 选中默认项0...
                mDelegate!!.selectedPosition[i] = 0
            } else {
                val options = mOptions!![i]
                for (j in 0..options.size) {
                    // 遍历找到选中的下标，如果没有找到，则将下标置为0
                    if (j == options.size) {
                        mDelegate!!.selectedPosition[i] = 0
                        break
                    }
                    if (values[i] == options[j].getValue()) {
                        mDelegate!!.selectedPosition[i] = j
                        break
                    }
                }
            }
            if (mDelegate!!.selectedPosition[i] != -1) {
                mDelegate!!.getPickerViews()[i]
                    .setSelectedPosition(mDelegate!!.selectedPosition[i], false)
            }
        }
    }

    override val selectedOptions: Array<OptionDataSet?>
        get() {
            val optionDataSets = arrayOfNulls<OptionDataSet>(
                mDelegate!!.hierarchy
            )
            for (i in 0 until mDelegate!!.hierarchy) {
                val selectedPosition = mDelegate!!.selectedPosition[i]
                if (selectedPosition == -1) break
                optionDataSets[i] = mOptions!![i]!![selectedPosition]
            }
            return optionDataSets
        }

    override fun reset() {
        for (i in 0 until mDelegate!!.hierarchy) {
            val pickerView = mDelegate!!.getPickerViews()[i]
            pickerView.setSelectedPosition(mDelegate!!.selectedPosition[i], false)
        }
    }
}