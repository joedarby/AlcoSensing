package com.joedarby.alcosensing1.Display;

import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.OrderedTask;
import org.researchstack.backbone.ui.ViewTaskActivity;

import java.io.Serializable;


public class MyOrderedTask extends OrderedTask implements Serializable {

    public MyOrderedTask(String identifier, Step... steps) {
        super(identifier, steps);

    }

    @Override
    public void onViewChange(ViewChangeType type, ViewTaskActivity activity, Step currentStep) {

        if (type == ViewChangeType.ActivityPause) {
            activity.finish();

        }
    }
}
