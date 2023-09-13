package org.jaaksi.pickerview.dialog

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.jaaksi.pickerview.R
import org.jaaksi.pickerview.databinding.DialogPickerviewDefaultBinding
import org.jaaksi.pickerview.picker.BasePicker

/**
 * picker.dialog(IPickerDialog接口)自定义Dialog。提供DefaultPickerDialog，支持全局设定
 */
class DefaultPickerDialog(context: Context) :
    BottomSheetDialog(context, R.style.BottomSheetDialog),
    IPickerDialog {
    lateinit var picker: BasePicker
        private set
    protected var onPickerChooseListener: OnPickerChooseListener? = null

    val binding = DialogPickerviewDefaultBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(sDefaultCanceledOnTouchOutside)
        binding.btnCancel.setOnClickListener {
            if (!picker.canSelected()) return@setOnClickListener  //  滑动未停止不响应点击事件
            dismiss()
            onPickerChooseListener?.onCancel()

        }
        binding.btnConfirm.setOnClickListener {
            if (!picker.canSelected()) return@setOnClickListener
            // 给用户拦截
            if (onPickerChooseListener == null || onPickerChooseListener!!.onConfirm()) {
                // 抛给picker去处理
                dismiss()
                picker.onConfirm()
            }
        }
    }

    /**
     * 先于onCreate(Bundle savedInstanceState)执行
     */
    override fun onCreate(picker: BasePicker) {
        this.picker = picker
        binding.root.addView(picker.view())
    }

    override fun showDialog() {
        show()
    }

    companion object {
        /** Canceled dialog OnTouch Outside  */
        var sDefaultCanceledOnTouchOutside = true
    }
}