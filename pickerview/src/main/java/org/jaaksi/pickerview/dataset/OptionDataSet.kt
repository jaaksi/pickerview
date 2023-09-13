package org.jaaksi.pickerview.dataset

/**
 * Created by fuchaoyang on 2018/2/11.<br></br>
 * description：[OptionPicker]专用数据集
 */
interface OptionDataSet : PickerDataSet {
    /**
     * @return 下一级的数据集
     */
    fun getSubs(): List<OptionDataSet>?
}