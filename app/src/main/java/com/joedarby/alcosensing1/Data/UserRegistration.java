package com.joedarby.alcosensing1.Data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.joedarby.alcosensing1.Display.GenderStep;
import com.joedarby.alcosensing1.Display.HeightWeightStep;
import com.joedarby.alcosensing1.Display.InfoStep;
import com.joedarby.alcosensing1.Display.TickBoxStep;
import com.joedarby.alcosensing1.Display.MyTextAnswerFormat;
import com.joedarby.alcosensing1.R;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.answerformat.BooleanAnswerFormat;
import org.researchstack.backbone.answerformat.ChoiceAnswerFormat;
import org.researchstack.backbone.answerformat.IntegerAnswerFormat;
import org.researchstack.backbone.answerformat.TextAnswerFormat;
import org.researchstack.backbone.model.Choice;
import org.researchstack.backbone.step.FormStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.task.OrderedTask;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.ViewTaskActivity;

import static com.joedarby.alcosensing1.Data.MainActivity.AGE;
import static com.joedarby.alcosensing1.Data.MainActivity.CONSENT;
import static com.joedarby.alcosensing1.Data.MainActivity.EMAIL;
import static com.joedarby.alcosensing1.Data.MainActivity.GENDER;
import static com.joedarby.alcosensing1.Data.MainActivity.HEIGHT;
import static com.joedarby.alcosensing1.Data.MainActivity.MOBILITY;
import static com.joedarby.alcosensing1.Data.MainActivity.MOBILITY_DETAIL;
import static com.joedarby.alcosensing1.Data.MainActivity.SHOW_CONSENT;
import static com.joedarby.alcosensing1.Data.MainActivity.SHOW_INFO;
import static com.joedarby.alcosensing1.Data.MainActivity.SHOW_PRIZE;
import static com.joedarby.alcosensing1.Data.MainActivity.WEIGHT;

public class UserRegistration {

    private static InfoStep welcomeStep;
    private static InfoStep aboutStep;
    private static InfoStep howStep;
    private static InfoStep dataStep;

    private static final int REQUEST_CONSENT = 0;
    private static final int REQUEST_SHOW_INFO = 1;
    private static final int REQUEST_SHOW_CONSENT = 2;
    private static final int REQUEST_SHOW_PRIZE_INFO = 3;

    private static void buildInfoSteps(Context mainContext) {

        welcomeStep = new InfoStep("welcomeStep");
        welcomeStep.setStepTitle(R.string.welcome);
        welcomeStep.setConsentHTML(mainContext.getString(R.string.welcome_text));

        aboutStep = new InfoStep("aboutStep");
        aboutStep.setStepTitle(R.string.about);
        aboutStep.setConsentHTML(mainContext.getString(R.string.about_text));

        howStep = new InfoStep("howStep");
        howStep.setStepTitle(R.string.about);
        howStep.setConsentHTML(mainContext.getString(R.string.how_text));

        dataStep = new InfoStep("dataStep");
        dataStep.setStepTitle(R.string.about);
        dataStep.setConsentHTML(mainContext.getString(R.string.data_protection_text));

    }

    public static void reshowInfo(Context mainContext) {
        buildInfoSteps(mainContext);
        Task infoTask = new OrderedTask(SHOW_INFO,
                welcomeStep,
                aboutStep,
                howStep,
                dataStep);

        Intent intent = ViewTaskActivity.newIntent(mainContext, infoTask);
        ((Activity) mainContext).startActivityForResult(intent, REQUEST_SHOW_INFO);
    }

    public static void reShowPrizeDraw(Context mainContext) {
        Boolean inPrizeDraw = AppPrefs.getInstance(mainContext).getPrizeOpt();
        String optMessage = inPrizeDraw
                ? "You will be entered into the prize draw after 10 survey responses"
                : "You chose to opt out of the prize draw";

        TickBoxStep formStep = new TickBoxStep("prizeStep",
                "",
                mainContext.getString(R.string.prize_text),
                "",
                true);

        ChoiceAnswerFormat format = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
                new Choice<>(optMessage, 0));

        QuestionStep prize = new QuestionStep("prize", "If you DO NOT want to be included in the prize draw, please tick this box:", format);

        formStep.setFormSteps(prize);

        Task consentTask = new OrderedTask(SHOW_PRIZE,
                formStep);

        Intent intent = ViewTaskActivity.newIntent(mainContext, consentTask);
        ((Activity) mainContext).startActivityForResult(intent, REQUEST_SHOW_PRIZE_INFO);

    }

    public static void reshowConsent(Context mainContext) {
        TickBoxStep formStep = new TickBoxStep("consentStep",
                "",
                mainContext.getString(R.string.consent_text),
                mainContext.getString(R.string.consent_fail_text),
                true);

        formStep.setOptional(false);

        String consentDate = "Consented on " + AppPrefs.getInstance(mainContext).getConsentDate();

        ChoiceAnswerFormat format = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
                new Choice<>(consentDate, 0));

        QuestionStep consent = new QuestionStep("consent","By ticking the box below you agree to " +
                "the above statements and consent to take part in this study.", format);

        formStep.setFormSteps(consent);

        Task consentTask = new OrderedTask(SHOW_CONSENT,
                formStep);

        Intent intent = ViewTaskActivity.newIntent(mainContext, consentTask);
        ((Activity) mainContext).startActivityForResult(intent, REQUEST_SHOW_CONSENT);

    }

    public static void launchConsent(Context mainContext) {

        buildInfoSteps(mainContext);

        TickBoxStep screeningStep = screeningStep(mainContext);
        screeningStep.setStepTitle(R.string.reqs_title);
        screeningStep.setOptional(false);

        FormStep formStep = registrationForm();
        formStep.setStepTitle(R.string.reg_title);
        formStep.setOptional(false);

        TickBoxStep consentStep = consentStep(mainContext);
        consentStep.setStepTitle(R.string.consent_string);
        consentStep.setOptional(false);

        TickBoxStep prize = prizeStep(mainContext);
        prize.setStepTitle(R.string.prize_title);
        prize.setOptional(false);

        // Finally, create and present a task including these steps.
        Task consentTask = new OrderedTask(CONSENT,
                welcomeStep,
                aboutStep,
                howStep,
                dataStep,
                screeningStep,
                consentStep,
                formStep,
                prize);

        // Launch using the ViewTaskActivity and make sure to listen for the activity result
        Intent intent = ViewTaskActivity.newIntent(mainContext, consentTask);
        ((Activity) mainContext).startActivityForResult(intent, REQUEST_CONSENT);
    }

    private static FormStep registrationForm() {
        FormStep formStep = new FormStep("registration",
                "Nearly there...",
                "We just need to know a few things about you.");
        formStep.setStepTitle(R.string.rsb_consent);

        IntegerAnswerFormat ageFormat = new IntegerAnswerFormat(18,99);
        QuestionStep age = new QuestionStep(AGE, "Age", ageFormat);
        age.setPlaceholder("Answer");

        TextAnswerFormat emailFormat = new TextAnswerFormat();
        emailFormat.setIsMultipleLines(false);
        QuestionStep email = new QuestionStep(EMAIL, "E-mail Address", emailFormat);

        IntegerAnswerFormat heightFormat = new IntegerAnswerFormat(50,250);
        HeightWeightStep height = new HeightWeightStep(HEIGHT, "Height", heightFormat, true);
        height.setPlaceholder("Answer");

        IntegerAnswerFormat weightFormat = new IntegerAnswerFormat(15,200);
        HeightWeightStep weight = new HeightWeightStep(WEIGHT, "Weight", weightFormat, false);
        weight.setPlaceholder("Answer");

        TextAnswerFormat genderFormat = new MyTextAnswerFormat();
        genderFormat.setIsMultipleLines(false);
        GenderStep gender = new GenderStep(GENDER, "Gender (e.g. \"Female\")", genderFormat, "We ask because there may be sensor differences between genders");
        gender.setPlaceholder("Answer (optional)");
        gender.setOptional(true);

        BooleanAnswerFormat mobilityFormat = new BooleanAnswerFormat("Yes", "No");
        QuestionStep mobility = new QuestionStep(MOBILITY, "Do you consider yourself to have any mobility impairments?", mobilityFormat);

        TextAnswerFormat mobilityDetailFormat = new MyTextAnswerFormat();
        mobilityDetailFormat.setIsMultipleLines(true);
        QuestionStep mobilityDetail = new QuestionStep(MOBILITY_DETAIL, "If YES, please give further details.", mobilityDetailFormat);
        mobilityDetail.setPlaceholder("Answer (optional)");
        mobilityDetail.setOptional(true);

        formStep.setFormSteps(email, age, height, weight, gender, mobility, mobilityDetail);

        return formStep;
    }

    private static TickBoxStep screeningStep (Context mainContext) {
        TickBoxStep formStep = new TickBoxStep("screeningStep",
                "",
                mainContext.getString(R.string.requirements_text),
                mainContext.getString(R.string.screening_fail_text), false);

        ChoiceAnswerFormat format = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
                new Choice<>("Agree", 0));

        QuestionStep over18 = new QuestionStep("over18", "I am at least 18 years old", format);
        QuestionStep notPregnant = new QuestionStep("notPregnant", "I am not pregnant", format);
        QuestionStep avoid = new QuestionStep("avoid", "I have never been advised by a medical professional to avoid drinking alcohol", format);
        QuestionStep tooMuch = new QuestionStep("tooMuch", "I have never been advised by a medical professional that I drink too much alcohol", format);
        QuestionStep worried = new QuestionStep("worried", "I am not worried about my drinking" , format);

        formStep.setFormSteps(over18, notPregnant, avoid, tooMuch, worried);

        return formStep;
    }

    private static TickBoxStep consentStep (Context mainContext) {
        TickBoxStep formStep = new TickBoxStep("consentStep",
                "",
                mainContext.getString(R.string.consent_text),
                mainContext.getString(R.string.consent_fail_text),
                false);

        ChoiceAnswerFormat format = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
                new Choice<>("I consent", 0));

        QuestionStep consent = new QuestionStep("consent","By ticking the box below you agree to " +
                "the above statements and consent to take part in this study.", format);

        formStep.setFormSteps(consent);

        return formStep;
    }

    private static TickBoxStep prizeStep (Context mainContext) {
        TickBoxStep formStep = new TickBoxStep("prizeStep",
                "",
                mainContext.getString(R.string.prize_text),
                "",
                true);

        ChoiceAnswerFormat format = new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
                new Choice<>("Opt out", 0));

        QuestionStep prize = new QuestionStep("prize", "If you DO NOT want to be included in the prize draw, please tick this box:", format);

        formStep.setFormSteps(prize);

        return formStep;
    }
}
