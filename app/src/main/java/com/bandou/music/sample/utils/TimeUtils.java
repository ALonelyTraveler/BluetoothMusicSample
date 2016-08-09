package com.bandou.music.sample.utils;

import android.text.TextUtils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期处理工具类
 * Created by macchen on 15/3/26.
 */
public class TimeUtils {
    /**
     * 默认的时间格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 默认的日期格式
     */
    public static final String DATE_FORMAT_DATE = "yyyy-MM-dd";

    /**
     * 禁止方法构造
     */
    private TimeUtils() {
        throw new AssertionError();
    }

    /**
     * 按指定格式去格式化毫秒数
     *
     * @param timeInMillis 毫秒数
     * @param format       转换格式
     * @return 返回转换后的字符串
     */
    public static String formatMillisTo(long timeInMillis, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date(timeInMillis));
    }

    /**
     * 按固定格式去格式化毫秒数
     *
     * @param timeInMillis 毫秒数
     * @return 返回转换后的字符串
     */
    public static String formatMillisToTime(long timeInMillis) {
        return formatMillisTo(timeInMillis, DEFAULT_DATE_FORMAT);
    }

    /**
     * 按固定格式去格式化毫秒数
     *
     * @param timeInMillis 毫秒数
     * @return 返回转换后的字符串
     */
    public static String formatMillisToDate(long timeInMillis) {
        return formatMillisTo(timeInMillis, DATE_FORMAT_DATE);
    }

    /**
     * @return 返回当前毫秒数
     */
    public static long getCurrentMillis() {
        return System.currentTimeMillis();
    }

    /**
     * @return 返回当前毫秒数转换后的字符串
     */
    public static String getCurrentTimeInString() {
        return getCurrentTimeInString(DEFAULT_DATE_FORMAT);
    }

    /**
     * 返回当前毫秒数转换后的字符串
     *
     * @param format 转换格式
     * @return
     */
    public static String getCurrentTimeInString(String format) {
        return formatMillisTo(getCurrentMillis(), format);
    }

    /**
     * 字符串指定格式转换成Date对象
     *
     * @param strDate 需要转换的字符串对象
     * @param format  转换格式
     * @return
     */
    public static Date stringToDate(String strDate, String format) {
        if (strDate == null || format == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        Date strToDate = formatter.parse(strDate, pos);
        return strToDate;
    }

    /**
     * 字符串固定格式转换成Date对象
     *
     * @param strDate 需要转换的字符串对象
     * @return
     */
    public static Date stringToDate(String strDate) {
        return stringToDate(strDate, DEFAULT_DATE_FORMAT);
    }

    /**
     * 按指定格式格式化时间
     *
     * @param date
     * @param format
     * @return
     */
    public static String format(Date date, String format) {
        if (date == null || TextUtils.isEmpty(format)) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String formatString = null;
        try {
            formatString = dateFormat.format(date);
        } catch (Exception e) {
            formatString = null;
        }
        return formatString;
    }

    /**
     * 毫秒数转成12时12分钟12秒
     *
     * @param millis
     * @return
     */
    public static String millis2HourMinSec(long millis) {
        int hour = (int) (millis / 3600000);
        int minute = (int) (millis / 60000 - 60 * hour);
        int second = (int) ((millis / 1000) % 60);
        if (hour > 0) {
            return hour + "小时" + minute + "分钟";
        }
        if (minute > 0) {
            return minute + "分钟";
        }
        if (second >= 0) {
            return second + "秒";
        }
        return 0 + "秒";
    }

    /**
     * 毫秒数转成12:12:12
     *
     * @param millis
     * @return
     */
    public static String millis2HourSynxMinSec(long millis) {
        int hour = (int) (millis / 3600000);
        int minute = (int) (millis / 60000 - 60 * hour);
        int second = (int) ((millis / 1000) % 60);
        if (hour > 0) {
            return (hour > 9 ? hour : ("0" + hour)) + ":" + (minute > 9 ? minute : ("0" + minute)) + ":" + (second > 9 ? second : ("0" + second));
        }
        if (minute > 0) {
            return (minute > 9 ? minute : ("0" + minute)) + ":" + (second > 9 ? second : ("0" + second)) + "";
        }
        if (second >= 0) {
            return "00:" + (second > 9 ? second : ("0" + second)) + "";
        }
        return "00:00";
    }

    public static String toHourMin(long min) {
        int hour = (int) (min / 60);
        int after = (int) (min % 60);
        return getTimeString(hour, after);
    }


    public static String getTimeString(int hour, int min) {
        return (hour >= 10 ? hour : ("0" + hour)) + ":" + ((min >= 10 ? min : ("0" + min)));
    }

    public static String toHourMinNotZero(long min) {
        int hour = (int) (min / 60);
        int after = (int) (min % 60);
        return getTimeStringNotZero(hour, after);
    }

    public static String getTimeStringNotZero(int hour, int min) {
        return hour + ":" + ((min >= 10 ? min : ("0" + min)));
    }

}