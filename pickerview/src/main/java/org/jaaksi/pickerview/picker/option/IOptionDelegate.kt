package org.jaaksi.pickerview.picker.option

import org.jaaksi.pickerview.dataset.OptionDataSet
import org.jaaksi.pickerview.picker.OptionPicker

/**
 * Created by fuchaoyang on 2018/7/6.<br></br>
 * description：
 */
interface IOptionDelegate {
    fun init(delegate: OptionPicker.Delegate)

    fun setData(vararg options: List<OptionDataSet>)

    /**
     * 根据选中的values初始化选中的position
     */
    fun setSelectedWithValues(vararg values: String?)

    /**
     * 获取选中的选项，如果指定index为null则表示该列没有数据
     */
    val selectedOptions: Array<OptionDataSet?>
    fun reset()
}