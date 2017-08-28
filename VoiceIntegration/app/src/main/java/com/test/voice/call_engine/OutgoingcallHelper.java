package com.test.voice.call_engine;

/**
 * Created by Madhura Nahar.
 */

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

/**
 * Process outgoing call.
 */
public class OutgoingcallHelper {

    private VoiceService mVoiceService;
    private ContactsReader mReader;
    private Logutil logger;
    private ArrayList<Contactsmodel> mNamePhoneList;
    private ArrayList<String> mNumberlistforName;
    private String mCallToContact;
    private int mSequence;
    private CallActions mCallActions;


    public OutgoingcallHelper(VoiceService service) {
        this.mVoiceService = service;
        mCallActions = new CallActions(service);
        logger = Logutil.getInstance();
        mSequence = 0;

        if(mReader == null)
            mReader = new ContactsReader(mVoiceService);
    }

    /**
     * Method to process outgoing call
     * @param speechText
     * @param mCommand
     */
    public void processSpeechMessage(String speechText, String mCommand){

        if(!speechText.equals("") && mSequence == 0){
            String contactToCall = speechText.substring(speechText.lastIndexOf("call") + 4);
            contactToCall = contactToCall.trim();

            if(contactToCall != null || !contactToCall.equals("")){
                checkContact(contactToCall);
            }
            else {
                logger.info("Invalid contact");
            }
        }
        else if(!speechText.equals("") && mSequence == 1){
            checkContactNumber(speechText.trim());
        }
        else if(!speechText.equalsIgnoreCase("") && mSequence == 2) {
            callPhone(speechText);
        }
        else if(speechText != null && !speechText.equalsIgnoreCase("") && mSequence == 3)
        {
            if(speechText.trim().equalsIgnoreCase("Yes")) {
                checkContactNumber(mCallToContact);
            }
            else if(speechText.trim().equalsIgnoreCase("No"))
            {
                this.mVoiceService.processTextToSpeech("Ok");
                destroy();
            }else
            {
                this.mVoiceService.processTextToSpeech(ConstantErrorMessages.INVALID_COMMAND);
                destroy();
            }
        }
    }

    /**
     *  check if contactToCall is a number or name.
     * @param contactToCall
     */
    private void checkContact(String contactToCall)
    {
        if(mReader == null)
            mReader = new ContactsReader(mVoiceService);

        List<Contactsmodel> phoneList = mReader.getContactsWithName(contactToCall.trim());

        if(phoneList != null)
            mNamePhoneList = new ArrayList<Contactsmodel>(phoneList);

         if(PhoneHelper.isNumber(contactToCall.trim()))
        {
            mCallToContact = contactToCall.trim();
            // Check if the number is numeric
            sendMessageToAgent(contactToCall.trim());
        }
        else if( ! PhoneHelper.isNumber(contactToCall.trim())){
            String modifiedContact = contactToCall.replace(" ", "");

            if(PhoneHelper.isNumber(modifiedContact)){
                mCallToContact = modifiedContact.trim();

                sendMessageToAgent(modifiedContact.trim());
            }
            else {
                modifiedContact = contactToCall;
                if((phoneList != null && phoneList.size() > 1 && !phoneList.get(0).getSoundex())) {
                    String speech = TextUtils.stringFormatter(TextUtils.OUTGOINGCALLMSG_NAMES_FOUND_COUNT, phoneList.size());
                    for(int i = 0; i < phoneList.size(); i++)
                    {
                        speech += i+1;
                        speech += " ";
                        speech += phoneList.get(i).getName();
                        speech += ". ";
                    }
                    speech += TextUtils.stringFormatter(TextUtils.CALLING_NUMBER_SELECT, contactToCall);
                    mSequence = 1;

                    this.mVoiceService.processTextToSpeech(speech);
                }
                else if(phoneList != null && phoneList.get(0).getSoundex()) {

                    if(phoneList.size() > 1) {
                        String speech = TextUtils.stringFormatter(TextUtils.OUTGOINGCALLMSG_SYNONYMOUS_MULTIPLE_NAMES_FOUND, "" + phoneList.size(), contactToCall);
                        for (int i = 0; i < phoneList.size(); i++) {
                            speech += i + 1;
                            speech += " ";
                            speech += phoneList.get(i).getName();
                            speech += ". ";
                        }
                        speech += TextUtils.SYN_CALLING_NUMBER_SELECT;
                        this.mVoiceService.processTextToSpeech(speech);
                        mSequence = 1;
                    }
                    else
                    {
                        String speech = TextUtils.stringFormatter(TextUtils.OUTGOINGCALLMSG_SYNONYMOUS_NAMES_FOUND, phoneList.get(0).getName(), contactToCall,phoneList.get(0).getName());
                        this.mVoiceService.processTextToSpeech(speech);
                        mCallToContact = phoneList.get(0).getName();
                        mSequence = 3;
                    }
                }
                else if(phoneList != null && phoneList.size() == 1){
                    this.mCallToContact = contactToCall;
                    identifyMultiplePhoneOptions(phoneList.get(0));
                }
                else {
                    String error = TextUtils.stringFormatter(TextUtils.NAME_NOT_FOUND_ERROR,contactToCall);
                    this.mVoiceService.processTextToSpeech(error);
                    logger.error(error);
                    destroy();
                }
            }
        }
    }

    void callPhone(String phoneOption)
    {
        phoneOption = phoneOption.trim();
        String phoneNo = "";
        List<Contactsmodel> models = new ArrayList<>();
        for(int i=0; i < mNamePhoneList.size(); i++) {
            Contactsmodel model = mNamePhoneList.get(i);

            if(model.getName().equalsIgnoreCase(this.mCallToContact.trim()) ||
                    model.getFirstName().equalsIgnoreCase(this.mCallToContact.trim()) ||
                    model.getLastName().equalsIgnoreCase(this.mCallToContact.trim())) {

                phoneNo=callToPhoneOption(phoneOption, model);
                break;
            }
            else if (mReader.getContactListByName(this.mCallToContact,models).size()>0) {
                if(models.size() > 1){
                    this.mVoiceService.processTextToSpeech("Got multiple options in Like query. This is not acceptable");
                }
                phoneNo=callToPhoneOption(phoneOption, models.get(0));
                break;
            }

            else if (mReader.getContactListByName_Soundex(this.mCallToContact, mNamePhoneList,models).size()>0) {
                if(models.size() > 1) {
                    this.mVoiceService.processTextToSpeech("Got multiple options in sound query. This is not acceptable");
                }
                phoneNo= callToPhoneOption(phoneOption, models.get(0));
                break;
            }
        }
        sendMessageToAgent(phoneNo);
    }

    /**
     * Check for multiple numbers associated with a contact in database
     * @param model
     * @return list of multiple phone numbers
     */
    private ArrayList<String> checkforMultipleNumbers(Contactsmodel model)
    {
        ArrayList<String> phoneNos = new ArrayList<String>();

        if(!model.getMobile_num().equals("null"))
        {
            phoneNos.add(Constants.mobile);
        }
        if(!model.getWork_num().equals("null"))
        {
            phoneNos.add(Constants.work);
        }

        if(!model.getWorkMobile_num().equals("null"))
        {
            phoneNos.add(Constants.workmobile);
        }

        if(!model.getOther_num().equals("null"))
        {
            phoneNos.add(Constants.other);
        }

        if(!model.getHome_num().equals("null"))
        {
            phoneNos.add(Constants.home);
        }

        return phoneNos;
    }

    /**
     * check if contact name is present in contacts database
     * @param contactToCall name to call
     */
    private void checkContactNumber(String contactToCall)
    {
        boolean bCorrectContact = false;
        for(int i=0; i < mNamePhoneList.size(); i++)
        {
            if(mNamePhoneList.get(i).getName().equalsIgnoreCase(contactToCall))
            {
                bCorrectContact = true;
                break;
            }else if(mReader.soundex(mNamePhoneList.get(i).getName()).equalsIgnoreCase(mReader.soundex(contactToCall)))
            {
                bCorrectContact = true;
                break;
            }
        }
        if(bCorrectContact == false)
        {
            String error = TextUtils.stringFormatter(TextUtils.NAME_NOT_FOUND_ERROR,contactToCall);
            this.mVoiceService.processTextToSpeech(error);
            logger.error(error);
            destroy();
            return;
        }
        Contactsmodel model = mReader.getContactNumbersForName(contactToCall.trim(), mNamePhoneList);

        if(model == null)
        {
            String error = TextUtils.stringFormatter(TextUtils.NAME_NOT_FOUND_ERROR,contactToCall);
            this.mVoiceService.processTextToSpeech(error);
            destroy();
            return;
        }
        this.mCallToContact = contactToCall;
        identifyMultiplePhoneOptions(model);
    }

    /**
     * Identify single(or multiple) number(s) to call and process user speech for outgoing call
     * @param model
     */
    private void identifyMultiplePhoneOptions(Contactsmodel model)
    {
        ArrayList<String> phoneNos = checkforMultipleNumbers(model);

        if(phoneNos != null && phoneNos.size() > 1) {
            mNumberlistforName = new ArrayList<String>(phoneNos);
            String speech = TextUtils.stringFormatter(TextUtils.OUTGOINGCALLMSG_NOS_FOUND_COUNT, String.valueOf(phoneNos.size()),this.mCallToContact);

            for(int i = 0; i < phoneNos.size(); i++) {
                speech += i+1;
                speech += " ";
                speech += phoneNos.get(i);
                speech += ". ";
            }
            speech += TextUtils.stringFormatter(TextUtils.CALLING_TYPE_SELECT,"");
            this.mVoiceService.processTextToSpeech(speech);
            mSequence = 2;
        }
        else if(phoneNos != null && phoneNos.size() == 1) {
           callPhone(phoneNos.get(0));
        }
        else  {
            logger.info(TextUtils.stringFormatter(TextUtils.NUM_NOT_FOUND_ERROR,this.mCallToContact));
            this.mVoiceService.processTextToSpeech(TextUtils.stringFormatter(TextUtils.NUM_NOT_FOUND_ERROR,this.mCallToContact));
            destroy();
        }
    }
    /**
     * Send command to agent for processing outgoing call
     * @param phoneNumber the number to call
     */
    private void sendMessageToAgent(String phoneNumber)
    {
        if(phoneNumber.isEmpty()){
            destroy();
            return;
        }

        try {
            mVoiceService.processTextToSpeech(TextUtils.stringFormatter(TextUtils.CALLING_CONFIRMATION, this.mCallToContact));
            mCallActions.callToNumber(phoneNumber);

          } catch (Exception e) {
               logger.exception(e);
          }
    }

    private String callToPhoneOption(String phoneOption, Contactsmodel model)
    {
        String phoneNo = "";
        if(phoneOption.equalsIgnoreCase(Constants.mobile))
        {
            phoneNo = model.getMobile_num();
        }
        else if(phoneOption.equalsIgnoreCase(Constants.home))
        {
            phoneNo = model.getHome_num();
        }
        else if(phoneOption.equalsIgnoreCase(Constants.work))
        {
            phoneNo = model.getWork_num();
        }
        else if(phoneOption.equalsIgnoreCase(Constants.other))
        {
            phoneNo = model.getOther_num();
        }
        else if(phoneOption.equalsIgnoreCase(Constants.workmobile))
        {
            phoneNo = model.getWorkMobile_num();
        }

        if(phoneNo.isEmpty()){
            this.mVoiceService.processTextToSpeech(TextUtils.stringFormatter(TextUtils.EMPTY_PHONE_NUM,""));
            destroy();
            return phoneNo;
        }
        return phoneNo;
    }


    private void destroy()
    {
        if(mNamePhoneList != null)
            mNamePhoneList.clear();

        if(mNumberlistforName != null)
            mNumberlistforName.clear();

        mCallToContact = "";
        mSequence = 0;
        mVoiceService.setCommand("");

    }
}
