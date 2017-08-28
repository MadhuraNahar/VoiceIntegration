package com.test.voice.receivers;

/**
 * Created by Madhura Nahar. Copyrights reserved.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.test.voice.contacts.ContactsReader;
import com.test.voice.jsonutils.JSONCreator;
import com.test.voice.model.JSONModel;
import com.test.voice.model.MessageModel;
import com.test.voice.services.VoiceService;
import com.test.voice.utils.Constants;
import com.test.voice.utils.Logutil;

public class IncomingMessageNotifier extends BroadcastReceiver {

    Logutil logger = Logutil.getInstance();
    ContactsReader contactsReader;

    public IncomingMessageNotifier() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            contactsReader = new ContactsReader(context);

            Bundle bundle = intent.getExtras();
            if(bundle != null){
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] msgs = new SmsMessage[pdus.length];
                String messageReceived = "";
                String msgSenderNum = "";
                String name = "";
                for (int i = 0; i < pdus.length; i++)
                {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    msgSenderNum = msgs[i].getOriginatingAddress();
                    messageReceived += msgs[i].getDisplayMessageBody();

                }

                logger.info("Message from: " + msgSenderNum + " Message: " + messageReceived);
                name=contactsReader.getContactName(msgSenderNum);

                sendJSONMessage(name,msgSenderNum,messageReceived);
            }

        }

    }

    private void sendJSONMessage(String name, String incomingSMSnumber, String messageContent){

        MessageModel msgModel = new MessageModel();
        msgModel.setNumber(incomingSMSnumber);
        msgModel.setName(name);
        msgModel.setMessageContent(messageContent);

        String incomingMsgJson = JSONCreator.createJSON(msgModel);
        logger.info("Incoming message JSON string is:\n" + incomingMsgJson);

        JSONModel model = new JSONModel();
        model.setCommandName(Constants.INCOMING_MSG);
        model.setResult(msgModel);
        String jsonString = JSONCreator.createJSON(model);

//        msgModel.getMessageContent()
        logger.info("JSON for incoming message is: " + jsonString);

        VoiceService.notifyHandler(jsonString);
    }
}
