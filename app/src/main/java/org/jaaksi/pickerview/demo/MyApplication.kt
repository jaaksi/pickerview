package org.jaaksi.pickerview.demo

import android.app.Application
import org.jaaksi.pickerview.demo.picker.PickerInitHelper

/**
 * 创建时间：2018年02月28日17:45 <br></br>
 * 作者：fuchaoyang <br></br>
 * 描述：
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 建议在application中初始化picker 默认属性实现全局设置
        PickerInitHelper.initDefaultPicker(applicationContext)
    }


}