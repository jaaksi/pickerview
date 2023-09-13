package org.jaaksi.pickerview.dialog

import org.jaaksi.pickerview.picker.BasePicker

interface IPickerDialog {
    /**
     * picker create 时回调
     * @see BasePicker.BasePicker
     */
    fun onCreate(picker: BasePicker)

    /**
     * 其实可以不提供这个方法，为了方便在[BasePicker.show]
     */
    fun showDialog()
}