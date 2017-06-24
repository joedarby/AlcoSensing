package com.joedarby.alcosensing1.Display;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.step.QuestionStep;

public class GenderStep extends QuestionStep {

    private String subtitle;

    public GenderStep(String identifier, String title, AnswerFormat format, String subtitle) {
        super(identifier, title, format);
        this.subtitle = subtitle;
    }

    @Override
    public Class<?> getStepBodyClass() {
        return GenderQuestionBody.class;
    }

    public String getSubtitle() {return subtitle;}

}
