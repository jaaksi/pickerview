package org.jaaksi.pickerview.demo.model

import androidx.annotation.Keep
import org.jaaksi.pickerview.dataset.OptionDataSet

/**
 * Created by fuchaoyang on 2018/2/11.<br></br>
 * description：省
 */
@Keep
data class Province(
    val name: String, val city: List<City>,
) : OptionDataSet {

    override fun getSubs() = city

    override fun getCharSequence() = name

    override fun getValue(): String = name
}