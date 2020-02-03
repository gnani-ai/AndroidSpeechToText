package com.gnani.speechtotext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SpeechService.Listener, Recorder.RecordingStatusListener {

    private Button btnS;
    private TextView txtT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnS = findViewById(R.id.btnS);
        txtT = findViewById(R.id.txtT);

        Recorder.bind(MainActivity.this);

        btnS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Recorder.onRecord(getString(R.string.lang_value));
            }
        });
    }

    @Override
    public void onRecordingStatus(final boolean status) {

        Log.e("STATUS", " " + status);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (status) {
                    btnS.setText("STOP");
                } else {
                    btnS.setText("START");
                }
            }
        });
    }

    @Override
    public void onSpeechRecognized(final String text, String asr, boolean isFinal) {

        Log.e("text", " " + text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                txtT.setText(text);

            }
        });

    }

    @Override
    public void onError(Throwable t) {
        Log.e("on_ERROR", " " + t);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnS.setText("START");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Recorder.unbind(MainActivity.this);
    }
}
