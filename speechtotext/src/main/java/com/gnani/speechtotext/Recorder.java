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

    public static SpeechService mSpeechService;
    public static boolean mStartRecording = false;
    public static final int RECORDER_SAMPLERATE = 16000;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    public static AudioRecord recorder = null;
    public static int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    //public static boolean isRecording = false;
    public static Thread recordingThread = null;
    public static SpeechService.Listener listener;
    public static Recorder.RecordingStatusListener listener1;
    public static CountDownTimer countDownTimer;

    public static void bind(Context context, SpeechService.Listener speechServiceListener, Recorder.RecordingStatusListener recordingStatusListener) {

        listener = speechServiceListener;
        listener1 = recordingStatusListener;
        context.bindService(new Intent(context, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

    }

    public static void unbind(Context context) {
        mSpeechService.removeListener(mSpeechServiceListener);
        context.unbindService(mServiceConnection);
        mSpeechService = null;

        stopCounter();
    }

    public static void onRecord(String token, String lang, String akey, String audioFormat, String encoding, String sad, String ip, int port, boolean tls) {

        if (!mStartRecording) {
            mSpeechService.startRecognizing(token, lang, akey, audioFormat, encoding, sad, ip, port, tls);
            startRecording();

        } else {
            stopCounter();
        }
    }

    public static void startRecording() {
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
                byte data[] = new byte[bufferSize];
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


    public static void stopRecording() {
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

    public static final ServiceConnection mServiceConnection = new ServiceConnection() {

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

    public static final SpeechService.Listener mSpeechServiceListener =
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
                    } else if (text == null && !isFinal) {
                        stopCounter();
                        listener.onSpeechRecognized(text, asr, isFinal);
                    }
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

    public static void startCounter() {

        countDownTimer = new CountDownTimer(16000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                stopCounter();
            }
        }.start();
    }

    public static void stopCounter() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (mSpeechService != null) {
            mSpeechService.finishRecognizing();
        }
        stopRecording();
    }

}