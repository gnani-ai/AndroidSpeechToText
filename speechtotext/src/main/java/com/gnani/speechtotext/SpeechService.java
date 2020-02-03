package com.gnani.speechtotext;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gnani.speech.ListenerGrpc;
import com.gnani.speech.SpeechChunk;
import com.gnani.speech.TranscriptChunk;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;


public class SpeechService extends Service {

    public interface Listener {

        /**
         * Called when a new piece of text was recognized by the Speech API.
         *
         * @param text    The text.
         * @param isFinal {@code true} when the API finished processing audio.
         */
        void onSpeechRecognized(String text, String asr, boolean isFinal);
        void onError(Throwable t);
    }

    private static final String TAG = "SpeechService";
    private final SpeechBinder mBinder = new SpeechBinder();
    private final ArrayList<Listener> mListeners = new ArrayList<>();
    private ListenerGrpc.ListenerStub mApiG;
    private ManagedChannel channelG;

    private final StreamObserver<TranscriptChunk> mResponseObserverG
            = new StreamObserver<TranscriptChunk>() {
        @Override
        public void onNext(TranscriptChunk response) {

            String transcript = response.getTranscript();
            String asr = response.getAsr();

            if (transcript != null) {
                for (Listener listener : mListeners) {

                    listener.onSpeechRecognized(transcript, asr, true);

                }
            }
        }

        @Override
        public void onError(Throwable t) {

            for (Listener listener : mListeners) {
                listener.onError(t);
            }

        }

        @Override
        public void onCompleted() {
        }

    };


    private StreamObserver<SpeechChunk> mRequestObserverG;

    public static SpeechService from(IBinder binder) {
        return ((SpeechBinder) binder).getService();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void fetchAccessToken(String tokenValue, String langValue, String akeyValue, String audioFormatValue, String encodingValue, String sadValue, String ipValue, int portValue, boolean tls) {
        try {
            Metadata header = new Metadata();
            Metadata.Key<String> token = Metadata.Key.of(getString(R.string.token_key), Metadata.ASCII_STRING_MARSHALLER);
            header.put(token, tokenValue);
            Metadata.Key<String> lang = Metadata.Key.of(getString(R.string.lang_key), Metadata.ASCII_STRING_MARSHALLER);
            header.put(lang, langValue);
            Metadata.Key<String> akey = Metadata.Key.of(getString(R.string.access_key_key), Metadata.ASCII_STRING_MARSHALLER);
            header.put(akey, akeyValue);
            Metadata.Key<String> audioformat = Metadata.Key.of(getString(R.string.audio_format_key), Metadata.ASCII_STRING_MARSHALLER);
            header.put(audioformat, audioFormatValue);
            Metadata.Key<String> encoding = Metadata.Key.of(getString(R.string.encoding_key), Metadata.ASCII_STRING_MARSHALLER);
            header.put(encoding, encodingValue);
            Metadata.Key<String> sad = Metadata.Key.of(getString(R.string.silence_key), Metadata.ASCII_STRING_MARSHALLER);
            header.put(sad, sadValue);

            if (tls) {
                channelG = ManagedChannelBuilder.forAddress(ipValue, portValue).build();
            } else {
                channelG = ManagedChannelBuilder.forAddress(ipValue, portValue).usePlaintext(true).build();
            }
            mApiG = ListenerGrpc.newStub(channelG);
            mApiG = MetadataUtils.attachHeaders(mApiG, header);
        } catch (Exception e) {

            for (Listener listener : mListeners) {
                listener.onError(e);
            }

//            for (Listener listener : mListeners) {
//                listener.onSpeechRecognized(null, null, false);
//            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Release the gRPC channel.
        if (mApiG != null) {
            if (channelG != null && !channelG.isShutdown()) {
                try {
                    channelG.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                }
            }
            mApiG = null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void addListener(@NonNull Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        mListeners.remove(listener);
    }

    /**
     * Starts recognizing speech audio.
     */
    public void startRecognizing(String token, String lang, String akey, String audioFormat, String encoding, String sad, String ip, int port, boolean tls) {

        fetchAccessToken(token, lang, akey, audioFormat, encoding, sad, ip, port, tls);
        if (mApiG != null) {

            mRequestObserverG = mApiG.doSpeechToText(mResponseObserverG);
        } else {
        }
    }

    /**
     * Recognizes the speech audio. This method should be called every time a chunk of byte buffer
     * is ready.
     *
     * @param data The audio data.
     * @param size The number of elements that are actually relevant in the {@code data}.
     */
    public void recognize(byte[] data, int size) {
        if (mRequestObserverG == null) {
            return;
        }
        try {

            mRequestObserverG.onNext(SpeechChunk.newBuilder()
                    .setContent(ByteString.copyFrom(data, 0, size))
                    .build());

        } catch (Exception e) {
        }
    }

    /**
     * Finishes recognizing speech audio.
     */
    public void finishRecognizing() {
        if (mRequestObserverG == null) {
            return;
        }

        mRequestObserverG.onCompleted();
        mRequestObserverG = null;
    }


    private class SpeechBinder extends Binder {

        SpeechService getService() {
            return SpeechService.this;
        }

    }
}
