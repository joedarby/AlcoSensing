package com.joedarby.alcosensing1.Display;

import org.researchstack.backbone.step.ConsentDocumentStep;

public class InfoStep extends ConsentDocumentStep {

    private int stepTitle;

    public InfoStep(String identifier) {
        super(identifier);
    }

    @Override
    public Class getStepLayoutClass() {
        return InfoStepLayout.class;
    }

    @Override
    public void setStepTitle(int stepTitle) {
        this.stepTitle = stepTitle;
    }

    @Override
    public int getStepTitle() {
        return stepTitle;
    }
}
