package com.joedarby.alcosensing1.Services;



import com.joedarby.alcosensing1.Helpers.AlarmHelper;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {


    public MyExceptionHandler() {}


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        AlarmHelper.get().setEmptyRestartAlarm();

        System.exit(2);
    }
}
