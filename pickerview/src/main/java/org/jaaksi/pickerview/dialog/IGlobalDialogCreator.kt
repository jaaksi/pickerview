package org.jaaksi.pickerview.dialog

import android.content.Context

interface IGlobalDialogCreator {
    /**
     * 创建IPickerDialog
     */
    fun create(context: Context): IPickerDialog
}