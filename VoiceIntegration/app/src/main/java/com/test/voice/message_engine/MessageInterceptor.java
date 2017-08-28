package com.test.voice.message_engine;

import com.test.voice.contacts.ContactsReader;
import com.test.voice.model.JSONMessageModel;
import com.test.voice.model.MessageModel;
import com.test.voice.services.VoiceService;
import com.test.voice.utils.ConstantErrorMessages;
import com.test.voice.utils.Constants;
import com.test.voice.utils.Logutil;

/**
 * Created by Madhura Nahar.
 */

public class MessageInterceptor {

    private ContactsReader mReader;
    private Logutil logger;
    private VoiceService mVoiceService;
    private boolean numberFlag = false;
    MessageModel messageDetails;
    private String mContactNumber;

    private String mContactName;

    public MessageInterceptor(VoiceService voiceservice) {
        this.mVoiceService = voiceservice;
        logger = Logutil.getInstance();

        if (mReader == null)
            mReader = new ContactsReader(mVoiceService);
    }

    /**
     * check for incoming call number in contacts database.
     *
     * @param model
     */
    public String checkMessageContact(JSONMessageModel model) {
        String contactName = null;
        if (model != null) {
            messageDetails = model.getResult();

            String name = "";

            if(messageDetails != null && messageDetails.getName() != null)
                name = messageDetails.getName().toString();

            String number = "";

            if(messageDetails != null && messageDetails.getNumber() != null)
                number = messageDetails.getNumber().toString();

            mContactNumber = number;
            if (!name.equals("")|| !name.isEmpty())
            {
                mContactName = name;
                return name;
            }
            logger.info("Message content: " + messageDetails.getMessageContent());

            number = messageDetails.getNumber();
            number = number.trim();
            if (number == null || number.equals(""))
                return "";

            String subStr = number;
            if (number.length() >= 13)
                subStr = number.substring(3, number.length());

            if (mReader == null)
                mReader = new ContactsReader(mVoiceService);

            contactName = mReader.getContactName(subStr);

            if (contactName == null)
                contactName = mReader.getContactName(number);

            if (contactName != null) {
                logger.info("Message sent from " + contactName);
                mContactName = name;
                return contactName;
            } else if (contactName == null) {
                logger.info("Message sent from  " + number);
                return number;
            }
        }
        return "";
    }

    public void processSpeechMessage(String speech) {

        String speechText = speech.toLowerCase().trim();
        logger.info("Speech Text Received: " + speechText);
        if (speechText.equalsIgnoreCase("yes")) {
            if (messageDetails != null)
            {
                mVoiceService.processTextToSpeech(messageDetails.getMessageContent() + "                 " + "Do you want to reply .");
                if(mVoiceService != null)
                    mVoiceService.setCommand(Constants.INCOMING_MSG_REPLY);
            }

        } else if (speechText.equalsIgnoreCase("no")) {
            messageDetails = null;
            mVoiceService.processTextToSpeech("OK");
            if(mVoiceService != null)
                mVoiceService.setCommand("");

        } else {
            speechText = null;
            mVoiceService.processTextToSpeech(ConstantErrorMessages.INVALID_COMMAND);
            if(mVoiceService != null)
                mVoiceService.setCommand("");
        }
    }

    public void processReplyMessage(String speech)
    {
        String speechText = speech.toLowerCase().trim();
        logger.info("Speech Text Received: " +speechText);
        if(speechText.equalsIgnoreCase("yes")){

            mVoiceService.getSendMessage().mSequence = 3;
            mVoiceService.setCommand(Constants.SEND_MSG);
            mVoiceService.getSendMessage().setPhoneNumberToSend(mContactNumber,mContactName);

            mVoiceService.processTextToSpeech("Please speak your message. ");
        }

        else if (speechText.equalsIgnoreCase("no")){
            messageDetails=null;
            mVoiceService.processTextToSpeech("OK");
        }
        else {
            speechText = null;
            mVoiceService.processTextToSpeech(ConstantErrorMessages.INVALID_COMMAND);
        }
    }

}
