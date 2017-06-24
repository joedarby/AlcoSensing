package com.joedarby.alcosensing1.Display;

import org.researchstack.backbone.answerformat.TextAnswerFormat;


public class MyTextAnswerFormat extends TextAnswerFormat {

    @Override
    public boolean isAnswerValid(String text) {
        return true;
    }
}
