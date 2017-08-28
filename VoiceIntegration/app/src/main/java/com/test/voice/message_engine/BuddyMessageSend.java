package com.test.voice.message_engine;

/**
 * Created by Madhura Nahar.
 */
import android.telephony.SmsManager;

import com.test.voice.contacts.ContactsReader;
import com.test.voice.model.Contactsmodel;
import com.test.voice.services.VoiceService;
import com.test.voice.utils.ConstantErrorMessages;
import com.test.voice.utils.Constants;
import com.test.voice.utils.Logutil;
import com.test.voice.utils.PhoneHelper;
import com.test.voice.utils.TextUtils;
import java.util.ArrayList;
import java.util.List;


public class BuddyMessageSend {

    private VoiceService mVoiceService;
    private Logutil logger;
    private ContactsReader mReader;
    private ArrayList<Contactsmodel> mNamePhoneList;
    private ArrayList<String> mNumberlistforName;

    public int mSequence;
    private String numberToSendMessage;
    private String messageContentToSend = "";
    private String initialMsgContent;
    private String mContactNameToMessage;


    public BuddyMessageSend(VoiceService voiceService) {
        this.mVoiceService = voiceService;
        logger = Logutil.getInstance();

        mSequence = 0;
        if (mReader == null) {
            mReader = new ContactsReader(mVoiceService);
        }
    }

    /**
     * Method to process send message
     *
     * @param speechText
     * @param mCommand
     */
    public void processSMSSpeech(String speechText, String mCommand) {

        if (!speechText.equals("") && mSequence == 0) {

            String contactToMsg = speechText.substring(speechText.lastIndexOf("send message to") + 15);
            if (contactToMsg != null || !contactToMsg.equals("")) {
//                logger.info(contactToMsg);
                checkContact(contactToMsg);
            } else {
                logger.info("Invalid contact");
            }
        }
        else if (!speechText.equals("") && mSequence == 1) {
            checkContactNumber(speechText.trim());
        }
        else if (!speechText.equalsIgnoreCase("") && mSequence == 2) {
            messagePhone(speechText);
        }
        else if(!speechText.equalsIgnoreCase("") && mSequence == 3 ){

            if(initialMsgContent == null){
                initialMsgContent = speechText;

                if ((initialMsgContent.toLowerCase()).endsWith(" end of message")){


                    initialMsgContent = initialMsgContent.replace(initialMsgContent.substring(initialMsgContent.lastIndexOf("end of message") - 1),"");
                    logger.info("Final Sending message" +initialMsgContent);
                    confirmUserMessage(initialMsgContent);
                }
                else {
                    logger.info("Final user message" +initialMsgContent);
                }
            }
            else {
                initialMsgContent = initialMsgContent +". " +speechText;
                if ((initialMsgContent.toLowerCase()).endsWith(" end of message")){
                    initialMsgContent = initialMsgContent.replace(initialMsgContent.substring(initialMsgContent.lastIndexOf("end of message") - 1),"");
                    logger.info("Final Sending message" +initialMsgContent);
                    confirmUserMessage(initialMsgContent);
                }
                else {
                    logger.info("Final user message" +initialMsgContent);
                }
            }
        }
        else if(speechText != null && !speechText.equalsIgnoreCase("") && mSequence == 4){
            speechText = speechText.trim();
            if(speechText.equalsIgnoreCase("yes")){
                sendMessageWithContent(messageContentToSend);
            }
            else if(speechText.equalsIgnoreCase("no")) {
                mVoiceService.processTextToSpeech(" Okay");
                destroy();
                return;
            }
            else {
                this.mVoiceService.processTextToSpeech(ConstantErrorMessages.INVALID_COMMAND);
                destroy();
                return;
            }
        }
    }

    private void sendMessageWithContent(String messageContent) {

        if(numberToSendMessage != null){
            sendMessageToAgent(numberToSendMessage, messageContent);
        }
    }
    /**
     * Method to confirm message content before sending.
     * @param speechText
     */
    private void confirmUserMessage(String speechText) {

        this.messageContentToSend = speechText;

        String messageConfirmation = TextUtils.stringFormatter(TextUtils.USER_MESSAGE_CONFIRM, speechText);
        initialMsgContent = null;
        mSequence = 4;
        this.mVoiceService.processTextToSpeech(messageConfirmation);
    }
    /**
     * check if contactToMsg is a number or name.
     *
     * @param contactToMsg
     */

    public void checkContact(String contactToMsg) {

        if (mReader == null)
            mReader = new ContactsReader(mVoiceService);

        List<Contactsmodel> phoneList = mReader.getContactsWithName(contactToMsg.trim());

        if (phoneList != null)
            mNamePhoneList = new ArrayList<Contactsmodel>(phoneList);

        if(PhoneHelper.isNumber(contactToMsg.trim()))
        {
            this.numberToSendMessage = contactToMsg.trim();
            this.mContactNameToMessage = numberToSendMessage;
            // Check if the number is numeric
            mSequence = 3;
            getMessageContent();
        }
        else if( ! PhoneHelper.isNumber(contactToMsg.trim())) {

            String modifiedContact = contactToMsg.replace(" ", "");
            if(PhoneHelper.isNumber(modifiedContact)){
                numberToSendMessage = modifiedContact.trim();
                this.mContactNameToMessage = numberToSendMessage;

                mSequence = 3;
                getMessageContent();
            }
            else {
                modifiedContact = contactToMsg;
                if(phoneList != null && phoneList.size() > 1){

                    String speech = TextUtils.stringFormatter(TextUtils.OUTGOINGCALLMSG_NAMES_FOUND_COUNT, phoneList.size());
                    for(int i = 0; i < phoneList.size(); i++){
                        speech += i+1;
                        speech += " ";
                        speech += phoneList.get(i).getName();
                        speech += " ";
                    }
                    speech += TextUtils.stringFormatter(TextUtils.MSGING_NUMBER_SELECT, contactToMsg);
                    mSequence = 1;
                    this.mVoiceService.processTextToSpeech(speech);
                }

                else if(phoneList != null && phoneList.size() == 1) {
                    this.numberToSendMessage = contactToMsg;
                    this.mContactNameToMessage = numberToSendMessage;
                    identifyMultiplePhoneOptions(phoneList.get(0));
                }
                else {
                    String error = TextUtils.stringFormatter(TextUtils.NAME_NOT_FOUND_ERROR,contactToMsg);
                    this.mVoiceService.processTextToSpeech(error);
                    logger.error(error);
                    destroy();
                }
            }
        }
    }


    private void getMessageContent(){
        String messageContent = "";
        messageContent = TextUtils.stringFormatter(TextUtils.SPEAK_MESSAGE, "");
        this.mVoiceService.processTextToSpeech(messageContent);
    }

    private void messagePhone(String phoneOption) {
        String phoneNo = "";
        for (int i = 0; i < mNamePhoneList.size(); i++) {
            Contactsmodel model = mNamePhoneList.get(i);
            if(model.getName().equalsIgnoreCase(this.numberToSendMessage.trim()) ||
                    model.getFirstName().equalsIgnoreCase(this.numberToSendMessage.trim()) ||
                    model.getLastName().equalsIgnoreCase(this.numberToSendMessage.trim())) {
                if (phoneOption.equalsIgnoreCase(Constants.mobile)) {
                    phoneNo = model.getMobile_num();
                } else if (phoneOption.equalsIgnoreCase(Constants.home)) {
                    phoneNo = model.getHome_num();
                } else if (phoneOption.equalsIgnoreCase(Constants.work)) {
                    phoneNo = model.getWork_num();
                } else if (phoneOption.equalsIgnoreCase(Constants.other)) {
                    phoneNo = model.getOther_num();
                } else if (phoneOption.equalsIgnoreCase(Constants.workmobile)) {
                    phoneNo = model.getWorkMobile_num();
                }

                if (phoneNo.isEmpty()) {
                    this.mVoiceService.processTextToSpeech(TextUtils.stringFormatter(TextUtils.EMPTY_PHONE_NUM, ""));
                    destroy();
                    return;
                }
                break;
            }
        }
        this.numberToSendMessage = phoneNo;
        mSequence = 3;
        getMessageContent();
    }

    /**
     * Check for multiple numbers associated with a contact in database
     *
     * @param model
     * @return list of multiple phone numbers
     */
    private ArrayList<String> checkforMultipleNumbers(Contactsmodel model) {
        ArrayList<String> phoneNos = new ArrayList<String>();

        if (!model.getMobile_num().equals("null")) {
            phoneNos.add(Constants.mobile);
        }
        if (!model.getWork_num().equals("null")) {
            phoneNos.add(Constants.work);
        }
        if (!model.getWorkMobile_num().equals("null")) {
            phoneNos.add(Constants.workmobile);
        }
        if (!model.getOther_num().equals("null")) {
            phoneNos.add(Constants.other);
        }
        if (!model.getHome_num().equals("null")) {
            phoneNos.add(Constants.home);
        }
        return phoneNos;
    }

    /**
     * check if contact name is present in contacts database
     *
     * @param contactToMessage name to send message
     */
    private void checkContactNumber(String contactToMessage) {
        boolean bCorrectContact = false;
        for (int i = 0; i < mNamePhoneList.size(); i++) {
            if (mNamePhoneList.get(i).getName().equalsIgnoreCase(contactToMessage)) {
                bCorrectContact = true;
                break;
            }
        }

        if (bCorrectContact == false) {
            String error = TextUtils.stringFormatter(TextUtils.NAME_NOT_FOUND_ERROR, contactToMessage);
            this.mVoiceService.processTextToSpeech(error);
            logger.error(error);
            destroy();
            return;
        }

        Contactsmodel model = mReader.getContactNumbersForName(contactToMessage.trim(),mNamePhoneList);

        if (model == null) {
            String error = TextUtils.stringFormatter(TextUtils.NAME_NOT_FOUND_ERROR, contactToMessage);
            this.mVoiceService.processTextToSpeech(error);
            destroy();
            return;
        }
        this.numberToSendMessage = contactToMessage;
        identifyMultiplePhoneOptions(model);
    }

    /**
     * Identify single(or multiple) number(s) to call and process user speech for outgoing call
     *
     * @param model
     */
    private void identifyMultiplePhoneOptions(Contactsmodel model) {
        ArrayList<String> phoneNos = checkforMultipleNumbers(model);

        if (phoneNos != null && phoneNos.size() > 1) {

            mNumberlistforName = new ArrayList<String>(phoneNos);
            String speech = TextUtils.stringFormatter(TextUtils.OUTGOINGCALLMSG_NOS_FOUND_COUNT, String.valueOf(phoneNos.size()), this.numberToSendMessage);

            for (int i = 0; i < phoneNos.size(); i++) {
                speech += i + 1;
                speech += " ";
                speech += phoneNos.get(i);
                speech += " ";
            }
            speech += TextUtils.stringFormatter(TextUtils.MSGING_NUMBER_TYPE_SELECT, "");
            this.mVoiceService.processTextToSpeech(speech);
            logger.info(speech);
            mSequence = 2;
        }
        else if (phoneNos != null && phoneNos.size() == 1) {
            messagePhone(phoneNos.get(0));
        }
        else {
            this.mVoiceService.processTextToSpeech(TextUtils.stringFormatter(TextUtils.NUM_NOT_FOUND_ERROR, this.numberToSendMessage));
            destroy();
        }
    }

    /**
     * Send command to agent for processing sending message
     *
     * @param phoneNumber the number to message
     */

    private void sendMessageToAgent(String phoneNumber, String messageContent) {

        if (phoneNumber.isEmpty()) {
            destroy();
            return;
        }

        if(this.mContactNameToMessage!=null)
            mVoiceService.processTextToSpeech("Sending message to " + this.mContactNameToMessage);
        else
            mVoiceService.processTextToSpeech("Sending message to " + phoneNumber);

        sendMessageToRecipient(phoneNumber,messageContent);
        destroy();
    }

    private void destroy() {
        if (mNamePhoneList != null)
            mNamePhoneList.clear();

        if (mNumberlistforName != null)
            mNumberlistforName.clear();

        mSequence = 0;
        numberToSendMessage = "";
        this.mContactNameToMessage = "";
        mVoiceService.setCommand("");

    }

    public void setPhoneNumberToSend(String phNumber, String phName)
    {
        numberToSendMessage = phNumber;
       this.mContactNameToMessage = phName;
    }

    public void sendMessageToRecipient(String phoneNumber, String messageContent){

        phoneNumber = phoneNumber.trim();
        SmsManager smsManager = SmsManager.getDefault();

        ArrayList<String> messageParts = smsManager.divideMessage(messageContent);
        // code to send long message, if message length exceeds 160 characters.
        smsManager.sendMultipartTextMessage(phoneNumber, null, messageParts, null, null);
        logger.info("Message sent to recipient.. " +phoneNumber);
    }
}


