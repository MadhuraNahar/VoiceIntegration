package com.test.voice.services;

/**
 * Created by Madhura Nahar.
 */

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.test.voice.MainActivity;
import com.test.voice.utils.Constants;
import com.test.voice.utils.Logutil;

import java.util.ArrayList;

/**
 * This class specifies methods for user speech recognition
 */
public class ButlerSpeechRecognizer implements RecognitionListener {
    private static ButlerSpeechRecognizer ourInstance = new ButlerSpeechRecognizer();
    public static SpeechRecognizer speech = null;

    private Intent mRecognizerIntent;
    protected static AudioManager mAudioManager;

    private static Boolean sIsMute;
    private static Boolean sIsListening;
    private static Boolean sHardStopListening;

    Logutil logger = Logutil.getInstance();
    static Context mContext;
    static Handler mHandler;

    public static ButlerSpeechRecognizer getInstance(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        return ourInstance;
    }

    private ButlerSpeechRecognizer() {
        mContext = null;
        sIsMute = false;
        sIsListening = false;
        sHardStopListening = false;
    }

    /**
     * Check if the process is running on main thread.
     */
    private static void checkIsCalledFromMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
//            Logutil.getInstance(ButlerSpeechRecognizer.class).error("SpeechRecognizer should be used only from the application's main thread");
            Logutil.getInstance().error("SpeechRecognizer should be used only from the application's main thread");
            throw new RuntimeException("SpeechRecognizer should be used only from the application's main thread");
        }
    }

    /**
     * Destroy speech recognition object
     */
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (speech != null) {
            UnMute();
            shutdownSpeech();
//            logger.info("In OnDestroy of SpeechRecognition");
        }
    }

    /**
     * Initialise and setup speech recognizer.
     */
    public void setupspeech() {
        checkIsCalledFromMainThread();


        if (mContext != null) {


            speech = SpeechRecognizer.createSpeechRecognizer(mContext);

            speech.setRecognitionListener(this);
            mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//		    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
//            mRecognizerIntent.putExtra(RecognizerIntent.ACTION_RECOGNIZE_SPEECH,RecognizerIntent.EXTRA_PREFER_OFFLINE);
            mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    mContext.getPackageName());
            mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
//            mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");



            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//            sIsMute = false;
            sIsListening = false;
            Mute();
            startspeechrec();

        }
    }

    /**
     * Start listening/recording user speech
     */
    private void startspeechrec() {
        if (!sIsListening) {
            speech.startListening(mRecognizerIntent);
            sHardStopListening = true;
            sIsListening = true;
        }
    }

    /**
     * Stops listening user speech
     */
    private void stopSpeechListening() {
        speech.cancel();
        speech.stopListening();
        UnMute();
        sHardStopListening = false;
        sIsListening = false;
    }

    /**
     * Restart speech recognizer.
     */
    private void restartSpeechRecognizer() {

        if(sHardStopListening == false)
            return;

        sIsListening = false;
        speech.destroy();
        speech = null;
        setupspeech();
    }

    /**
     * Close speech recognizer and destroy its object if not null.
     */
    public void shutdownSpeech() {
        checkIsCalledFromMainThread();

        if (speech != null) {
            UnMute();
            sIsListening = false;
            speech.destroy();
            speech = null;
            sHardStopListening = false;
        }
    }

    @Override
    public void onBeginningOfSpeech() {

        sIsListening = true;
    }

    @Override
    public void onBufferReceived(byte[] buffer) {


    }

    @Override
    public void onEndOfSpeech() {

        sIsListening = false;
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);

//        if (error != SpeechRecognizer.ERROR_NO_MATCH)
//            logger.error("Speech Error - " + error + errorMessage);

//        if(!sIsListening)
        restartSpeechRecognizer();

    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (matches != null && !matches.isEmpty())
            logger.info("User speech - " + matches.get(0));

        if (!sIsListening)
            restartSpeechRecognizer();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.BROADCAST_USER_SPEECH);
        broadcastIntent.putExtra("UserSpeechData", matches.get(0));
        mContext.sendBroadcast(broadcastIntent);
//        processSpeechRecognition
        // Send the message received to ButlerService to parse.
        Message msg = mHandler.obtainMessage(Constants.BUDDY_SPEECH_RECEIVE);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.USER_SPEECH_DATA, matches.get(0));
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    /**
     * Mute all the audio profiles.
     */
    public void Mute() {

        if (sIsMute == false) {


//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
//            }
//            else
            {
                mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
                mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                mAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
                mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }

//            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//            mAudioManager.startBluetoothSco();
//            mAudioManager.setBluetoothScoOn(true);

            sIsMute = true;
        } else {

        }
    }

    /**
     * Unmute all the audio profiles
     */
    public void UnMute() {
        if (sIsMute == true) {


//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
//
////                int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
////                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
////                float percent = 0.99f;
////                int seventyVolume = (int) (maxVolume*percent);
////                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
//            }
//            else
            {
                mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
                mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                mAudioManager.setStreamMute(AudioManager.STREAM_RING, false);
                mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 1,0);

//                int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//                float percent = 0.99f;
//                int seventyVolume = (int) (maxVolume*percent);
//                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
            }

//            mAudioManager.setMode(AudioManager.MODE_NORMAL);
//            mAudioManager.stopBluetoothSco();
//            mAudioManager.setBluetoothScoOn(false);

            sIsMute = false;
        } else {

        }

    }
}
