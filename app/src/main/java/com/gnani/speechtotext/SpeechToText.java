package com.gnani.speechtotext;

import android.app.Application;

public class SpeechToText extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Recorder.init("yourToken", "yourAccessKey");
    }
}
