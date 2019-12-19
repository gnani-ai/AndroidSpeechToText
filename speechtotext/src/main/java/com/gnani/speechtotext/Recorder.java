package com.gnani.speechtotext;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.IBinder;

import static android.content.Context.BIND_AUTO_CREATE;

public class Recorder {

    private static SpeechService mSpeechService;
    private static boolean mStartRecording = false;
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static AudioRecord recorder = null;
    private static int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    private static Thread recordingThread = null;
    private static SpeechService.Listener listener;
    private static RecordingStatusListener listener1;
    private static CountDownTimer countDownTimer;
    private static String TOKEN = null;
    private static String ACCESS_KEY = null;

    static void bind(Context context) {

        listener = (SpeechService.Listener) context;
        listener1 = (RecordingStatusListener) context;
        context.bindService(new Intent(context, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

    }

    static void unbind(Context context) {
        mSpeechService.removeListener(mSpeechServiceListener);
        context.unbindService(mServiceConnection);
        mSpeechService = null;

        stopCounter();
    }

    static void onRecord(String lang) {

        if (!mStartRecording) {
            mSpeechService.startRecognizing(TOKEN, lang, ACCESS_KEY, "wav", "pcm16", "yes", "asr.gnani.ai", 443, true);
            startRecording();

        } else {
            stopCounter();
        }
    }

    private static void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize);
        int i = recorder.getState();
        if (i == 1)
            recorder.startRecording();

        if (listener1 != null) {
            listener1.onRecordingStatus(true);
        }

        startCounter();

        mStartRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[bufferSize];
                int read = 0;
                while (mStartRecording) {

                    read = recorder.read(data, 0, bufferSize);
                    if (read > 0) {
                    }

                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {

                        try {
                            mSpeechService.recognize(data, bufferSize);

                        } catch (Exception e) {
                        }

                    }
                }
            }
        }, "AudioRecorder Thread");

        recordingThread.start();
    }


    private static void stopRecording() {
        if (recorder != null) {

            int i = recorder.getState();

            if (i == 1)
                recorder.stop();
            recorder.release();

            mStartRecording = false;

            if (listener1 != null) {
                listener1.onRecordingStatus(false);
            }
            recorder = null;
            recordingThread = null;
        }

    }

    private static final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };

    private static final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final String asr, final boolean isFinal) {

                    if (asr != null) {
                        if (asr.equalsIgnoreCase("gnani")) {
                            listener.onSpeechRecognized(text, asr, isFinal);

                        } else if (asr.equalsIgnoreCase("yes")) {

                            stopCounter();
                            listener.onSpeechRecognized(text, asr, isFinal);

                        }
                    }
                }

                @Override
                public void onError(Throwable t) {

                    stopCounter();
                    listener.onError(t);

                }

            };


    public interface RecordingStatusListener {

        /**
         * Called when a recording starts and stops.
         * <p>
         * //         * @param boolean status    true if recording is started and false if recording is stopped.
         */
        void onRecordingStatus(boolean status);
    }

    private static void startCounter() {

        countDownTimer = new CountDownTimer(16000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                stopCounter();
            }
        }.start();
    }

    private static void stopCounter() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (mSpeechService != null) {
            mSpeechService.finishRecognizing();
        }
        stopRecording();
    }

    static void init(String token, String accessKey) {

        TOKEN = token;
        ACCESS_KEY = accessKey;

    }

}