package org.jaaksi.pickerview.dataset

/**
 * 创建时间：2018年01月31日16:34 <br></br>
 * 作者：fuchaoyang <br></br>
 * 描述：数据实现接口，用户显示文案
 */
interface PickerDataSet {
    fun getCharSequence(): CharSequence?

    /**
     * @return 上传的value，用于匹配初始化选中的下标
     */
    fun getValue(): String?
}