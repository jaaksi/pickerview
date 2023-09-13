package org.jaaksi.pickerview.adapter

/**
 * The simple Array wheel adapter
 * 数据实现 [PickerDataSet]即可
 *
 * @param <T> the element type
 */
class ArrayWheelAdapter<T>(val data: List<T>?) : WheelAdapter<T> {

    override fun getItem(index: Int): T? {
        return data?.getOrNull(index)
    }

    override val itemCount: Int
        get() = data?.size ?: 0
}