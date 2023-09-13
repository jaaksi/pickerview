package org.jaaksi.pickerview.picker

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.SparseArray
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import org.jaaksi.pickerview.dialog.DefaultPickerDialog
import org.jaaksi.pickerview.dialog.IGlobalDialogCreator
import org.jaaksi.pickerview.dialog.IPickerDialog
import org.jaaksi.pickerview.widget.PickerView

/**
 * 创建时间：2018年01月31日18:28 <br></br>
 * 作者：fuchaoyang <br></br>
 * BasePicker中并不提供对pickerview的设置方法，而是通过接口PickerHandler转交PickerView处理
 * 三个picker的的思路有部分是不一样的，如reset调用地方，看看是不是可以优化
 */
abstract class BasePicker(protected var mContext: Context) {

    /** 是否启用dialog  */
    @JvmField
    protected var needDialog = true

    @JvmField
    protected var iPickerDialog: IPickerDialog? = null
    protected lateinit var mPickerContainer: LinearLayout
    private var mInterceptor: Interceptor? = null

    /**
     * setTag用法同[View.setTag]
     *
     * @param tag tag
     */
    var tag: Any? = null
    private var mKeyedTags: SparseArray<Any>? = null
    private val mPickerViews: MutableList<PickerView<*>> = ArrayList()

    /**
     * 设置拦截器，用于用于在pickerview创建时拦截，设置pickerview的属性。Picker内部并不提供对PickerView的设置方法，
     * 而是通过Interceptor实现，实现Picker和PickerView的属性设置解耦。
     * 必须在调用 [.createPickerView]之前设置。
     * 子类应该在Builder中提供该方法。
     */
    protected fun setInterceptor(interceptor: Interceptor?) {
        mInterceptor = interceptor
    }

    /**
     * 设置picker背景
     *
     * @param color color
     */
    fun setPickerBackgroundColor(@ColorInt color: Int) {
        mPickerContainer.setBackgroundColor(color)
    }

    /**
     * 设置pickerview父容器padding 单位:px
     */
    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mPickerContainer.setPadding(left, top, right, bottom)
    }

    fun getTag(key: Int): Any? {
        return mKeyedTags?.get(key)
    }

    protected fun initPickerView() {
        mPickerContainer = LinearLayout(mContext)
        mPickerContainer.orientation = LinearLayout.HORIZONTAL
        mPickerContainer.layoutParams = LinearLayout.LayoutParams(-1, -2)
        if (sDefaultPaddingRect != null) {
            setPadding(
                sDefaultPaddingRect!!.left, sDefaultPaddingRect!!.top, sDefaultPaddingRect!!.right,
                sDefaultPaddingRect!!.bottom
            )
        }
        if (sDefaultPickerBackgroundColor != Color.TRANSPARENT) {
            setPickerBackgroundColor(sDefaultPickerBackgroundColor)
        }
        if (needDialog) { // 是否使用弹窗
            // 弹窗优先级：自定义的 > 全局的 > 默认的
            if (iPickerDialog == null) { // 如果没有自定义dialog
                iPickerDialog = if (sDefaultDialogCreator != null) { // 如果定义了全局的dialog
                    sDefaultDialogCreator!!.create(mContext)
                } else { // 使用默认的
                    DefaultPickerDialog(mContext)
                }
            }
            iPickerDialog?.onCreate(this)
        }
    }

    /**
     * [.createPickerView]
     *
     * @return Picker中所有的pickerview集合
     */
    val pickerViews: List<PickerView<*>>
        get() = mPickerViews

    /**
     * 如果使用[.createPickerView]创建pickerview，就不需要手动添加
     *
     * @param pickerView pickerView
     */
    protected fun addPicker(pickerView: PickerView<*>) {
        mPickerViews.add(pickerView)
    }

    /**
     * 创建pickerview
     *
     * @param tag settag
     * @param weight 权重
     */
    protected fun <T> createPickerView(tag: Any?, weight: Float): PickerView<T> {
        val pickerView: PickerView<T> = PickerView(mContext)
        pickerView.tag = tag
        // 这里是竖直方向的，如果要设置横向的，则自己再设置LayoutParams
        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = weight
        // do it
        if (mInterceptor != null) {
            mInterceptor!!.intercept(pickerView, params)
        }
        pickerView.layoutParams = params
        mPickerContainer.addView(pickerView)
        addPicker(pickerView)
        return pickerView
    }

    /**
     * 通过tag找到对应的pickerview
     *
     * @param tag tag
     * @return 对应tag的pickerview，找不到返回null
     */
    fun findPickerViewByTag(tag: Any): PickerView<*>? {
        for (pickerView in mPickerViews) {
            if (checkIsSamePickerView(tag, pickerView.tag)) return pickerView
        }
        return null
    }

    /**
     * 通过两个tag判断是否是同一个pickerview
     */
    protected fun checkIsSamePickerView(tag: Any, pickerViewTag: Any): Boolean {
        return tag == pickerViewTag
    }

    /**
     * 是否滚动未停止
     */
    fun canSelected(): Boolean {
        for (i in mPickerViews.indices.reversed()) {
            val pickerView = mPickerViews[i]
            if (!pickerView.canSelected()) {
                return false
            }
        }
        return true
    }

    /**
     * setTag 用法同[View.setTag]
     *
     * @param key key R.id.xxx
     * @param tag tag
     */
    fun setTag(key: Int, tag: Any) {
        // If the package id is 0x00 or 0x01, it's either an undefined package
        // or a framework id
        require(key ushr 24 >= 2) { "The key must be an application-specific " + "resource id." }
        setKeyedTag(key, tag)
    }

    private fun setKeyedTag(key: Int, tag: Any) {
        if (mKeyedTags == null) {
            mKeyedTags = SparseArray(2)
        }
        mKeyedTags!!.put(key, tag)
    }

    /**
     * @return 获取IPickerDialog
     */
    fun dialog(): IPickerDialog? {
        return iPickerDialog
    }

    /**
     * @return 获取picker的view，用于非弹窗情况
     */
    fun view(): LinearLayout {
        return mPickerContainer
    }

    /**
     * 显示picker弹窗
     */
    fun show() {
        iPickerDialog?.showDialog()
    }

    /**
     * 点击确定按钮的回调
     */
    abstract fun onConfirm()

    /**
     * 用于子类修改设置PickerView属性
     */
    fun interface Interceptor {
        /**
         * 拦截pickerview的创建，我们可以自定义
         *
         * @param pickerView 增加layoutparams参数，方便设置weight
         */
        fun intercept(pickerView: PickerView<*>, params: LinearLayout.LayoutParams)
    }

    companion object {
        /** pickerView父容器的 default padding  */
        var sDefaultPaddingRect: Rect? = null

        /** default picker background color  */
        var sDefaultPickerBackgroundColor = Color.WHITE

        /** Canceled dialog OnTouch Outside  */
        var sDefaultCanceledOnTouchOutside = true

        /** 用于构建全局的DefaultDialog的接口  */
        var sDefaultDialogCreator: IGlobalDialogCreator? = null
    }
}