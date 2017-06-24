package com.joedarby.alcosensing1.Display;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.step.QuestionStep;


public class HeightWeightStep extends QuestionStep {

    private Boolean isHeight;

    public HeightWeightStep(String identifier, String title, AnswerFormat format, Boolean isHeight) {
        super(identifier, title, format);
        this.isHeight = isHeight;
    }

    @Override
    public Class<?> getStepBodyClass() {
        return HeightWeightQuestionBody.class;
    }

    public Boolean getIsHeight() {return isHeight;}
}
