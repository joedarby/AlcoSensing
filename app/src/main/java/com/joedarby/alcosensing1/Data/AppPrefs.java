package com.joedarby.alcosensing1.Data;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AppPrefs
{
    private static final String HAS_CONSENTED     = "HAS_CONSENTED";
    private static final String CONSENT_AWAITING_PROCESS     = "CONSENT_AWAITING_PROCESS";
    private static final String CONSENT_DATE     = "CONSENT_DATE";
    private static final String PRIZE_OPT     = "PRIZE_OPT";
    private static final String SURVEY_RESPONSE_COUNT = "SURVEY_RESPONSE_COUNT";
    private static final String SURVEY_PENDING = "SURVEY_PENDING";
    private static final String DATA_UPLOAD_PENDING = "DATA_UPLOAD_PENDING";
    private static final String USER_ID = "USER_ID";
    private static final String CONSENT_UPLOAD_PENDING = "CONSENT_UPLOAD_PENDING";
    private static final String SENSING = "SENSING";
    private static final String SENSING_USER_TRIGGERED = "SENSING_USER_TRIGGERED";
    private static final String START_TIME = "START_TIME";
    private static final String END_TIME = "END_TIME";
    private static final String VERSION = "VERSION";



    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Statics
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private static AppPrefs instance;

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Field Vars
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private final SharedPreferences prefs;

    private AppPrefs(Context context)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static synchronized AppPrefs getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new AppPrefs(context);
        }
        return instance;
    }

    public boolean hasConsented()
    {
        return prefs.getBoolean(HAS_CONSENTED, false);
    }

    public void setHasConsented(boolean consented)
    {
        prefs.edit().putBoolean(HAS_CONSENTED, consented).commit();
    }

    public String getConsentDate()
    {
        return prefs.getString(CONSENT_DATE, "");
    }

    public void setConsentDate(String date)
    {
        prefs.edit().putString(CONSENT_DATE, date).commit();
    }

    public boolean getPrizeOpt() { return prefs.getBoolean(PRIZE_OPT, false);}

    public void setPrizeOpt(boolean opt) { prefs.edit().putBoolean(PRIZE_OPT, opt);}


    public int getResponseCount()
    {
        return prefs.getInt(SURVEY_RESPONSE_COUNT, 0);
    }

    public void setResponseCount(int count)
    {
        prefs.edit().putInt(SURVEY_RESPONSE_COUNT, count).commit();
    }
    public void incrementResponseCount()
    {
        int count = getResponseCount();
        count += 1;
        prefs.edit().putInt(SURVEY_RESPONSE_COUNT, count).commit();
    }

    public boolean isSurveyPending() { return  prefs.getBoolean(SURVEY_PENDING, false); }

    public void setIsSurveyPending(boolean pending) {
        prefs.edit().putBoolean(SURVEY_PENDING, pending).commit();
    }

    public boolean isConsentAwaitingProcess() { return  prefs.getBoolean(CONSENT_AWAITING_PROCESS, false); }

    public void setIsConsentAwaitingProcess(boolean pending) {
        prefs.edit().putBoolean(CONSENT_AWAITING_PROCESS, pending).commit();
    }

    public boolean isDataUploadPending() { return  prefs.getBoolean(DATA_UPLOAD_PENDING, false); }

    public void setIsDataUploadPending(boolean pending) {
        prefs.edit().putBoolean(DATA_UPLOAD_PENDING, pending).commit();
    }

    public boolean isConsentUploadPending() { return  prefs.getBoolean(CONSENT_UPLOAD_PENDING, false); }

    public void setIsConsentUploadPending(boolean pending) {
        prefs.edit().putBoolean(CONSENT_UPLOAD_PENDING, pending).commit();
    }

    public boolean isSensingTriggeredByUser() { return  prefs.getBoolean(SENSING_USER_TRIGGERED, false); }

    public void setSensingTriggeredByUser(boolean triggered) {
        prefs.edit().putBoolean(SENSING_USER_TRIGGERED, triggered).commit();
    }

    public boolean isSensing() { return  prefs.getBoolean(SENSING, false); }

    public void setIsSensing(boolean sensing) {
        prefs.edit().putBoolean(SENSING, sensing).commit();
    }

    public String getUserID() { return prefs.getString(USER_ID, "");}

    public void setUserID() {
        String id = Long.toHexString(Double.doubleToLongBits(Math.random()));
        prefs.edit().putString(USER_ID, id).commit();
    }

    public String getSensingStartTime() { return prefs.getString(START_TIME, "");}

    public void setSensingStartTime(String time) {
        prefs.edit().putString(START_TIME, time).commit();
    }

    public long getSensingEndTime() { return prefs.getLong(END_TIME, 0);}

    public void setSensingEndTime(long time) {
        prefs.edit().putLong(END_TIME, time).commit();
    }

    public boolean isAppVersionOK() { return  prefs.getBoolean(VERSION, true); }

    public void setIsAppVersionOK(boolean isOK) {
        prefs.edit().putBoolean(VERSION, isOK).commit();
    }


}