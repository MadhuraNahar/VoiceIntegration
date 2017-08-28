package com.test.voice.services;

/**
 * Created by Madhura Nahar.
 */

import android.content.Context;
import android.content.Intent;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class contains apis for intermediate between speech recognition and TTS.
 */
public class ButlerSpeechIntermediate implements RecognitionListener {

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    Logutil logger = Logutil.getInstance();

    private int nCounter = 0;
    final Handler handler = new Handler(Looper.getMainLooper());

    private Timer mTimer;
    private TimerTask mTimerTask;
    private Context mContext;
    private Handler mHandler;
    private Boolean mShutdownRecognition;

    public ButlerSpeechIntermediate(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        mShutdownRecognition = false;
    }

    public void startRecognition() {
        mShutdownRecognition = false;
        startTimer();
        startSpeechRecognition();
    }

    /**
     * method to start listening for speech recognition.
     */
    private void startSpeechRecognition() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                mContext.getPackageName());

        mSpeechRecognizer.setRecognitionListener(this);
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    public void startTimer() {

        //set a new Timer
        mTimer = new Timer();
        initializeTimerTask();
        mTimer.schedule(mTimerTask, 15000);
    }

    public void stoptimertask() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    public void initializeTimerTask() {

        mTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        sendSpeechBroadcast("");
                    }
                });
            }
        };

    }

    protected void onDestroy() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
    }


    /**
     * Send broadcast intent for the recognised speech data.
     *
     * @param speechData recognised speech text
     */
    private void sendSpeechBroadcast(String speechData) {

        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
        stoptimertask();

//        if(!speechData.isEmpty())
        {
            String message = "";

            if (speechData.isEmpty())
                message = "Error -";

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.BROADCAST_USER_SPEECH);
            broadcastIntent.putExtra("UserSpeechData", speechData);
            mContext.sendBroadcast(broadcastIntent);

            message += " " + speechData;
            // Send the message received to ButlerService to parse.
            Message msg = mHandler.obtainMessage(Constants.BUDDY_SPEECH_ACTIVITY_DATA);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.USER_SPEECH_DATA, message);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }


    private void restartEngine() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
        startSpeechRecognition();
    }

    public void shutdown() {
        mShutdownRecognition = true;

        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
    }



        @Override
        public void onBeginningOfSpeech() {
            //Log.d(TAG, "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            //Log.d(TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error) {
            if (mShutdownRecognition == true)
                return;

            nCounter++;

            if (nCounter >= 45) {

                sendSpeechBroadcast("");
            } else {
                restartEngine();
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            logger.info("SpeechActivity : onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results) {
            //Log.d(TAG, "onResults"); //$NON-NLS-1$
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (matches != null && matches.size() > 0) {
                sendSpeechBroadcast(matches.get(0));
            }

            if (mShutdownRecognition == true) {
                return;
            }

        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

}
