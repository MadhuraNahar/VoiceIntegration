package com.test.voice.receivers;

/**
 * Created by Madhura Nahar.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.test.voice.utils.Logutil;

public class OutgoingCallReceiver extends BroadcastReceiver {

    Logutil logger = Logutil.getInstance();

    public OutgoingCallReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final String originalNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        String msg = "Intercepted outgoing call. Old number " + originalNumber ;
        logger.info(msg);

        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        logger.info("RINGING");
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        logger.info("OFFHOOK");

                        break;
                    case TelephonyManager.CALL_STATE_IDLE:

                        logger.info("IDLE");
                        break;
                    default:
                        logger.info("Default: " + state);
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

    }
}
