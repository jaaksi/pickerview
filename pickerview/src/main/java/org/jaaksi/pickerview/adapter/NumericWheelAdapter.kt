package org.jaaksi.pickerview.adapter

/**
 * 数字adapter
 *
 * @param minValue the wheel min value
 * @param maxValue the wheel max value
 */
class NumericWheelAdapter(private val minValue: Int, private val maxValue: Int) :
    WheelAdapter<Int> {
    override val itemCount: Int
        get() = maxValue - minValue + 1

    override fun getItem(index: Int): Int {
        return if (index in 0 until itemCount) minValue + index else 0
    }
}