package com.joedarby.alcosensing1.Data;


import android.content.Intent;

import com.joedarby.alcosensing1.Display.DrinkStep;
import com.joedarby.alcosensing1.Display.MyOrderedTask;
import com.joedarby.alcosensing1.R;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.answerformat.ChoiceAnswerFormat;
import org.researchstack.backbone.answerformat.IntegerAnswerFormat;
import org.researchstack.backbone.model.Choice;
import org.researchstack.backbone.step.FormStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.ViewTaskActivity;


public class Survey {

    private final String timePeriodString;

    Survey(String timePeriod){

        timePeriodString = timePeriod;
    }

    public void launchSurvey(PostDataSurveyActivity surveyActivity) {

        FormStep formStep = surveyForm();
        Task surveyTask = new MyOrderedTask(PostDataSurveyActivity.SURVEY, formStep);
        Intent intent = ViewTaskActivity.newIntent(surveyActivity, surveyTask);
        surveyActivity.startActivityForResult(intent, PostDataSurveyActivity.REQUEST_SURVEY);

    }

    private FormStep surveyForm() {
        FormStep formStep = new FormStep(PostDataSurveyActivity.SURVEY_STEP,
                timePeriodString,
                "");
        formStep.setStepTitle(R.string.survey_title);
        formStep.setOptional(false);

        //BooleanAnswerFormat didDrinkFormat = new BooleanAnswerFormat("Yes", "No");
        //QuestionStep didDrink = new QuestionStep(PostDataSurveyActivity.DID_DRINK, "Did you drink any alcohol during this time period?", didDrinkFormat);

        IntegerAnswerFormat unitsCountFormat = new IntegerAnswerFormat(0,99);
        DrinkStep beerPintCount = new DrinkStep(PostDataSurveyActivity.BEER_PINT, "Roughly how much did you have to drink?\n\nBeer, cider, etc:", "Pint (568ml)", unitsCountFormat, true);
        DrinkStep beerBottleCount = new DrinkStep(PostDataSurveyActivity.BEER_BOTTLE, "Beer, cider, etc:", "Bottle (330ml)", unitsCountFormat, false);
        DrinkStep wineStandardCount = new DrinkStep(PostDataSurveyActivity.WINE_STANDARD, "Wine:", "Standard glass (175ml)", unitsCountFormat, true);
        DrinkStep wineLargeCount = new DrinkStep(PostDataSurveyActivity.WINE_LARGE, "Wine:", "Large glass (250ml)", unitsCountFormat, false);
        DrinkStep spiritSingleCount = new DrinkStep(PostDataSurveyActivity.SPIRIT_SINGLE, "Spirits (e.g. Vodka, Gin, Rum, Shots):", "Single (25ml)", unitsCountFormat, true);
        DrinkStep spiritDoubleCount = new DrinkStep(PostDataSurveyActivity.SPIRIT_DOUBLE, "Spirits (e.g. Vodka, Gin, Rum, Shots):", "Double (50ml)", unitsCountFormat, false);

        QuestionStep feeling = new QuestionStep(PostDataSurveyActivity.FEELING);
        feeling.setStepTitle(R.string.survey);
        AnswerFormat feelingFormat = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.SingleChoice,
                new Choice<>("1. Totally Sober", 0),
                new Choice<>("2.", 1),
                new Choice<>("3. Quite Tipsy", 2),
                new Choice<>("4.", 3),
                new Choice<>("5. Very Drunk", 4),
                new Choice<>("Can't remember", 5));
        feeling.setTitle("Please rate how you felt.");
        feeling.setAnswerFormat(feelingFormat);


        formStep.setFormSteps(beerPintCount, beerBottleCount, wineStandardCount, wineLargeCount, spiritSingleCount, spiritDoubleCount, feeling);

        return formStep;
    }
}
