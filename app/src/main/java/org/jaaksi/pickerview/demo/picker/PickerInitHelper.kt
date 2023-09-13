package org.jaaksi.pickerview.demo.picker

import android.content.Context
import android.graphics.Rect
import org.jaaksi.pickerview.dialog.IGlobalDialogCreator
import org.jaaksi.pickerview.dialog.IPickerDialog
import org.jaaksi.pickerview.picker.BasePicker
import org.jaaksi.pickerview.util.Util
import org.jaaksi.pickerview.widget.BasePickerView
import org.jaaksi.pickerview.widget.DefaultCenterDecoration
import org.jaaksi.pickerview.widget.PickerView

object PickerInitHelper {
    fun initDefaultPicker(context: Context) {
        // 利用修改静态默认属性值，快速定制一套满足自己app样式需求的Picker.
        // BasePickerView
        // 把这些改成资源，通过覆盖资源的方式修改全局默认属性
        BasePickerView.sDefaultVisibleItemCount = 5
        BasePickerView.sDefaultItemSize = 50
        BasePickerView.sDefaultIsCirculation = false
        //BasePickerView.sDefaultDrawIndicator = false

        // PickerView
        PickerView.sOutTextSize = 18
        PickerView.sCenterTextSize = 18
        //PickerView.sCenterColor = "#41bc6a".toColorInt()
        //PickerView.sOutColor = "#666666".toColorInt()
        //PickerView.sShadowColors = null

        // BasePicker
        val padding = Util.dip2px(context, 20f)
        BasePicker.sDefaultPaddingRect = Rect(padding, padding, padding, padding)
        //BasePicker.sDefaultPickerBackgroundColor = Color.WHITE
        BasePicker.sDefaultCanceledOnTouchOutside = false
        BasePicker.sDefaultDialogCreator = object : IGlobalDialogCreator {
            override fun create(context: Context): IPickerDialog {
                return PickerDialog(context)
            }
        }

        // DefaultCenterDecoration
        //DefaultCenterDecoration.sDefaultLineWidth = 1f
        //DefaultCenterDecoration.sDefaultLineColor = "#ECECEE".toColorInt()
        //DefaultCenterDecoration.sDefaultDrawable =  ColorDrawable(Color.WHITE)
        val leftMargin = Util.dip2px(context, 10f)
        val topMargin = Util.dip2px(context, 0f)
        DefaultCenterDecoration.sDefaultMarginRect =
            Rect(leftMargin, -topMargin, leftMargin, -topMargin)
    }
}