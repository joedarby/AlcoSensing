package com.joedarby.alcosensing1.Helpers;


import android.text.format.DateUtils;

public class TimeConstants {

    //20 minute testing, 3 hours real
    public static final long SENSING_PERIOD = DateUtils.HOUR_IN_MILLIS * 3;

    //5 minute testing, 2 hours real
    public static final long PERIODIC_SENSING_ATTEMPT_INTERVAL = DateUtils.HOUR_IN_MILLIS * 2;

    //5 minute for testing, 18 hours real life
    public static long GEOFENCING_RESET_DELAY_AFTER_SENSING = DateUtils.HOUR_IN_MILLIS * 18;

    //2 minutes testing, 5 minutes real
    public static long GEOFENCING_RESET_DELAY_AFTER_FAILURE = DateUtils.MINUTE_IN_MILLIS * 10;

    //5 minutes for testing, 28 hours real life
    public static final long PERIOD_ALARM_RESET_DELAY = DateUtils.HOUR_IN_MILLIS *28;

    //5 minutes testing, 15 minutes real?
    public static final long DATA_UPLOAD_RETRY_DELAY = DateUtils.MINUTE_IN_MILLIS *15;

    //ALSO SET SURVEY DELAY IN scheduleSurvey() of ContinuousSensingService
    //ALSO SET SENSING ABORT TIME PERIOD IN NOTIFICATION

}
