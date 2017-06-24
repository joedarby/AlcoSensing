package com.joedarby.alcosensing1.Data;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.joedarby.alcosensing1.R;
import com.joedarby.alcosensing1.Services.PostDataNoSurveyService;

public class YesNoDialogActivity extends AppCompatActivity {

    private static String end;
    private static String start;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        start = getIntent().getStringExtra("start");
        end = getIntent().getStringExtra("end");

        DialogFragment dialogFrag = new YesNoDialogFragment();
        dialogFrag.show(getSupportFragmentManager(), "YesNo");

    }

    public static class YesNoDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setIcon(R.mipmap.pint_glass)
                    .setTitle("AlcoSensing")
                    .setMessage((String.format("From %s until %s.\nDid you drink any alcohol between these times?", start, end)))
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            Intent surveyIntent = new Intent(getContext(), PostDataSurveyActivity.class);
                            surveyIntent.putExtra("start", start);
                            surveyIntent.putExtra("end", end);
                            startActivity(surveyIntent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            Intent noSurveyIntent = new Intent(getContext(), PostDataNoSurveyService.class);
                            getContext().startService(noSurveyIntent);
                            Intent mainIntent = new Intent(getContext(), MainActivity.class);
                            startActivity(mainIntent);
                        }
                    });

            return builder.create();
        }
    }
}
