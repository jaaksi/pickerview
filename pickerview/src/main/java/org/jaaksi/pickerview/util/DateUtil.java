package org.jaaksi.pickerview.util;

import java.util.Calendar;

/**
 * 创建时间：2018年02月02日12:00 <br>
 * 作者：fuchaoyang <br>
 * 描述：时间工具类
 */

public class DateUtil {
  public static final long ONE_DAY = 1000 * 60 * 60 * 24;

  /**
   * 获取某年某月有多少天
   */
  public static int getDayOfMonth(int year, int month) {
    Calendar c = Calendar.getInstance();
    c.set(year, month, 0); //输入类型为int类型
    return c.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * 获取两个时间相差的天数
   *
   * @param time1 time1
   * @param time2 time2
   * @return time1 - time2相差的天数
   */
  public static int getDayOffset(long time1, long time2) {
    // 将小的时间置为当天的0点
    long offsetTime;
    if (time1 > time2) {
      offsetTime = time1 - getDayStartTime(getCalendar(time2)).getTimeInMillis();
    } else {
      offsetTime = getDayStartTime(getCalendar(time1)).getTimeInMillis() - time2;
    }
    return (int) (offsetTime / ONE_DAY);
  }

  public static Calendar getCalendar(long time) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return calendar;
  }

  public static Calendar getDayStartTime(Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar;
  }
}
