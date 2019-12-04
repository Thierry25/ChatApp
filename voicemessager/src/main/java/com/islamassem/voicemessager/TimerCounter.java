package com.islamassem.voicemessager;

import android.text.TextUtils;

import java.util.Timer;
import java.util.TimerTask;
public class TimerCounter {
    private Timer timer;
    private OnTimerTick OnTimerTick;
    private OnTimerFinished OnTimerFinished;
    private long time,timeConstant;
    private int passedSeconds;
    private TimerStatus timerStatus;
    private TimeType timeType;
    private int TimeToUpdate = 10000;//30 second
    private int executionInterval;
    String day="Day",hour="Hour",minute="Min",seconds="Sec";

    boolean numberFormat;
    public TimerCounter(OnTimerTick onTimerTick, TimeType timeType) {
        timerStatus=TimerStatus.notWorking;
        OnTimerTick = onTimerTick;
        switch (timeType){
            case Second:executionInterval=1;break;
            case Minute:executionInterval=30;break;
            case Hour:executionInterval=150;break;
            case Day:executionInterval=3600;break;
        }
    }

    public TimerCounter(long time, TimeType timeType, OnTimerTick onTimerTick, TimerCounter.OnTimerFinished onTimerFinished) {
        timerStatus=TimerStatus.notWorking;
        OnTimerTick = onTimerTick;
        OnTimerFinished = onTimerFinished;
        this.time=time;
        this.timeConstant=time;
        this.timeType=timeType;
        switch (timeType){
            case Second:executionInterval=1;break;
            case Minute:executionInterval=30;break;
            case Hour:executionInterval=150;break;
            case Day:executionInterval=3600;break;
        }
    }

    public static long getSeconds(long date, TimeType timeType) {
        switch (timeType) {
            case Day: {
                return date * 24 * 3600;
            }
            case Hour:{
                return date*3600;
            }
            case Minute:{
                return date*60;
            }
            case Second:{
                return date;
            }
            default: {
                return date;
            }
        }
    }


    public TimerStatus getTimerStatus() {
        if (timer == null)
            return TimerStatus.notWorking;
        return timerStatus;
    }

    public void setTimeToUpdate(int timeToUpdate) {
        TimeToUpdate = timeToUpdate;
    }

    public interface OnTimerTick {
        void onTimerTick(String time);
    }
    public interface OnTimerFinished{
        void onfinished(String time);
    }

    public void startTimer()
    {
        cancel();
        if(time!= 0L)
            startRegularTimer();
        else startInfinteTimer();
    }

    public boolean isNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(boolean numberFormat) {
        this.numberFormat = numberFormat;
    }

    private void startRegularTimer() {
        if (OnTimerTick == null && OnTimerFinished == null)
            return;
        cancel();
        passedSeconds = 0;
        if(time==0)OnTimerFinished.onfinished("");
        timer=new Timer();
        timerStatus=TimerStatus.started;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (time>1) {
                    OnTimerTick.onTimerTick(decreaseOneSecond());
                }
                else
                {
                    OnTimerFinished.onfinished("");
                    TimerCounter.this.cancel();
                    timerStatus=TimerStatus.finished;
                    time=timeConstant;
                }
                passedSeconds += 1;
            }
        }, 0, executionInterval*1000);
    }

    private void startInfinteTimer() {

        if (OnTimerTick == null)
            return;
        passedSeconds = 0;
        timer=new Timer();
        timerStatus=TimerStatus.started;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                OnTimerTick.onTimerTick(passedSeconds+"");
                passedSeconds += 1;
            }
        }, 0, executionInterval*1000);
    }

    public interface RequireUpdate {
        void onUpdate();
    }

    public void cancel() {
        timerStatus=TimerStatus.notWorking;
        try {
            if(timer!=null)
                timer.cancel();
            timer=null;
        }catch (Exception ignored){
            timer=null;
        }
    }

    public static String getTimeString(long time,TimeType type,boolean numberFormat) {
        time=getSeconds(time,type);
        int seconds = (int) (time % 60);
        int minutes = (int) ((time / 60) % 60);
        int hours = (int) ((time / 3600));
        //int days = (int) ((timeInSeconds / (86400)));
        return getTimeString(0, hours, minutes, seconds,numberFormat);
    }

    public long getTime() {
        return time;
    }

    private String decreaseOneSecond() {
        if(timeType!=TimeType.Second)return "";
        time -= 1;
        int seconds = (int) time;//(int) (time % 60);
        int minutes = (int) ((time / 60) % 60);//0;
        int hours =(int) ((time / 3600));// 0;//
        int days = (int) ((time / (86400)));
        if(time<=0)
            return getTimeString(0, 0, 0, 0,numberFormat);
        return getTimeString(0, hours, minutes, seconds,numberFormat);
    }
    private static String getTimeString(@SuppressWarnings("SameParameterValue") int day, int hour, int minute, int seconds,boolean numberFormat) {

        if(!numberFormat)
        {
            String days = day > 0 ? day + " "+day+ " " : "";
            String hours = hour > 0 ? hour+ " " +hour+ " " : "";
            String minutes = minute > 0 ? minute + " "+minute+ " " : "";
            String secondss = seconds > 0 ? seconds + " "+ seconds+" " : "";
            return days +hours +minutes +secondss;
        }else {
            String days = day >9 ? ""+day:(day>0?"0"+day:"");
            String hours = hour > 9 ?""+hour:(hour>0?"0"+hour:"");
            String minutes = minute > 9 ?""+minute:"0"+minute;
            String secondss = seconds > 9? ""+seconds:"0"+seconds;
            return (!TextUtils.isEmpty(days)?days+":"+hours:(TextUtils.isEmpty(hours)?hours:hours+":")) /**/+minutes/**/ +":"+secondss;
        }
    }

}
