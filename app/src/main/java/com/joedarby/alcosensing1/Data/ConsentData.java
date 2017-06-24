package com.joedarby.alcosensing1.Data;

import android.os.Build;

import org.researchstack.backbone.result.StepResult;

import java.util.Map;

import static com.joedarby.alcosensing1.Data.MainActivity.AGE;
import static com.joedarby.alcosensing1.Data.MainActivity.EMAIL;
import static com.joedarby.alcosensing1.Data.MainActivity.GENDER;
import static com.joedarby.alcosensing1.Data.MainActivity.HEIGHT;
import static com.joedarby.alcosensing1.Data.MainActivity.MOBILITY;
import static com.joedarby.alcosensing1.Data.MainActivity.MOBILITY_DETAIL;
import static com.joedarby.alcosensing1.Data.MainActivity.WEIGHT;

public class ConsentData {


    public int age;
    public String email;
    public int height;
    public int weight;
    public String gender;
    public boolean mobility;
    public String mobilityDetail;
    public boolean inPrizeDraw;
    public String UID;
    public String device;
    public String deviceManufacturer;
    public String deviceModel;
    public String OSRelease;

    public ConsentData(Map<String,StepResult> result, String UID) {

        deviceManufacturer = Build.MANUFACTURER;
        deviceModel = Build.MODEL;
        device = Build.DEVICE;
        OSRelease = Build.VERSION.RELEASE;


        this.UID = UID;

        StepResult regFormResult = result.get("registration");

        age = (int) getResultValue(regFormResult, AGE);
        email = (String) getResultValue(regFormResult, EMAIL);
        height = (int) getResultValue(regFormResult, HEIGHT);
        weight = (int) getResultValue(regFormResult, WEIGHT);
        gender = (String) getResultValue(regFormResult, GENDER);
        mobility = (Boolean) getResultValue(regFormResult, MOBILITY);
        mobilityDetail = (String) getResultValue(regFormResult, MOBILITY_DETAIL);

        StepResult prizeFormResult = result.get("prizeStep");
        //prize step is multichoice so result is an array. If array empty, opt-out box was not checked
        int prizeArray = ((Object[])((StepResult) prizeFormResult.getResultForIdentifier("prize")).getResult()).length;
        inPrizeDraw = prizeArray == 0;
    }

    private Object getResultValue(StepResult stepRes, String ID) {
        return ((StepResult) stepRes.getResultForIdentifier(ID)).getResult();
    }


}
