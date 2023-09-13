package org.jaaksi.pickerview.demo.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.jaaksi.pickerview.demo.R

/**
 * Created by fuchaoyang on 2018/2/10.<br></br>
 * description：
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_timepicker -> openFragment(TimePickerFragment())
            R.id.btn_mixedtimepicker -> openFragment(MixedTimeFormatFragment())
            R.id.btn_optionpicker -> openFragment(OptionPickerFragment())
            R.id.btn_optionpicker2 -> openFragment(OptionPickerFragment2())
            R.id.btn_test_pickerview -> openFragment(TestPickerViewFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }
}