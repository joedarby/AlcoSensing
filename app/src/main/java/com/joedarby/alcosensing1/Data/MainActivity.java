package com.joedarby.alcosensing1.Data;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.joedarby.alcosensing1.R;
import com.joedarby.alcosensing1.Helpers.AlarmHelper;
import com.joedarby.alcosensing1.Services.ConsentUpload;
import com.joedarby.alcosensing1.Services.ContinuousSensingService;
import com.joedarby.alcosensing1.Services.SensingTriggerService;
import com.joedarby.alcosensing1.Helpers.StatusHelper;

import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.ui.PinCodeActivity;
import org.researchstack.backbone.ui.ViewTaskActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public class MainActivity extends PinCodeActivity
{
    // Activity Request Codes
    private static final int REQUEST_CONSENT = 0;
    private static final int REQUEST_SHOW_INFO = 1;
    private static final int REQUEST_SHOW_CONSENT = 2;
    private static final int REQUEST_SHOW_PRIZE_INFO = 3;


    // Task/Step Identifiers
    public static final  String AGE                       = "age";
    public static final  String EMAIL                     = "email_address";
    public static final  String HEIGHT                    = "height";
    public static final  String WEIGHT                    = "weight";
    public static final  String GENDER                    = "gender";
    public static final  String MOBILITY                  = "mobility";
    public static final  String MOBILITY_DETAIL           = "mobility_detail";
    public static final  String CONSENT                   = "consent";
    public static final  String SHOW_INFO                 = "show_info";
    public static final  String SHOW_CONSENT              = "show_consent";
    public static final  String SHOW_PRIZE                = "show_prize";

    private static final long SPLASH_DELAY = 700;

    private static final int MY_PERMISSIONS_LOCATION = 0;

    private AppPrefs prefs;
    private Intent resultDataIntent;
    private Button drinkingButton;
    private WebView statusMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        prefs = AppPrefs.getInstance(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!prefs.hasConsented()) {
                    UserRegistration.launchConsent(MainActivity.this);
                } else {
                    getPermissions();
                }
            }
        }, SPLASH_DELAY);
    }

    private void showStatus() {

        View lblConsentedDate = findViewById(R.id.consented_date_lbl);
        TextView consentedDate = (TextView)findViewById(R.id.consent_date);
        ImageView mainImage = (ImageView) findViewById(R.id.main_image);
        mainImage.setImageResource(R.mipmap.large_pint_glass);
        TextView lblProgress = (TextView) findViewById(R.id.progress_lbl);
        TextView progress = (TextView) findViewById(R.id.progress);
        TextView warningMessage = (TextView) findViewById(R.id.wifi_message);
        statusMessage = (WebView) findViewById(R.id.advice_text);
        drinkingButton = (Button) findViewById(R.id.drinking_button);
        drinkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drinkingButtonClick();
            }
        });
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        consentedDate.setVisibility(View.VISIBLE);
        lblConsentedDate.setVisibility(View.VISIBLE);
        lblProgress.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        drinkingButton.setVisibility(View.VISIBLE);
        setStatusScreenMessage();

        int progressPct = prefs.getResponseCount() < 10 ? prefs.getResponseCount() * 10 : 100;
        progressBar.setProgress(progressPct);

        consentedDate.setText(prefs.getConsentDate());
        String progressText = Integer.toString(prefs.getResponseCount()) + "/10";
        progress.setText(progressText);

        if (prefs.isDataUploadPending()) {
            if (StatusHelper.checkWifiOnAndConnected(this)) {
                warningMessage.setVisibility(View.INVISIBLE);
            } else {
                warningMessage.setVisibility(View.VISIBLE);
                warningMessage.setText(getString(R.string.wifi_warning));
            }
        } else if (prefs.isSurveyPending()) {
            warningMessage.setVisibility(View.VISIBLE);
            warningMessage.setText(getString(R.string.survey_warning));
            ContinuousSensingService.scheduleSurvey(this);
        }
        else {
            warningMessage.setVisibility(View.INVISIBLE);
        }

        if (prefs.isDataUploadPending() || prefs.isSurveyPending() || prefs.isSensingTriggeredByUser()) {
            drinkingButton.setEnabled(false);
        } else {
            drinkingButton.setEnabled(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CONSENT && resultCode == RESULT_OK)
        {
            prefs.setHasConsented(true);
            resultDataIntent = data;
            prefs.setIsConsentAwaitingProcess(true);
            getPermissions();

        }
        else if (requestCode == REQUEST_SHOW_INFO) {
            showStatus();
        }
        else if (requestCode == REQUEST_SHOW_CONSENT) {
            showStatus();
        }
        else if (requestCode == REQUEST_SHOW_PRIZE_INFO) {
            showStatus();
        }

    }


    private void processConsentResult(TaskResult result)
    {
        Map<String, StepResult> resultMap = result.getResults();

        StorageAccess.getInstance().getAppDatabase().saveTaskResult(result);

        AppPrefs prefs = AppPrefs.getInstance(this);
        prefs.setUserID();
        prefs.setResponseCount(0);
        String UID = prefs.getUserID();
        Crashlytics.setUserIdentifier(UID);

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String date = format.format(new Date());

        prefs.setConsentDate(date);

        ConsentData data = new ConsentData(resultMap, UID);
        prefs.setPrizeOpt(data.inPrizeDraw);
        String fileName = prefs.getUserID() + "-";

        Gson gson = new Gson();
        File folder = new File(getFilesDir().getAbsolutePath()+ "/SensorData/data/");
        File consentFile = new File(folder, fileName + "consent.json");

        try {
            if (!folder.exists())
                folder.mkdirs();
            consentFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(consentFile);
            stream.write(gson.toJson(data).getBytes());
            stream.close();
            prefs.setIsConsentUploadPending(true);
        } catch (IOException e) {
            Log.e("ConsentResult", "File Not created!!!!", e);
        }

        Intent consentIntent = new Intent(this, ConsentUpload.class);
        startService(consentIntent);

        AlarmHelper.get().resetSensingAlarmsAfterSensing(prefs.getSensingEndTime());

        prefs.setIsConsentAwaitingProcess(false);

        showStatus();

    }


    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO};
                ActivityCompat.requestPermissions(this,
                        permissions,
                        MY_PERMISSIONS_LOCATION);
            } else {
                showStatus();
            }
        } else {
            if (prefs.isConsentAwaitingProcess()) {
                processConsentResult((TaskResult) resultDataIntent.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT));
            } else {
                showStatus();
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length == 4
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
                && grantResults[3] == PackageManager.PERMISSION_GRANTED) {



            if (prefs.isConsentAwaitingProcess()) {
                processConsentResult((TaskResult) resultDataIntent.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT));
            } else {
                showStatus();
            }


        } else {
            DialogFragment dialogFrag = new PermissionsDialogFragment();
            dialogFrag.show(getSupportFragmentManager(), "permissions");
        }
    }


    public static class PermissionsDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setIcon(R.mipmap.pint_glass)
                    .setTitle("Permissions")
                    .setMessage("AlcoSensing needs all requested permissions to work properly.\n\nRemoving any permissions will cause the app to fail.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            MainActivity main = (MainActivity) getActivity();
                            main.getPermissions();
                        }
                    });

            return builder.create();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                UserRegistration.reshowConsent(this);
                return true;

            case R.id.action_settings2:
                UserRegistration.reshowInfo(this);
                return true;

            case R.id.action_settings3:
                UserRegistration.reShowPrizeDraw(this);
                return true;

            case R.id.action_settings4:
                Intent creditsIntent = new Intent(this, CreditsActivity.class);
                startActivity(creditsIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void drinkingButtonClick() {
        if (StatusHelper.getBatteryLevel(this) != 0) {
            CharSequence text = "Battery too low to start sensing right now";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        } else {
            Intent sensingTriggerIntent = new Intent(this, SensingTriggerService.class);
            sensingTriggerIntent.putExtra("UserTriggered", true);
            startService(sensingTriggerIntent);
            drinkingButton.setEnabled(false);
        }
    }

    private void setStatusScreenMessage() {
        Date endDate = new GregorianCalendar(2017,8,5).getTime();
        Date today = new Date();

        if (today.compareTo(endDate) < 0) {
            if (prefs.getResponseCount() < 10) {
                statusMessage.loadData(getString(R.string.status_screen_advice), "text/html; charset=utf-8", "\"UTF-8\"");
            } else {
                statusMessage.loadData(getString(R.string.full_responses), "text/html; charset=utf-8", "\"UTF-8\"");
            }
        } else {
            statusMessage.loadData(getString(R.string.study_end), "text/html; charset=utf-8", "\"UTF-8\"");
        }

        statusMessage.setVisibility(View.VISIBLE);
        statusMessage.setScrollbarFadingEnabled(false);
        statusMessage.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);


    }
}


