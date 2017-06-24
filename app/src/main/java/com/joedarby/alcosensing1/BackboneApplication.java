package com.joedarby.alcosensing1;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.joedarby.alcosensing1.Data.AppPrefs;
import com.joedarby.alcosensing1.Helpers.AlarmHelper;
import com.joedarby.alcosensing1.Helpers.StatusHelper;
import com.joedarby.alcosensing1.Services.ConsentUpload;
import com.joedarby.alcosensing1.Services.ContinuousSensingService;
import com.joedarby.alcosensing1.Services.DataUpload;
import com.joedarby.alcosensing1.Services.MyExceptionHandler;

import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.storage.database.AppDatabase;
import org.researchstack.backbone.storage.database.sqlite.DatabaseHelper;
import org.researchstack.backbone.storage.file.EncryptionProvider;
import org.researchstack.backbone.storage.file.FileAccess;
import org.researchstack.backbone.storage.file.PinCodeConfig;
import org.researchstack.backbone.storage.file.SimpleFileAccess;
import org.researchstack.backbone.storage.file.UnencryptedProvider;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import io.fabric.sdk.android.Fabric;


public class BackboneApplication extends MultiDexApplication {

    private static BackboneApplication instance;


    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
        Fabric.with(this, new Crashlytics());

        recoverBadlyEndedSensing(this);
        StatusHelper.updateAppVersionStatus(this);

        Log.e("BackboneApplication", "onCreate");

        // Customize your pin code preferences
        PinCodeConfig pinCodeConfig = new PinCodeConfig(); // default pin config (4-digit, 1 min lockout)

        // Customize encryption preferences
        EncryptionProvider encryptionProvider = new UnencryptedProvider(); // No pin, no encryption

        // If you have special file handling needs, implement FileAccess
        FileAccess fileAccess = new SimpleFileAccess();

        // If you have your own custom database, implement AppDatabase
        AppDatabase database = new DatabaseHelper(this,
                DatabaseHelper.DEFAULT_NAME,
                null,
                DatabaseHelper.DEFAULT_VERSION);

        StorageAccess.getInstance().init(pinCodeConfig, encryptionProvider, fileAccess, database);
    }

    public static Context getContext() { return instance.getApplicationContext();}

    public static void appRecoverySanityChecks(Context context) {
        String tag = "Sanity Checks";
        AppPrefs prefs = AppPrefs.getInstance(context);

        if (prefs.hasConsented() && prefs.isAppVersionOK()) {

            if (prefs.isConsentUploadPending()) {
                Intent consentIntent = new Intent(context, ConsentUpload.class);
                context.startService(consentIntent);
            }
            if (prefs.isSurveyPending()) {
                ContinuousSensingService.scheduleSurvey(context);
                Log.e(tag, "Survey triggered line 100");
            } else if (prefs.isDataUploadPending()) {
                Intent dataUploadIntent = DataUpload.getIntent();
                context.startService(dataUploadIntent);
            } else {
                AlarmHelper.get().resetSensingAlarmsAfterSensing(prefs.getSensingEndTime());
                Log.e(tag, "Call to set sensing alarms");
            }
        }

    }

    private static void recoverBadlyEndedSensing(Context context) {
        AppPrefs prefs = AppPrefs.getInstance(context);
        if (prefs.isSensing() && !StatusHelper.isSensingServiceRunning(context)) {
            Log.e("Application", "Sensing was badly ended, recovering...");
            prefs.setIsSensing(false);
            prefs.setSensingTriggeredByUser(false);

            File folder = new File(context.getFilesDir().getAbsolutePath() + "/SensorData/data/");
            File fileList[] = folder.listFiles();

            if (fileList.length > 0) {
                Comparator<File> comp = new Comparator<File>() {
                    @Override
                    public int compare(File lhs, File rhs) {
                        return lhs.length() < rhs.length() ? 1 : lhs.length() == rhs.length() ? 0 : -1;
                    }
                };
                Arrays.sort(fileList, comp);
                prefs.setSensingEndTime(fileList[0].lastModified());
                ContinuousSensingService.compressSensorData(context);
                ContinuousSensingService.scheduleSurvey(context);
            } else {
                for (final File child : folder.listFiles()) {
                    child.delete();
                }
            }
        }
    }
}
