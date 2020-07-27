package com.app.ahgas_2c37724.util;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.text.format.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    public interface MyDatePicker{
        void onDateSelect(long date);
    }
    public interface MyTimePicker{
        void onTimeSelect(long time);
    }

    public static void showDatePicker(final Context context, long minDate, long maxDate, final MyDatePicker myDatePicker){
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if(myDatePicker != null)
                    myDatePicker.onDateSelect(calendar.getTimeInMillis());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        if(minDate > 0)
            dialog.getDatePicker().setMinDate(minDate);
        if(maxDate > 0)
            dialog.getDatePicker().setMaxDate(maxDate);
        dialog.show();
    }

    public static void showTimePicker(final Context context, final MyTimePicker myTimePicker){
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                if(myTimePicker != null)
                    myTimePicker.onTimeSelect(cal.getTimeInMillis());
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.show();
    }

    public static long getOffset(Date timeNew, Date timeOld) {
        return timeNew.getTime() - timeOld.getTime();
    }

    public static String getTimerLabel(long millis) {
        long seconds = millis / 1000 % 60;
        long minutes = millis / (60 * 1000) % 60;
        long hours = millis / (60 * 60 * 1000) % 24;
        return twoDigit(hours)+":"+twoDigit(minutes)+":"+twoDigit(seconds);
    }

    public static String twoDigit(long number){
        return String.format("%02d", number);
    }

    public static SimpleDateFormat getSdf(String format){
        return new SimpleDateFormat(format, Locale.US);
    }

    public static String getCurrentDate(String format){
        return getSdf(format).format(new Date().getTime());
    }

    public static String getTimeAgo(long seconds) {
        if (seconds < 60) {
            return seconds + "s ago";
        } else if (seconds < 3600) {
            return "" + (seconds / 60) + "m ago";
        } else if (seconds < 86400) {
            return "" + (seconds / 3600) + "h ago";
        } else if (seconds < 604800) {
            return "" + (seconds / 86400) + "d ago";
        } else if (seconds < 2419200){
            return "" + (seconds / 604800) + "w ago";
        } else if(seconds < 29030400){
            return "" + (seconds / 2419200) + "M ago";
        } else {
            return "" + (seconds / 29030400) + "Y ago";
        }
    }

    public static boolean isToday(long when) {
        Time time = new Time();
        time.set(when);
        int thenYear = time.year;
        int thenMonth = time.month;
        int thenMonthDay = time.monthDay;
        time.set(System.currentTimeMillis());
        return (thenYear == time.year)
                && (thenMonth == time.month)
                && (thenMonthDay == time.monthDay);
    }

    public static boolean hasSameDate(long millisFirst, long millisSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(millisFirst).equals(dateFormat.format(millisSecond));
    }

    public static int getNumberOfDaysPassed(long dateNew, long dateOld){
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(dateNew);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(dateOld);
        int yearDiff = calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR);
        if(yearDiff >= 0){
            int days = calendar1.get(Calendar.DAY_OF_YEAR) - calendar2.get(Calendar.DAY_OF_YEAR);
            days = days + yearDiff*360;
            return days;
        }
        else{
            return 0;
        }
    }

    //--------------------------------------Timer--------------------------------------------------------------

    public interface TimerTick{
        void onTick(long remainingMillis);
    }

    private static Handler handler;
    private static Runnable runnable;
    private static long remaining;

    public static void startTimer(final long intervalMillis, final long stopMillis, final TimerTick timerTick) {
        remaining = stopMillis;
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if(stopMillis == -1) {
                    timerTick.onTick(remaining);
                    // never stop
                    handler.postDelayed(runnable, intervalMillis);
                }
                else{
                    timerTick.onTick(remaining);
                    // stop after time finish
                    remaining = remaining - intervalMillis;
                    if(remaining < 0){
                        stopTimer();
                        return;
                    }
                    handler.postDelayed(runnable, intervalMillis);
                }
            }
        };
        handler.postDelayed(runnable, intervalMillis);
    }

    public static void stopTimer(){
        if(handler != null){
            handler.removeCallbacks(runnable);
            handler = null;
            runnable = null;
        }
    }
}

