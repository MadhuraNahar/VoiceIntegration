package com.test.voice.utils;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by Madhura Nahar. Copyrights reserved.
 */
public class CustomAudioManager {

    protected static AudioManager mAudioManager;
    private static CustomAudioManager ourInstance = new CustomAudioManager();
    private static Boolean isMute = false;
    private static Context mContext;

    public static CustomAudioManager getInstance(Context context) {
        mContext = context;
        return ourInstance;
    }

    public void Mute() {

        if(mAudioManager == null)
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (isMute == false) {
            Logutil.getInstance().info("Mute is true");
            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            isMute = true;
        } else {
            Logutil.getInstance().info("Mute is false");
        }
    }

    public void UnMute() {

        if(mAudioManager == null)
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (isMute == true) {
            Logutil.getInstance().info("Unmute is true");
            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_RING, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            isMute = false;
        } else {
            Logutil.getInstance().info("Unmute is false");
        }

    }
}
