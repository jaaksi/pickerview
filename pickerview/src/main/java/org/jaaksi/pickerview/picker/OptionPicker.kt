package org.jaaksi.pickerview.picker

import android.content.Context
import org.jaaksi.pickerview.dataset.OptionDataSet
import org.jaaksi.pickerview.dialog.IPickerDialog
import org.jaaksi.pickerview.picker.option.ForeignOptionDelegate
import org.jaaksi.pickerview.picker.option.IOptionDelegate
import org.jaaksi.pickerview.picker.option.OptionDelegate
import org.jaaksi.pickerview.widget.BasePickerView
import org.jaaksi.pickerview.widget.BasePickerView.OnSelectedListener
import org.jaaksi.pickerview.widget.PickerView

/**
 * Created by fuchaoyang on 2018/2/11.<br></br>
 * description：多级别的的选项picker，支持联动，与非联动
 * 强大点：
 * 与https://github.com/Bigkoo/Android-PickerView对比
 * 1.支持设置层级
 * 2.构造数据源简单，只需要实现OptionDataSet接口
 * 3.支持联动及不联动
 * 3.支持通过选中的value设置选中项，内部处理选中项逻辑，避免用户麻烦的遍历处理
 *
 *
 */
class OptionPicker private constructor(
    context: Context,
    private val hierarchy: Int,
    private val onOptionSelectListener: OnOptionSelectListener
) : BasePicker(context), OnSelectedListener, BasePickerView.Formatter {
    // 层级，有几层add几个pickerview

    // 选中的下标。如果为-1，表示当前选中的index列没有数据
    /**
     * 获取选中的下标
     *
     * @return 选中的下标，数组size=mHierarchy，如果为-1表示该列没有数据
     */
    val selectedPosition: IntArray = IntArray(this.hierarchy)

    /** 是否无关连  */
    private var mIsForeign = false
    private var mFormatter: Formatter? = null
    private var mDelegate: IOptionDelegate? = null

    private fun initPicker() {
        for (i in 0 until this.hierarchy) {
            val pickerView: PickerView<*> = createPickerView<Any>(i, 1f)
            pickerView.setOnSelectedListener(this)
            pickerView.formatter = this
        }
    }

    fun setFormatter(formatter: Formatter?) {
        mFormatter = formatter
    }

    private fun initForeign(foreign: Boolean) {
        mIsForeign = foreign
        mDelegate = if (mIsForeign) { // 不关联的
            ForeignOptionDelegate()
        } else {
            OptionDelegate()
        }
        mDelegate!!.init(object : Delegate {
            override val hierarchy: Int
                get() = this@OptionPicker.hierarchy
            override val selectedPosition: IntArray
                get() = this@OptionPicker.selectedPosition

            override fun getPickerViews(): List<PickerView<OptionDataSet>> {
                return pickerViews as List<PickerView<OptionDataSet>>
            }

        })
    }

    /**
     * 根据选中的values初始化选中的position并初始化pickerview数据
     *
     * @param options data
     */
    fun setData(vararg options: List<OptionDataSet>) {
        // 初始化是否关联
        initForeign(options.size > 1)
        mDelegate!!.setData(*options)
    }

    /**
     * 根据选中的values初始化选中的position
     *
     * @param values 选中数据的value[OptionDataSet.getValue]，如果values[0]==null，则进行默认选中，其他为null认为没有该列
     */
    fun setSelectedWithValues(vararg values: String?) {
        mDelegate!!.setSelectedWithValues(*values)
    }


    private fun reset() {
        mDelegate!!.reset()
    }

    val selectedOptions: Array<OptionDataSet?>
        /**
         * 获取选中的选项
         *
         * @return 选中的选项，如果指定index为null则表示该列没有数据
         */
        get() = mDelegate!!.selectedOptions

    override fun onConfirm() {
        onOptionSelectListener.onOptionSelect(this, this.selectedPosition, selectedOptions)
    }

    // 重置选中的position
    private fun resetPosition(index: Int, position: Int) {
        for (i in index until selectedPosition.size) {
            if (i == index) {
                selectedPosition[i] = position
            } else {
                if (!mIsForeign) {
                    // 如果是无关的则不需要处理后面的index，关联的则直接重置为0
                    selectedPosition[i] = 0
                }
            }
        }
    }

    override fun onSelected(pickerView: BasePickerView<*>, position: Int) {
        // 1联动2联动3...当前选中position，后面的都重置为0，更改mSelectedPosition,然后直接reset
        val index = pickerView.tag as Int
        resetPosition(index, position)
        reset()
        if (!needDialog){
            onConfirm()
        }
    }

    override fun format(
        pickerView: BasePickerView<*>,
        position: Int,
        charSequence: CharSequence?
    ): CharSequence? {
        return if (mFormatter == null) charSequence else mFormatter!!.format(
            this,
            pickerView.tag as Int,
            position,
            charSequence
        )
    }

    /**
     * 强制设置的属性直接在构造方法中设置
     *
     * @param hierarchy 层级，有几层add几个pickerview
     * @param listener listener
     */
    class Builder(
        private val context: Context,
        private val hierarchy: Int,
        private val onOptionSelectListener: OnOptionSelectListener
    ) {
        private var mInterceptor: Interceptor? = null
        private var mFormatter: Formatter? = null
        private var needDialog = true
        private var iPickerDialog: IPickerDialog? = null

        /**
         * 设置内容 Formatter
         *
         * @param formatter formatter
         */
        fun setFormatter(formatter: Formatter?): Builder {
            mFormatter = formatter
            return this
        }

        /**
         * 设置拦截器
         *
         * @param interceptor 拦截器
         */
        fun setInterceptor(interceptor: Interceptor?): Builder {
            mInterceptor = interceptor
            return this
        }

        /**
         * 自定义弹窗，如果为null表示不需要弹窗
         * @param iPickerDialog
         */
        fun dialog(iPickerDialog: IPickerDialog?): Builder {
            needDialog = iPickerDialog != null
            this.iPickerDialog = iPickerDialog
            return this
        }

        fun create(): OptionPicker {
            val picker = OptionPicker(context, hierarchy, onOptionSelectListener)
            picker.needDialog = needDialog
            picker.iPickerDialog = iPickerDialog
            picker.initPickerView()
            picker.setFormatter(mFormatter)
            picker.setInterceptor(mInterceptor)
            picker.initPicker()
            return picker
        }
    }

    interface Formatter {
        /**
         * @param level 级别 0 ~ mHierarchy - 1
         * @param charSequence charSequence
         */
        fun format(
            picker: OptionPicker,
            level: Int,
            position: Int,
            charSequence: CharSequence?
        ): CharSequence?
    }

    interface OnOptionSelectListener {
        /**
         * @param selectedPosition length = mHierarchy。选中的下标:如果指定index为-1，表示当前选中的index列没有数据
         * @param selectedOptions length = mHierarchy。选中的选项，如果指定index为null则表示该列没有数据
         */
        fun onOptionSelect(
            picker: OptionPicker, selectedPosition: IntArray,
            selectedOptions: Array<OptionDataSet?>
        )
    }

    interface Delegate {
        val hierarchy: Int
        val selectedPosition: IntArray
        fun getPickerViews(): List<PickerView<OptionDataSet>>
    }
}