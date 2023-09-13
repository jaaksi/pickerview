package org.jaaksi.pickerview.adapter

interface WheelAdapter<T> {
    /**
     * Gets items count
     *
     * @return the count of wheel items
     */
    val itemCount: Int

    /**
     * Gets a wheel item by index.
     *
     * @param index the item index
     * @return the wheel item text or null
     */
    fun getItem(index: Int): T?
}