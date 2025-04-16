package org.jaaksi.pickerview.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * 创建时间：2018年02月02日12:00 <br></br>
 * 作者：fuchaoyang <br></br>
 * 描述：时间工具类
 */
object DateUtil {

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

    // 判断两个时间戳是否是同一天
    fun isSameDay(time1: Long, time2: Long): Boolean{
        return time1.dayStart() == time2.dayStart()
    }

    /**
     * 不能用时间戳差值 / 86400000, 夏令时会有误差
     * @return endTime - startTime 相差的天数
     */
    fun getIntervalDay(time1: Long, time2: Long): Int {
        val cal1 = Calendar.getInstance().apply { timeInMillis = min(time1,time2) }
        val cal2 = Calendar.getInstance().apply { timeInMillis = max(time1, time2) }

        // 如果是同一年，直接计算 dayOfYear 差值
        if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
            return if (time1 > time2)
                -(cal2.get(Calendar.DAY_OF_YEAR) - cal1.get(Calendar.DAY_OF_YEAR))
            else
                cal2.get(Calendar.DAY_OF_YEAR) - cal1.get(Calendar.DAY_OF_YEAR)
        }

        // 跨年计算
        var daysBetween = 0

        // 1. 计算起始年剩余的天数
        val daysLeftInYear1 = cal1.getActualMaximum(Calendar.DAY_OF_YEAR) - cal1.get(Calendar.DAY_OF_YEAR)
        daysBetween += daysLeftInYear1

        // 2. 计算中间完整年份的天数
        var year = cal1.get(Calendar.YEAR) + 1
        while (year < cal2.get(Calendar.YEAR)) {
            val tempCal = Calendar.getInstance().apply { set(Calendar.YEAR, year) }
            daysBetween += tempCal.getActualMaximum(Calendar.DAY_OF_YEAR)
            year++
        }

        // 3. 计算结束年已过的天数
        daysBetween += cal2.get(Calendar.DAY_OF_YEAR)

        return if (time1 > time2) -daysBetween else daysBetween
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