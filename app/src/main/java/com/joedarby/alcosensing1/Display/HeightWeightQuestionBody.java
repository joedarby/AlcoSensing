package com.joedarby.alcosensing1.Display;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.joedarby.alcosensing1.R;

import org.researchstack.backbone.answerformat.IntegerAnswerFormat;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.step.body.BodyAnswer;
import org.researchstack.backbone.ui.step.body.StepBody;

import static android.view.View.GONE;


public class HeightWeightQuestionBody implements StepBody {

    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    // Constructor Fields
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    private HeightWeightStep step;
    private StepResult<Integer> result;
    private IntegerAnswerFormat format;

    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    // View Fields
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    private int      viewType;
    private EditText editText;
    private EditText editText2;
    private Boolean isHeight;
    private Switch metricSwitch;

    public HeightWeightQuestionBody(Step step, StepResult result)
    {
        this.step = (HeightWeightStep) step;
        this.result = result == null ? new StepResult<>(step) : result;
        this.format = (IntegerAnswerFormat) this.step.getAnswerFormat();

    }

    @Override
    public View getBodyView(int viewType, LayoutInflater inflater, ViewGroup parent)
    {
        this.viewType = viewType;
        this.isHeight = step.getIsHeight();

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
            return initViewCompact(inflater, parent);
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


    private View initViewCompact(LayoutInflater inflater, ViewGroup parent)
    {
        final View formItemView = inflater.inflate(R.layout.height_weight_input, parent, false);
        TextView title = (TextView) formItemView.findViewById(R.id.label);
        title.setText(step.getTitle());

        final TextView label1 = (TextView) formItemView.findViewById(R.id.unit_label_1);
        label1.setVisibility(View.VISIBLE);
        final TextView label2 = (TextView) formItemView.findViewById(R.id.unit_label_2);
        label2.setVisibility(View.GONE);
        editText = (EditText) formItemView.findViewById(R.id.value1);
        editText.setVisibility(View.VISIBLE);
        editText.setText("0");
        editText2 = (EditText) formItemView.findViewById(R.id.value2);
        editText2.setVisibility(View.GONE);
        editText2.setText("0");

        TextView switchLabelLeft = (TextView) formItemView.findViewById(R.id.switch_label_left);
        TextView switchLabelRight = (TextView) formItemView.findViewById(R.id.switch_label_right);

        metricSwitch = (Switch) formItemView.findViewById(R.id.h_w_switch);

        if (isHeight) {
            switchLabelLeft.setText("cm");
            switchLabelRight.setText("ft/inches");
            label1.setText("cm");

        } else {
            switchLabelLeft.setText("kg");
            switchLabelRight.setText("st/lbs");
            label1.setText("kg");

        }

        metricSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isHeight) {
                    if (!isChecked) {
                        editText2.setVisibility(GONE);
                        editText2.setText("0");
                        label2.setVisibility(GONE);
                        label1.setText("cm");
                    } else {
                        editText2.setVisibility(View.VISIBLE);
                        label2.setVisibility(View.VISIBLE);
                        label1.setText("ft");
                        label2.setText("inches");
                    }
                } else {
                    if (!isChecked) {
                        editText2.setVisibility(GONE);
                        editText2.setText("0");
                        label2.setVisibility(GONE);
                        label1.setText("kg");
                    } else {
                        editText2.setVisibility(View.VISIBLE);
                        label2.setVisibility(View.VISIBLE);
                        label1.setText("st");
                        label2.setText("lbs");
                    }
                }
            }
        });

        return formItemView;
    }

    @Override
    public StepResult getStepResult(boolean skipped)
    {
        if(skipped || TextUtils.isEmpty(editText.getText().toString()))
        {
            result.setResult(null);
        }
        else {
            float value;
            if(isHeight) {
                if (metricSwitch.isChecked()) {
                    value = (Integer.parseInt(editText.getText().toString()) * 12) + Integer.parseInt(editText2.getText().toString());
                    value = value * 2.54f;
                } else {
                    value = Integer.parseInt(editText.getText().toString());
                }
            } else {
                if (metricSwitch.isChecked()) {
                    value = (Integer.parseInt(editText.getText().toString()) * 14) + Integer.parseInt(editText2.getText().toString());
                    value = value / 2.20462f;
                } else {
                    value = Integer.parseInt(editText.getText().toString());
                }
            }

            result.setResult((int) value);
        }

        return result;
    }

    @Override
    public BodyAnswer getBodyAnswerState()
    {
        if (TextUtils.isEmpty(editText2.getText().toString())) {
            editText2.setText("0");
        }

        float value;
        if (TextUtils.isEmpty(editText.getText().toString())) {
            return format.validateAnswer("");
        }
        else if(isHeight) {
            if (metricSwitch.isChecked()) {
                value = (Integer.valueOf(editText.getText().toString()) * 12) + Integer.valueOf(editText2.getText().toString());
                value = value * 2.54f;
            } else {
                value = Integer.parseInt(editText.getText().toString());
            }
        } else {
            if (metricSwitch.isChecked()) {
                value = (Integer.valueOf(editText.getText().toString()) * 14) + Integer.valueOf(editText2.getText().toString());
                value = value / 2.20462f;
            } else {
                value = Integer.parseInt(editText.getText().toString());
            }
        }


        return validateHeightWeight(Integer.toString((int) value));
    }

    private BodyAnswer validateHeightWeight(String inputString) {
        // If no answer is recorded
        if(org.researchstack.backbone.utils.TextUtils.isEmpty(inputString)) {
            return BodyAnswer.INVALID;
        } else {
            // Parse value from editText
            Integer intAnswer = Integer.valueOf(inputString);
            if(isHeight) {
                if(intAnswer < format.getMinValue())
                {
                    return new BodyAnswer(false,
                            R.string.invalid_height_under,
                            String.valueOf(format.getMinValue()));
                }

                else if(intAnswer > format.getMaxValue())
                {
                    return new BodyAnswer(false,
                            R.string.invalid_height_over,
                            String.valueOf(format.getMaxValue()));
                }
            } else {
                if(intAnswer < format.getMinValue())
                {
                    return new BodyAnswer(false,
                            R.string.invalid_weight_under,
                            String.valueOf(format.getMinValue()));
                }

                else if(intAnswer > format.getMaxValue())
                {
                    return new BodyAnswer(false,
                            R.string.invalid_weight_over,
                            String.valueOf(format.getMaxValue()));
                }
            }


        }

        return BodyAnswer.VALID;
    }


}
