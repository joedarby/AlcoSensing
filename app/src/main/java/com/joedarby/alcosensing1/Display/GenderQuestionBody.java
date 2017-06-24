package com.joedarby.alcosensing1.Display;

import android.content.res.Resources;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.joedarby.alcosensing1.R;

import org.researchstack.backbone.answerformat.TextAnswerFormat;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.step.body.BodyAnswer;
import org.researchstack.backbone.ui.step.body.TextQuestionBody;
import org.researchstack.backbone.utils.TextUtils;
import org.researchstack.backbone.utils.ViewUtils;

import rx.functions.Action1;

public class GenderQuestionBody extends TextQuestionBody {

    private EditText editText;
    private GenderStep step;
    private StepResult<String> result;

    public GenderQuestionBody(Step step, StepResult result) {
        super(step,result);
        this.step = (GenderStep) step;
        this.result = result == null ? new StepResult<>(step) : result;
    }

    @Override
    public View getBodyView(int viewType, LayoutInflater inflater, ViewGroup parent) {
        View body = inflater.inflate(R.layout.gender_question, parent, false);

        editText = (EditText) body.findViewById(R.id.value);
        if(step.getPlaceholder() != null)
        {
            editText.setHint(step.getPlaceholder());
        }
        else
        {
            editText.setHint(org.researchstack.backbone.R.string.rsb_hint_step_body_text);
        }

        TextView title = (TextView) body.findViewById(R.id.label);
        TextView subtitle = (TextView) body.findViewById(R.id.label2);

        if(viewType == VIEW_TYPE_COMPACT)
        {
            title.setText(step.getTitle());
            subtitle.setText(step.getSubtitle());
        }
        else
        {
            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
        }

        // Restore previous result
        String stringResult = result.getResult();
        if(! TextUtils.isEmpty(stringResult))
        {
            editText.setText(stringResult);
        }

        // Set result on text change

        RxTextView.textChanges(editText).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                result.setResult(charSequence.toString());
            }
        });

        // Format EditText from TextAnswerFormat
        TextAnswerFormat format = (TextAnswerFormat) step.getAnswerFormat();

        editText.setSingleLine(! format.isMultipleLines());

        if(format.getMaximumLength() > TextAnswerFormat.UNLIMITED_LENGTH)
        {
            InputFilter.LengthFilter maxLengthFilter = new InputFilter.LengthFilter(format.getMaximumLength());
            InputFilter[] filters = ViewUtils.addFilter(editText.getFilters(), maxLengthFilter);
            editText.setFilters(filters);
        }

        Resources res = parent.getResources();
        LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = res.getDimensionPixelSize(org.researchstack.backbone.R.dimen.rsb_margin_left);
        layoutParams.rightMargin = res.getDimensionPixelSize(org.researchstack.backbone.R.dimen.rsb_margin_right);
        body.setLayoutParams(layoutParams);

        return body;
    }

    @Override
    public BodyAnswer getBodyAnswerState() {
        return BodyAnswer.VALID;
    }
}
