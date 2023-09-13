package org.jaaksi.pickerview.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 创建时间：2018年02月02日12:00 <br></br>
 * 作者：fuchaoyang <br></br>
 * 描述：时间工具类
 */
object DateUtil {
    const val ONE_DAY = 1000 * 60 * 60 * 24L

    @JvmStatic
    fun createDateFormat(format: String): SimpleDateFormat {
        return SimpleDateFormat(format, Locale.getDefault())
    }

    /**
     * 获取某年某月有多少天
     */
    @JvmStatic
    fun getDayOfMonth(year: Int, month: Int): Int {
        val c = Calendar.getInstance()
        c[year, month] = 0 //输入类型为int类型
        return c[Calendar.DAY_OF_MONTH]
    }

    /**
     * 获取两个时间相差的天数
     *
     * @param time1 time1
     * @param time2 time2
     * @return time1 - time2相差的天数
     */
    @JvmStatic
    fun getDayOffset(time1: Long, time2: Long): Int {
        // 将小的时间置为当天的0点
        val offsetTime: Long = if (time1 > time2) {
            time1 - time2.dayStart()
        } else {
            time1.dayStart() - time2
        }
        return (offsetTime / ONE_DAY).toInt()
    }

    fun Long.toCalendar(): Calendar {
        return Calendar.getInstance().apply { timeInMillis = this@toCalendar }
    }

    fun Long.dayStart(): Long {
        val calendar = this.toCalendar()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.timeInMillis
    }
}