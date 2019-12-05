package com.gnani.speechtotext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gnani.stt.Recorder;
import com.gnani.stt.SpeechService;

public class MainActivity extends AppCompatActivity implements SpeechService.Listener, Recorder.RecordingStatusListener {

    private Button btnS;
    private TextView txtT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Recorder.bind(MainActivity.this, MainActivity.this, MainActivity.this);

        btnS = findViewById(R.id.btnS);
        txtT = findViewById(R.id.txtT);

        btnS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Recorder.onRecord(BuildConfig.token, BuildConfig.language, BuildConfig.accesskey, BuildConfig.audioformat, BuildConfig.encoding, BuildConfig.sad, BuildConfig.ip, BuildConfig.port, BuildConfig.tls);

            }
        });
    }

    @Override
    public void onRecordingStatus(boolean status) {

        Log.e("STATUS", " " + status);

        if (status) {
            btnS.setText("STOP");
        } else {
            btnS.setText("START");
        }

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
    protected void onDestroy() {
        super.onDestroy();

        Recorder.unbind(MainActivity.this);
    }
}
