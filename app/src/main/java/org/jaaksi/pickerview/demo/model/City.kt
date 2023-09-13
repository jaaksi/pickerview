package org.jaaksi.pickerview.demo.model

import androidx.annotation.Keep
import org.jaaksi.pickerview.dataset.OptionDataSet

/**
 * Created by fuchaoyang on 2018/2/11.<br></br>
 * description：市
 */
@Keep
data class City(val name: String, val area: List<District>) : OptionDataSet {

    override fun getSubs() = area

    override fun getCharSequence() = name

    override fun getValue() = name
}