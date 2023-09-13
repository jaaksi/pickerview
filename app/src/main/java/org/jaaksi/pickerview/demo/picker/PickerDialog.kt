package org.jaaksi.pickerview.demo.picker

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.jaaksi.pickerview.R
import org.jaaksi.pickerview.demo.databinding.DialogPickerviewCustomBinding
import org.jaaksi.pickerview.dialog.IPickerDialog
import org.jaaksi.pickerview.dialog.OnPickerChooseListener
import org.jaaksi.pickerview.picker.BasePicker

/**
 * picker.dialog(IPickerDialog接口)自定义Dialog。提供DefaultPickerDialog，支持全局设定
 */
class PickerDialog(context: Context) :
    BottomSheetDialog(context, R.style.BottomSheetDialog),
    IPickerDialog {
    lateinit var picker: BasePicker
        private set
    var onPickerChooseListener: OnPickerChooseListener? = null

    val binding = DialogPickerviewCustomBinding.inflate(layoutInflater)

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
        setCanceledOnTouchOutside(true)
        binding.btnCancel.setOnClickListener {
            if (!picker.canSelected()) return@setOnClickListener  //  滑动未停止不响应点击事件
            dismiss()
            if (onPickerChooseListener != null) {
                onPickerChooseListener!!.onCancel()
            }

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
        binding.pickerContainer.addView(picker.view())
    }

    override fun showDialog() {
        show()
    }

}