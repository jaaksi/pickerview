package org.jaaksi.pickerview.demo.model

import androidx.annotation.Keep
import org.jaaksi.pickerview.dataset.OptionDataSet

/**
 * Created by fuchaoyang on 2018/2/11.<br></br>
 * description：区、县
 */
@Keep
data class District(val name: String) : OptionDataSet {
    override fun getSubs(): List<OptionDataSet>? {
        return null
    }

    override fun getCharSequence(): CharSequence {
        return name
    }

    override fun getValue(): String {
        return name
    }

}