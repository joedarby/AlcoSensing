package com.joedarby.alcosensing1.Display;

import org.researchstack.backbone.step.FormStep;
import org.researchstack.backbone.step.QuestionStep;

import java.util.Arrays;
import java.util.List;

public class TickBoxStep extends FormStep {

    private List<QuestionStep> formSteps;
    private String declineText;
    private boolean canBeUnticked;

    public TickBoxStep(String identifier, String title, String text, String declineText, Boolean canBeUnticked)
    {
        super(identifier, title, text);
        this.declineText = declineText;
        this.canBeUnticked = canBeUnticked;
    }

    @Override
    public Class getStepLayoutClass() {
        return TickBoxStepLayout.class;
    }

    /**
     * Returns the list of items in the form.
     *
     * @return a list of QuestionSteps in the form
     */
    public List<QuestionStep> getFormSteps()
    {
        return formSteps;
    }

    public void setFormSteps(List<QuestionStep> formSteps)
    {
        this.formSteps = formSteps;
    }

    public void setFormSteps(QuestionStep... formSteps)
    {
        setFormSteps(Arrays.asList(formSteps));
    }

    public String getDeclineText() {return declineText;}

    public boolean isCanBeUnticked() {
        return canBeUnticked;
    }
}
