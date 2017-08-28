package com.test.voice.receivers;

/**
 * Created by Madhura Nahar.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.test.voice.services.VoiceService;


/**
 * This class is used to send broadcast speech recognition restart
 */
public class BuddySpeechReciver extends BroadcastReceiver {

    private Handler mHandler;

    public BuddySpeechReciver() {
    }

    /**
     * Constructor to send broadcast speech recognition restart
     * @param handler
     */
    public BuddySpeechReciver(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("com.voice.activity.stop")) {

            VoiceService.speechActivityNotifier();
        }

    }
}
