package com.test.voice.call_engine;

/**
 * Created by Madhura Nahar.
 */

import com.test.voice.contacts.ContactsReader;
import com.test.voice.jsonutils.JSONCreator;
import com.test.voice.model.JSONCallModel;
import com.test.voice.model.JSONModel;
import com.test.voice.services.VoiceService;
import com.test.voice.utils.Logutil;


/**
 * Intercept incoming call.
 */
public class CallInterceptor {

    private ContactsReader mReader;
    private Logutil logger;
    private VoiceService mVoiceService;
//    private ButlerClientSocket mClientSocket;     //New Comment
    private boolean numberFlag = false;
    private CallActions mCallActions;


    public CallInterceptor(VoiceService service) {
        this.mVoiceService = service;
        mCallActions = new CallActions(service);
        logger = Logutil.getInstance();

        if(mReader == null)
            mReader = new ContactsReader(mVoiceService);
    }

    /**
     * check for incoming call number in contacts database.
     * @param model
     */
    public String checkPhoneContact(JSONCallModel model){
        String contactName = null;
        if(model != null){
            String name = model.getName().toString();
            String number = model.getNumber().toString();
            if (!name.isEmpty() || !name.equals("")) {
                return name;
            }

            number = number.trim();
            if(number == null || number.equals(""))
                return "";

            String subStr = number;
            if(number.length() >= 13)
                subStr = number.substring(3,number.length());

            if(mReader == null)
                mReader = new ContactsReader(mVoiceService);

            contactName = mReader.getContactName(subStr);

            if(contactName == null)
                contactName = mReader.getContactName(number);

            if(contactName != null){
                logger.info("Incoming call from " +contactName);
               return contactName;
            }else if(contactName == null){
                logger.info("Incoming call from " +number);
                return number;
            }
        }
        return "";
    }

    /**
     * Process user response for receiving or disconnecting incoming call.
     * @param speech user response string
     * @param mCommand
     */
    public void processSpeechMessage(String speech, String mCommand){

        JSONModel model = new JSONModel();

        String speechText = speech.toLowerCase().trim();

        if(speechText.equalsIgnoreCase("yes")){
            logger.info("Command received: " +speechText + " accept call");
            mCallActions.answerCall();
        }

        else if (speechText.equalsIgnoreCase("No")){
            logger.info("Command received: " + speechText + " Reject call");
            mCallActions.disconnectCall();
        }
        else {
            speechText = null;
        }
    }
}
