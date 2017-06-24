package com.joedarby.alcosensing1.Display;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.joedarby.alcosensing1.R;

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.ConsentDocumentStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.ui.step.layout.StepLayout;
import org.researchstack.backbone.ui.views.SubmitBar;

import rx.functions.Action1;


public class InfoStepLayout extends LinearLayout implements StepLayout {

    private StepCallbacks callbacks;

    private String confirmationDialogBody;
    private String htmlContent;

    private ConsentDocumentStep step;
    private StepResult<Boolean> stepResult;

    public InfoStepLayout(Context context)
    {
        super(context);
    }

    public InfoStepLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public InfoStepLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initialize(Step step, StepResult result)
    {
        this.step = (ConsentDocumentStep) step;
        this.confirmationDialogBody = ((ConsentDocumentStep) step).getConfirmMessage();
        this.htmlContent = ((ConsentDocumentStep) step).getConsentHTML();
        this.stepResult = result;

        if(stepResult == null)
        {
            stepResult = new StepResult<>(step);
        }

        initializeStep();
    }

    @Override
    public View getLayout()
    {
        return this;
    }

    @Override
    public boolean isBackEventConsumed()
    {
        stepResult.setResult(false);
        callbacks.onSaveStep(StepCallbacks.ACTION_PREV, step, stepResult);
        return false;
    }

    @Override
    public void setCallbacks(StepCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    private void initializeStep()
    {
        setOrientation(VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.my_consent_step_layout, this, true);

        final WebView pdfView = (WebView) findViewById(R.id.my_consent_layout_webview);
        final SubmitBar submitBar = (SubmitBar) findViewById(org.researchstack.backbone.R.id.submit_bar);
        pdfView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        pdfView.loadData(htmlContent, "text/html; charset=UTF-8", null);

        pdfView.post(new Runnable() {
            @Override
            public void run() {
                int height = pdfView.getMeasuredHeight();
                int scrollHeight = findViewById(R.id.scrollView).getMeasuredHeight() - submitBar.getMeasuredHeight();
                if(height > 100 && scrollHeight > height) {
                    pdfView.getLayoutParams().height = scrollHeight;
                    pdfView.requestLayout();
                } else if (height < 100) {
                    pdfView.postDelayed(this, 10);
                }
            }
        });

        View submitNegative = submitBar.findViewById(org.researchstack.backbone.R.id.bar_submit_negative);
        submitNegative.setVisibility(GONE);

        submitBar.setPositiveAction(new Action1() {
            @Override
            public void call(Object v) {
                stepResult.setResult(true);
                callbacks.onSaveStep(StepCallbacks.ACTION_NEXT, step, stepResult);
            }
        });
        /*submitBar.setNegativeAction(new Action1() {
            @Override
            public void call(Object v) {
                stepResult.setResult(true);
                callbacks.onSaveStep(StepCallbacks.ACTION_NEXT, step, stepResult);
            }
        });*/
    }

}
