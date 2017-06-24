package com.joedarby.alcosensing1.Display;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.step.QuestionStep;

public class DrinkStep extends QuestionStep {

    private Boolean showTitle;
    private String subTitle;

    public DrinkStep(String identifier, String title,String subTitle, AnswerFormat format, Boolean showTitle) {
        super(identifier, title, format);
        this.showTitle = showTitle;
        this.subTitle = subTitle;
    }

    @Override
    public Class<?> getStepBodyClass() {
        return DrinkQuestionBody.class;
    }

    public Boolean displayWithTitle() {
        return showTitle;
    }

    public String getSubTitle() {
        return subTitle;
    }

}
