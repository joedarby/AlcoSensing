package com.joedarby.alcosensing1.Display;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hrules.horizontalnumberpicker.HorizontalNumberPicker;
import com.joedarby.alcosensing1.R;

import org.researchstack.backbone.answerformat.IntegerAnswerFormat;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.step.body.BodyAnswer;
import org.researchstack.backbone.ui.step.body.StepBody;
import org.researchstack.backbone.utils.ViewUtils;

import static android.view.View.GONE;

public class DrinkQuestionBody implements StepBody {


    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    // Constructor Fields
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    private DrinkStep step;
    private StepResult<Integer> result;
    private IntegerAnswerFormat format;

    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    // View Fields
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    private int      viewType;
    private EditText editText;
    private Boolean showTitle;
    private HorizontalNumberPicker picker;

    public DrinkQuestionBody(Step step, StepResult result)
    {
        this.step = (DrinkStep) step;
        this.result = result == null ? new StepResult<>(step) : result;
        this.format = (IntegerAnswerFormat) this.step.getAnswerFormat();

    }

    @Override
    public View getBodyView(int viewType, LayoutInflater inflater, ViewGroup parent)
    {
        this.viewType = viewType;
        this.showTitle = step.displayWithTitle();

        View view = getViewForType(viewType, inflater, parent);

        Resources res = parent.getResources();
        LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = res.getDimensionPixelSize(org.researchstack.backbone.R.dimen.rsb_margin_left);
        layoutParams.rightMargin = res.getDimensionPixelSize(org.researchstack.backbone.R.dimen.rsb_margin_right);
        view.setLayoutParams(layoutParams);

        return view;
    }

    private View getViewForType(int viewType, LayoutInflater inflater, ViewGroup parent)
    {
        if(viewType == VIEW_TYPE_DEFAULT)
        {
            return initViewDefault(inflater, parent);
        }
        else if(viewType == VIEW_TYPE_COMPACT)
        {
            return initViewCompact(inflater, parent);
        }
        else
        {
            throw new IllegalArgumentException("Invalid View Type");
        }
    }

    private View initViewDefault(LayoutInflater inflater, ViewGroup parent)
    {
        View formItemView = inflater.inflate(R.layout.drink_edit_text, parent, false);

        if (showTitle) {
            TextView title = (TextView) formItemView.findViewById(R.id.label);
            title.setText(step.getTitle());
        }
        else {
            TextView title = (TextView) formItemView.findViewById(R.id.label);
            title.setVisibility(GONE);
        }

        TextView subTitle = (TextView) formItemView.findViewById(R.id.sub_label);
        subTitle.setText(step.getSubTitle());

        editText = (EditText) formItemView.findViewById(org.researchstack.backbone.R.id.value);
        setFilters(parent.getContext());

        return editText;
    }

    private View initViewCompact(LayoutInflater inflater, ViewGroup parent)
    {
        View formItemView = inflater.inflate(R.layout.drink_edit_text, parent, false);

        if (showTitle) {
            TextView title = (TextView) formItemView.findViewById(R.id.label);
            title.setText(step.getTitle());
        }
        else {
            TextView title = (TextView) formItemView.findViewById(R.id.label);
            title.setVisibility(GONE);
        }

        TextView subTitle = (TextView) formItemView.findViewById(R.id.sub_label);
        subTitle.setText(step.getSubTitle());

        //editText = (EditText) formItemView.findViewById(org.researchstack.backbone.R.id.value);
        //setFilters(parent.getContext());

        picker = (HorizontalNumberPicker) formItemView.findViewById(R.id.numberPicker);
        picker.setMinValue(0);
        picker.setMaxValue(30);
        picker.setScaleX(1.5f);
        picker.setScaleY(1.5f);

        return formItemView;
    }

    private void setFilters(Context context)
    {
        editText.setSingleLine(true);
        final int minValue = format.getMinValue();
        // allow any positive int if no max value is specified
        final int maxValue = format.getMaxValue() == 0 ? Integer.MAX_VALUE : format.getMaxValue();

        if(step.getPlaceholder() != null)
        {
            editText.setHint(step.getPlaceholder());
        }
        else if(maxValue == Integer.MAX_VALUE)
        {
            editText.setHint(context.getString(org.researchstack.backbone.R.string.rsb_hint_step_body_int_no_max));
        }
        else
        {
            editText.setHint(context.getString(org.researchstack.backbone.R.string.rsb_hint_step_body_int,
                    minValue,
                    maxValue));
        }

        editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

        if(result.getResult() != null)
        {
            editText.setText(String.valueOf(result.getResult()));
        }

        String minStr = Integer.toString(minValue);
        String maxStr = Integer.toString(maxValue);
        int maxLength = maxStr.length() >= minStr.length() ? maxStr.length() : minStr.length();
        InputFilter.LengthFilter maxLengthFilter = new InputFilter.LengthFilter(maxLength);
        InputFilter[] newFilters = ViewUtils.addFilter(editText.getFilters(), maxLengthFilter);
        editText.setFilters(newFilters);
    }

    @Override
    public StepResult getStepResult(boolean skipped)
    {
        if(skipped)
        {
            result.setResult(null);
        }
        else {
            /*
            String numString = editText.getText().toString();
            if(! TextUtils.isEmpty(numString))
            {
                result.setResult(Integer.valueOf(editText.getText().toString()));
            }*/

            result.setResult(picker.getValue());
        }

        return result;
    }

    @Override
    public BodyAnswer getBodyAnswerState()
    {
        /*if(editText == null)
        {
            return BodyAnswer.INVALID;
        }

        return format.validateAnswer(editText.getText().toString());
        */

        return format.validateAnswer(Integer.toString(picker.getValue()));
    }


}
