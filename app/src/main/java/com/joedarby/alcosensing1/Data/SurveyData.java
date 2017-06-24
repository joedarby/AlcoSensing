package com.joedarby.alcosensing1.Data;

import android.content.Context;

import org.researchstack.backbone.result.StepResult;

public class SurveyData {

    // transient to exclude from gson
    private transient StepResult mainResult;

    public Boolean didDrink;
    public int beerPintCount;
    public int beerBottleCount;
    public int wineStandardCount;
    public int wineLargeCount;
    public int spiritSingleCount;
    public int spiritDoubleCount;

    public int feeling;
    public int responseCount;
    public float units;


    public SurveyData(StepResult result, Context context) {

        mainResult = result;

        didDrink = true;
        beerPintCount = (int) getResultValue(PostDataSurveyActivity.BEER_PINT);
        beerBottleCount = (int) getResultValue(PostDataSurveyActivity.BEER_BOTTLE);
        wineStandardCount = (int) getResultValue(PostDataSurveyActivity.WINE_STANDARD);
        wineLargeCount = (int) getResultValue(PostDataSurveyActivity.WINE_LARGE);
        spiritSingleCount = (int) getResultValue(PostDataSurveyActivity.SPIRIT_SINGLE);
        spiritDoubleCount = (int) getResultValue(PostDataSurveyActivity.SPIRIT_DOUBLE);
        feeling = (int) getResultValue(PostDataSurveyActivity.FEELING);

        AppPrefs prefs = AppPrefs.getInstance(context);
        responseCount = prefs.getResponseCount();
        units = getUnits();
    }

    public SurveyData(Context context) {

        mainResult = null;

        didDrink = false;
        beerPintCount = 0;
        beerBottleCount = 0;
        wineStandardCount = 0;
        wineLargeCount = 0;
        spiritSingleCount = 0;
        spiritDoubleCount = 0;
        feeling = 0;

        AppPrefs prefs = AppPrefs.getInstance(context);
        responseCount = prefs.getResponseCount();
    }



    private Object getResultValue(String ID) {
        return ((StepResult) mainResult.getResultForIdentifier(ID)).getResult();
    }

    private float getUnits() {
        return (beerPintCount * 2.3f) + (beerBottleCount * 1.6f) + (wineStandardCount * 2.3f) + (wineLargeCount * 3.3f) + spiritSingleCount + (spiritDoubleCount * 2.0f);
    }


}
