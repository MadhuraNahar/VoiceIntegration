package com.test.voice.message_engine;

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
 * This class contains apis for message intermediate between speech recognition and TTS.
 */
public class BuddyMessageIntermediate implements RecognitionListener {

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    Logutil logger = Logutil.getInstance();

    private int nCounter = 0;
    final Handler handler = new Handler(Looper.getMainLooper());

    private Context mContext;
    private Handler mHandler;
    private Boolean mShutdownRecognition;
    private String mMessageContent;

    private Timer msgResultTimer;
    private TimerTask msgResultTimerTask;
    private boolean isEndOfMessage = false;

    private Timer msgPartialTimer;
    private TimerTask msgPartialTimerTask;


    public BuddyMessageIntermediate(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        mShutdownRecognition = false;
        mMessageContent = "";
    }

    public void startRecognition() {
        mShutdownRecognition = false;
        startSpeechRecognition();
        mMessageContent = "";
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
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        //mSpeechRecognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);

        mSpeechRecognizer.setRecognitionListener(this);
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
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

        if (isEndOfMessage) {
            stopMsgTimertask();
            stopPartialTimertask();
        }

//        if(!speechData.isEmpty())
        {
            String message = "";

            if (speechData.isEmpty())
                message = "";

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

        logger.info("onBufferReceived " +buffer.toString());
    }

    @Override
    public void onEndOfSpeech() {
        logger.info("Message End of Speech , onEndOfSpeech");
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
    public void onPartialResults(Bundle arg0) {

        ArrayList<String> matches = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += " "+result + " ";

        logger.info("Partial Result: " + text);
        startPartialTimer();

    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        logger.info("SpeechActivity: onReadyForSpeech"); //$NON-NLS-1$
    }

    @Override
    public void onResults(Bundle results) {
        //Log.d(TAG, "onResults"); //$NON-NLS-1$
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        mMessageContent += matches.get(0)+ " ";

        if (!isEndOfMessage)
        {
            startMsgTimer();
        }



        if (mShutdownRecognition == true) {
            return;
        }

        restartEngine();

    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    private void startPartialTimer() {
        if (msgPartialTimer != null)
            stopPartialTimertask();
        //set a new Timer
        msgPartialTimer = new Timer();
        initializePartialTimerTask();
        msgPartialTimer.schedule(msgPartialTimerTask, 5000);
    }

    public void stopPartialTimertask() {
        if (msgPartialTimer != null) {
            msgPartialTimer.cancel();
            msgPartialTimer = null;
        }
        if (msgPartialTimerTask != null) {
            msgPartialTimerTask.cancel();
            msgPartialTimerTask = null;
        }
    }

    public void initializePartialTimerTask() {

        isEndOfMessage = false;
        msgPartialTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
//                            sendSpeechBroadcast("end of message");
                        isEndOfMessage = true;
                        logger.info("initializePartialTimerTask");
                    }
                });
            }
        };

    }

    private void startMsgTimer() {
        if (msgResultTimer != null)
            stopMsgTimertask();
        //set a new Timer
        msgResultTimer = new Timer();
        initializeMsgTimerTask();
        msgResultTimer.schedule(msgResultTimerTask, 4000);
    }

    public void stopMsgTimertask() {
        if (msgResultTimer != null) {
            msgResultTimer.cancel();
            msgResultTimer = null;
        }
        if (msgResultTimerTask != null) {
            msgResultTimerTask.cancel();
            msgResultTimerTask = null;
        }
    }

    public void initializeMsgTimerTask() {

        msgResultTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        logger.info("initializeMsgTimerTask");
                        if (isEndOfMessage) {
                            String temp = mMessageContent.toLowerCase();
                            if(!temp.endsWith("end of message"))
                            {
                                mMessageContent += " end of message";
                            }

                            sendSpeechBroadcast(mMessageContent);
                        } else {
                            stopMsgTimertask();
                            startMsgTimer();
                        }

                    }
                });
            }
        };

    }


}
