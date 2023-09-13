package org.jaaksi.pickerview.picker.option

import org.jaaksi.pickerview.adapter.ArrayWheelAdapter
import org.jaaksi.pickerview.dataset.OptionDataSet
import org.jaaksi.pickerview.picker.OptionPicker

/**
 * Created by fuchaoyang on 2018/7/6.<br></br>
 * description：关联的Option Picker Delegate
 */
class OptionDelegate : IOptionDelegate {
    private var mDelegate: OptionPicker.Delegate? = null
    private var mOptions: List<OptionDataSet>? = null

    override fun init(delegate: OptionPicker.Delegate) {
        mDelegate = delegate
    }

    override fun setData(vararg options: List<OptionDataSet>) {
        mOptions = options[0]
        setSelectedWithValues()
    }

    /**
     * 根据选中的values初始化选中的position
     *
     * @param values 选中数据的value[OptionDataSet.getValue]，如果values[i]==null，如果该列有数据，则进行默认选中，否则认为没有该列
     */
    override fun setSelectedWithValues(vararg values: String?) {
        var temp = mOptions
        for (i in 0 until mDelegate!!.hierarchy) {
            val pickerView = mDelegate!!.getPickerViews()[i]
            val adapter = pickerView.adapter as ArrayWheelAdapter<*>?
            if (adapter == null || adapter.data !== temp) {
                pickerView.adapter = ArrayWheelAdapter(temp)
            }
            if (temp == null || temp.size == 0) { // 数据源无效
                mDelegate!!.selectedPosition[i] = -1
            } else if (values.size <= i || values[i] == null) { // 选中默认项0...
                mDelegate!!.selectedPosition[i] = 0
            } else { // 遍历找到选中的下标，如果没有找到，则将下标置为0
                for (j in temp.indices) {
                    val dataSet = temp[j]
                    if (values[i] == dataSet.getValue()) {
                        mDelegate!!.selectedPosition[i] = j
                        break
                    }
                    if (j == temp.size) {
                        mDelegate!!.selectedPosition[i] = 0
                    }
                }
            }
            if (mDelegate!!.selectedPosition[i] == -1) {
                temp = null
            } else {
                pickerView.setSelectedPosition(mDelegate!!.selectedPosition[i], false)
                val dataSet = temp!![mDelegate!!.selectedPosition[i]]
                temp = dataSet.getSubs()
            }
        }
    }

    override fun reset() {
        var temp = mOptions
        for (i in mDelegate!!.getPickerViews().indices) {
            val pickerView = mDelegate!!.getPickerViews()[i]
            val adapter = pickerView.adapter as ArrayWheelAdapter<*>?
            if (adapter == null || adapter.data !== temp) {
                pickerView.adapter = ArrayWheelAdapter(temp)
            }
            // 重置下标
            pickerView.setSelectedPosition(mDelegate!!.selectedPosition[i], false)
            if (temp.isNullOrEmpty()) {
                mDelegate!!.selectedPosition[i] = -1 // 下标置为-1表示选中的第i列没有
            } else if (temp.size <= mDelegate!!.selectedPosition[i]) { // 下标超过范围，取默认值0
                mDelegate!!.selectedPosition[i] = 0
            }
            if (mDelegate!!.selectedPosition[i] == -1) {
                temp = null
            } else {
                val dataSet = temp!![mDelegate!!.selectedPosition[i]]
                temp = dataSet.getSubs()
            }
        }
    }

    override val selectedOptions: Array<OptionDataSet?>
        /**
         * 获取选中的选项
         *
         * @return 选中的选项，如果指定index为null则表示该列没有数据
         */
        get() {
            val optionDataSets = arrayOfNulls<OptionDataSet>(
                mDelegate!!.hierarchy
            )
            var temp = mOptions
            for (i in 0 until mDelegate!!.hierarchy) {
                if (mDelegate!!.selectedPosition[i] == -1) break
                // !=-1则一定会有数据，所以不需要判断temp是否为空，也不用担心会下标越界
                optionDataSets[i] = temp!![mDelegate!!.selectedPosition[i]]
                temp = optionDataSets[i]!!.getSubs()
            }
            return optionDataSets
        }
}