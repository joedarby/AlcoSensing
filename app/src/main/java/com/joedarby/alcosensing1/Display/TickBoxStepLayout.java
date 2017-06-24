package com.joedarby.alcosensing1.Display;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.ui.step.body.BodyAnswer;
import org.researchstack.backbone.ui.step.body.StepBody;
import org.researchstack.backbone.ui.step.layout.StepLayout;
import org.researchstack.backbone.ui.step.layout.SurveyStepLayout;
import org.researchstack.backbone.ui.views.FixedSubmitBarLayout;
import org.researchstack.backbone.ui.views.SubmitBar;
import org.researchstack.backbone.utils.LogExt;
import org.researchstack.backbone.utils.TextUtils;

import java.lang.reflect.Constructor;

import rx.functions.Action1;

public class TickBoxStepLayout extends FixedSubmitBarLayout implements StepLayout {

    public static final String TAG = SurveyStepLayout.class.getSimpleName();

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Data used to initializeLayout and return
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private TickBoxStep questionStep;
    private StepResult stepResult;

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Communicate w/ host
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private StepCallbacks callbacks;

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Child Views
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private LinearLayout container;
    private StepBody stepBody;

    public TickBoxStepLayout(Context context) {
        super(context);
    }

    public TickBoxStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TickBoxStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(Step step) {
        initialize(step, null);
    }

    @Override
    public void initialize(Step step, StepResult result) {
        if (!(step instanceof QuestionStep)) {
            throw new RuntimeException("Step being used in SurveyStep is not a QuestionStep");
        }

        this.questionStep = (TickBoxStep) step;
        this.stepResult = result;

        initializeStep();
    }

    @Override
    public View getLayout() {
        return this;
    }

    /**
     * Method allowing a step to consume a back event.
     *
     * @return
     */
    @Override
    public boolean isBackEventConsumed() {
        callbacks.onSaveStep(StepCallbacks.ACTION_PREV, getStep(), stepBody.getStepResult(false));
        return false;
    }

    @Override
    public void setCallbacks(StepCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public int getContentResourceId() {
        return org.researchstack.backbone.R.layout.rsb_step_layout;
    }

    public void initializeStep() {
        initStepLayout();
        initStepBody();
    }

    public void initStepLayout() {
        LogExt.i(getClass(), "initStepLayout()");

        container = (LinearLayout) findViewById(org.researchstack.backbone.R.id.rsb_survey_content_container);
        TextView title = (TextView) findViewById(org.researchstack.backbone.R.id.rsb_survey_title);
        TextView summary = (TextView) findViewById(org.researchstack.backbone.R.id.rsb_survey_text);
        final SubmitBar submitBar = (SubmitBar) findViewById(org.researchstack.backbone.R.id.rsb_submit_bar);
        submitBar.post(new Runnable() {
            @Override
            public void run() {
                submitBar.setTranslationY(0);
            }
        });
        submitBar.setVisibility(VISIBLE);
        submitBar.setPositiveAction(new Action1() {
            @Override
            public void call(Object o) {
                onNextClicked();
            }
        });

        if (questionStep != null) {
            if (!TextUtils.isEmpty(questionStep.getTitle())) {
                title.setVisibility(View.VISIBLE);
                title.setText(questionStep.getTitle());
            }

            if (!TextUtils.isEmpty(questionStep.getText())) {
                summary.setVisibility(View.VISIBLE);
                summary.setText(Html.fromHtml(questionStep.getText()));
                summary.setMovementMethod(new LinkMovementMethod());
            }

            if (questionStep.isOptional()) {
                submitBar.setNegativeTitle(org.researchstack.backbone.R.string.rsb_step_skip);
                //submitBar.setNegativeAction(v -> onSkipClicked());
            } else {
                submitBar.getNegativeActionView().setVisibility(View.GONE);
            }
        }
    }

    public void initStepBody() {
        LogExt.i(getClass(), "initStepBody()");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        stepBody = createStepBody(questionStep, stepResult);
        View body = stepBody.getBodyView(StepBody.VIEW_TYPE_DEFAULT, inflater, this);

        if (body != null) {
            View oldView = container.findViewById(org.researchstack.backbone.R.id.rsb_survey_step_body);
            int bodyIndex = container.indexOfChild(oldView);
//            container.removeView(oldView);
            oldView.setVisibility(View.GONE);
            container.addView(body, bodyIndex);
//            body.setId(org.researchstack.backbone.R.id.rsb_survey_step_body);
        }
    }

    @NonNull
    private StepBody createStepBody(QuestionStep questionStep, StepResult result) {
        try {
            Class cls = questionStep.getStepBodyClass();
            Constructor constructor = cls.getConstructor(Step.class, StepResult.class);
            return (StepBody) constructor.newInstance(questionStep, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        callbacks.onSaveStep(StepCallbacks.ACTION_NONE, getStep(), stepBody.getStepResult(false));
        return super.onSaveInstanceState();
    }

    protected void onNextClicked() {
        BodyAnswer bodyAnswer = stepBody.getBodyAnswerState();

        if (questionStep.isCanBeUnticked()) {
            callbacks.onSaveStep(StepCallbacks.ACTION_NEXT,
                    getStep(),
                    stepBody.getStepResult(false));
        } else if (bodyAnswer == null || !bodyAnswer.isValid()) {
            new AlertDialog.Builder(getContext()).setTitle("Sorry")
                    .setMessage(questionStep.getDeclineText())
                    .setCancelable(false)
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //stepResult.setResult(false);
                                    //callbacks.onSaveStep(StepCallbacks.ACTION_NEXT, questionStep, stepResult);
                                }
                            }

                    )
                    .show();
        } else {
            callbacks.onSaveStep(StepCallbacks.ACTION_NEXT,
                    getStep(),
                    stepBody.getStepResult(false));
        }
    }

    public void onSkipClicked() {
        if (callbacks != null) {
            // empty step result when skipped
            callbacks.onSaveStep(StepCallbacks.ACTION_NEXT,
                    getStep(),
                    stepBody.getStepResult(true));
        }
    }

    public Step getStep() {
        return questionStep;
    }

    public String getString(@StringRes int stringResId) {
        return getResources().getString(stringResId);
    }
}