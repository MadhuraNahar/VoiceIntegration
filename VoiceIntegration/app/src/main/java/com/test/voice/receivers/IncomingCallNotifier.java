package com.test.voice.receivers;

/**
 * Created by Madhura Nahar. Copyrights reserved.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.test.voice.contacts.ContactsReader;
import com.test.voice.jsonutils.JSONCreator;
import com.test.voice.model.JSONCallModel;
import com.test.voice.services.VoiceService;
import com.test.voice.utils.Constants;
import com.test.voice.utils.Logutil;
import com.test.voice.utils.PhoneHelper;


/**
 *   This receiver is called when the BroadcastReceiver is receiving an Intent broadcast for incoming call.
 */
public class IncomingCallNotifier extends BroadcastReceiver {

    TelephonyManager mTelephonyManager;
    Logutil logger = Logutil.getInstance();
    Handler mHandler;
    ContactsReader contactsReader;
    String mIncomingNumber;

    public static int mLastState;
    public IncomingCallNotifier() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        mTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        contactsReader = new ContactsReader(context);

        //Create Listener
        PhoneStateListener phoneListener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);

                logger.info("Phone state Listener: " +state);

                if (state == TelephonyManager.CALL_STATE_RINGING){
                    String message = "Incoming call from: " +incomingNumber;
                    logger.info(message);

                    if(state != mLastState){
                        mLastState = state;
                        logger.info("Last state: "+mLastState +"Current State: "+state);
                        Log.d("Incoming call", incomingNumber);
                        String name= contactsReader.getContactName(incomingNumber);
                        sendJSONMessage(name,incomingNumber);
                    }
                }
                if (state == TelephonyManager.CALL_STATE_IDLE || state == TelephonyManager.CALL_STATE_OFFHOOK)
                {
                    mLastState = state;
                }
            }
        };
        //Register listener for LISTEN_CALL_STATE
        mTelephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    private void sendJSONMessage(String name, String incomingNumber) {

        JSONCallModel model = new JSONCallModel();
        model.setCommandName(Constants.INCOMING_CALL);
        model.setNumber(incomingNumber);
        model.setName(name);
        String jsonString = JSONCreator.createJSON(model);
        logger.info("JSON for incoming call: " + jsonString);

        VoiceService.notifyHandler(jsonString);


    }
}
