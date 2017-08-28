package com.test.voice.services;

/**
 * Created by Madhura Nahar.
 */

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneNumberUtils;
import com.test.voice.utils.Constants;
import com.test.voice.utils.Logutil;
import java.util.HashMap;
import java.util.Locale;


/**
 * This class contains methods to process the text to speech.
 */
public class ButlerTextToSpeech implements TextToSpeech.OnInitListener{
    private TextToSpeech mTextTospeech;
    private Context mContext;
    private Logutil mLogger;
    private String mTextToSpeak;
    private static HashMap<String, String> mParams;
    boolean isInitialized;
    protected static AudioManager mAudioManager;

    ButlerTextToSpeech(Context context, String textToSpeak)
    {
        mContext = context;
        mTextToSpeak = textToSpeak;
        mTextTospeech = new TextToSpeech(mContext, this);
        mLogger = Logutil.getInstance();

        mParams = new HashMap<String, String>();
        mParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Constants.UTTERANCEID);

        isInitialized = false;
    }

    public void onDestroy() {
        // Don't forget to shutdown mTextTospeech!
        if (mTextTospeech != null) {
            mLogger.info("In OnDestroy of TextToSpeech");
            mTextTospeech.stop();
            mTextTospeech.shutdown();
            isInitialized = false;
        }
    }

    /**
     * Check if the process is running on main thread.
     */
    private static void checkIsCalledFromMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Logutil.getInstance().error("SpeechRecognizer should be used only from the application's main thread");
            throw new RuntimeException("SpeechRecognizer should be used only from the application's main thread");
        }
    }

    @Override
    public void onInit(int status) {

        checkIsCalledFromMainThread();

        if (status == TextToSpeech.SUCCESS) {
            int result = mTextTospeech.setLanguage(Locale.US);
            mTextTospeech.setSpeechRate(0.9f);
            mTextTospeech.setPitch(1);

            isInitialized = true;
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                mLogger.error("This Language is not supported");
            } else {
//                speakOut();
            }

            mTextTospeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
						/*
						 * When myTTS is done speaking the current "audio file",
						 * call playAudio on the next audio file.
						 */
                    if (utteranceId.equals("buddyutteranceId")) {
                        // Here we are going to send broadcast to Engine..
                        muteVolume();
                        mLogger.info("Completed the text to speech");
                        startSpeech();
                    }
                }

                @Override
                public void onError(String utteranceId) {

                    muteVolume();
                    startSpeech();

                    mLogger.error("Utterance progress listener error");
                }

                @Override
                public void onStart(String utteranceId) {
                }
            });

        } else {
            mLogger.error("Initilization Failed!");
        }
    }

    /**
     * Method to speak the specified text using TTS engine.
     * @param textSpeech
     */
    public void speakOut(String textSpeech) {
        if(textSpeech != "" && isInitialized == true)
        {
            checkIsCalledFromMainThread();

            String convertedText = getProperTextToSpeak(textSpeech);
            increaseVolume();
            if(convertedText != null && !convertedText.isEmpty())
            {
                mLogger.info("TTS text: " +convertedText);
                int error = mTextTospeech.speak(convertedText, TextToSpeech.QUEUE_FLUSH, mParams);

                if(error != mTextTospeech.SUCCESS)
                    mLogger.error("Failed to speak. Please try again.");
            }
            else
            {
                mLogger.error(textSpeech);
                int error = mTextTospeech.speak(textSpeech, TextToSpeech.QUEUE_FLUSH, mParams);

                if(error != mTextTospeech.SUCCESS)
                    mLogger.error("Failed to speak. Please try again.");
            }

        }
        else
        {
            startSpeech();
            mLogger.error("Could not speak as the content is empty");
        }
    }

    /**
     * Sends broadcast intent for  text to speech completion.
     */
    public void startSpeech()
    {
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_SPEECH_COMPLETED);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * creates corrected text for TTS using the specified text
     * @param txt
     * @return
     */
    private String getProperTextToSpeak(String txt)
    {
        String properText = "";

        String[]  strList = txt.split("\\s+");

        if(strList != null && strList.length > 0)
        {
            for(int i = 0; i < strList.length; i++)
            {
                String word = strList[i];

                if(word != null && !word.equals(""))
                {
                    if(isNumber(word) == true)
                    {
                        String spaceWord = addSpaceInWord(word);

                        if(spaceWord != null && !spaceWord.isEmpty())
                        {
                            properText += spaceWord;
                            properText += " ";
                        }
                    }
                    else
                    {
                        properText += word;
                        properText += " ";
                    }
                }
            }

            return properText;
        }

        return null;
    }

    private boolean isNumber(String word)
    {
        boolean isNumber = false;
        try
        {

            if(PhoneNumberUtils.isGlobalPhoneNumber(word))
                return true;


            String regex = "^[0-9]+";
            if(word.matches(regex) == true)
                return true;

            return false;

        } catch (NumberFormatException e)
        {
            isNumber = false;
        }
        return isNumber;
    }

    private String addSpaceInWord(String word)
    {
        String spacedWord = "";
        for(int i = 0 ; i < word.length(); i++)
        {
            spacedWord += word.charAt(i);
            spacedWord += " ";
        }

        return spacedWord;
    }

    private void increaseVolume()
    {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
        }
        else*/
        {
            int vol = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            vol = (int) (vol * 0.90);

            mAudioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    vol,
                    0);

            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_RING, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 1,0);

//            int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//            float percent = 0.99f;
//            int seventyVolume = (int) (maxVolume*percent);
//            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);

        }
    }

    private void muteVolume()
    {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
        }
        else*/
        {
            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }
}
