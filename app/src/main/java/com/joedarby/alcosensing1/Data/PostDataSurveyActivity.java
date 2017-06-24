package com.joedarby.alcosensing1.Data;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.gson.Gson;
import com.joedarby.alcosensing1.R;
import com.joedarby.alcosensing1.Services.DataUpload;

import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.ui.PinCodeActivity;
import org.researchstack.backbone.ui.ViewTaskActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class PostDataSurveyActivity extends PinCodeActivity {

    // Task/Step Identifiers
    public static final  String SURVEY                            = "survey";
    public static final  String SURVEY_STEP                       = "survey_step";
    public static final  String BEER_PINT                         = "beer_pint";
    public static final  String BEER_BOTTLE                       = "beer_bottle";
    public static final  String WINE_LARGE                        = "wine_large";
    public static final  String WINE_STANDARD                     = "wine_standard";
    public static final  String SPIRIT_SINGLE                     = "spirit_single";
    public static final  String SPIRIT_DOUBLE                     = "spirit_double";
    public static final  String FEELING                           = "feeling";

    public static final int REQUEST_SURVEY                        = 0;

    private AppPrefs prefs;
    private SurveyData surveyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);

        setContentView(R.layout.activity_post_data_survey);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);

        prefs = AppPrefs.getInstance(this);

        String start = getIntent().getStringExtra("start");
        String end = getIntent().getStringExtra("end");

        String timePeriod = String.format("From %s until %s:", start, end);
        new Survey(timePeriod).launchSurvey(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_SURVEY && resultCode == RESULT_OK)
        {
            TaskResult result = (TaskResult) data.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT);
            StorageAccess.getInstance().getAppDatabase().saveTaskResult(result);
            StepResult surveyResult = result.getStepResult(SURVEY_STEP);
            surveyData = new SurveyData(surveyResult, this);
            float units = surveyData.units;

            if (units > 14) {
                showDialog();
            } else {
                processSurveyResult();
            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void processSurveyResult()
    {
        prefs.incrementResponseCount();

        String fileName = prefs.getUserID() + "-" + prefs.getSensingStartTime() + "-";

        prefs.setIsSurveyPending(false);

        Gson gson = new Gson();
        File file = new File(getFilesDir().getAbsolutePath() + "/SensorData/data/", fileName + "SurveyResult.json");

        try {
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(gson.toJson(surveyData).getBytes());
            stream.close();
        } catch (IOException e) {
            Log.e("Survey File", "Not created", e);
        }

        prefs.setIsDataUploadPending(true);

        Intent uploadIntent = DataUpload.getIntent();
        startService(uploadIntent);

    }

    private void showDialog() {
        DialogInterface.OnClickListener dialogOnClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                processSurveyResult();
            }
        };
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
            .setIcon(R.mipmap.pint_glass)
            .setTitle("AlcoSensing")
            .setMessage(R.string.drink_dialog_text)
            .setPositiveButton("OK", dialogOnClick);
        dialogBuilder.show();
    }

}
