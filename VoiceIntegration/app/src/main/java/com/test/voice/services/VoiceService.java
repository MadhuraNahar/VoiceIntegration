package com.test.voice.services;

/**
 * Created by Madhura Nahar.
 */
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.test.voice.MainActivity;
import com.test.voice.call_engine.CallInterceptor;
import com.test.voice.call_engine.OutgoingcallHelper;
import com.test.voice.contacts.ContactsReader;
import com.test.voice.jsonutils.JSONParser;
import com.test.voice.message_engine.BuddyMessageIntermediate;
import com.test.voice.message_engine.BuddyMessageSend;
import com.test.voice.message_engine.MessageInterceptor;
import com.test.voice.model.Contactsmodel;
import com.test.voice.model.JSONCallModel;
import com.test.voice.model.JSONMessageModel;
import com.test.voice.model.JSONModel;
import com.test.voice.utils.ConstantErrorMessages;
import com.test.voice.utils.Constants;
import com.test.voice.utils.Logutil;
import com.test.voice.utils.TextUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VoiceService extends Service {

    private Timer mTimer;
    private TimerTask mTimerTask;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private static Handler sHandler;
    private ButlerSpeechRecognizer mSpeechRecognizer;
    private ButlerTextToSpeech textEngine;
    private static Logutil logger;
    private ContactsReader reader;
    private CallInterceptor mCallInterceptor;
    private OutgoingcallHelper mOutgoingCall;
    private BuddyMessageSend mSendMessage;
    private String mCommand;
    private ButlerSpeechIntermediate mSpeechIntermediate;

    private BuddyMessageIntermediate mMessageIntermediate;
    private String speechText;

    private MessageInterceptor mMessageInterceptor;
    private Boolean mHeyAutoSpokenEarlier = false;
    ScheduledThreadPoolExecutor sched_disconnect;


    public VoiceService() {
        mCommand = "";
        speechText = "";
    }

    private Handler msgHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    /*
                     messageToRead : parse, encryption/ decryption and delegate to appropriate component(ex: call or Message).
                     */
                    final String message = (msg.getData().getString(Constants.USER_MESSAGE));
                    mainThreadHandler.post(new Runnable() {
                        public void run() {
                            parseAndExecuteCommand(message);

                        }
                    });
                    break;
                case Constants.MESSAGE_WRITE:
                    break;

                case Constants.BUDDY_SPEECH_RECEIVE:
                    final String speechMsg = msg.getData().getString(Constants.USER_SPEECH_DATA);
                    if (speechMsg != null) {
                        speechText = speechMsg;
                        processSpeechRecognition(speechMsg);
                    }
                    break;

                case Constants.BUDDY_SPEECH_ACTIVITY_DATA:

                    final String messageSpeech = (msg.getData().getString(Constants.USER_SPEECH_DATA));
                    mainThreadHandler.post(new Runnable() {
                        public void run() {

                            if (messageSpeech.equalsIgnoreCase("Error")) {
                                mCommand = "";
                                return;
                            }

                            if (mSpeechRecognizer == null)
                                mSpeechRecognizer = ButlerSpeechRecognizer.getInstance(VoiceService.this, msgHandler);

                            mSpeechRecognizer.setupspeech();
                            speechText = messageSpeech;
                            if (speechText != null) {
                                processSpeechRecognition(speechText);
                            }
                        }
                    });
                    break;

                case Constants.BUDDY_SPEECH_RESTART:
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (mSpeechRecognizer != null) {
                                mSpeechRecognizer.onDestroy();
                                mSpeechRecognizer = null;
                            }
                            if (mSpeechRecognizer == null) {
                                mSpeechRecognizer = ButlerSpeechRecognizer.getInstance(VoiceService.this, msgHandler);
                            }
                            mSpeechRecognizer.setupspeech();
                        }
                    });
                    break;
            }
            return false;
        }
    });


    public static void notifyHandler(String newJsonString){

        if(sHandler != null){
            Message msg = sHandler.obtainMessage(Constants.MESSAGE_READ);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.USER_MESSAGE, newJsonString);
            msg.setData(bundle);
            sHandler.sendMessage(msg);

        }
    }

    /**
     * method to notify speech restart
     */
    public static void speechActivityNotifier() {

        if (sHandler != null) {
            Message msg = sHandler.obtainMessage(Constants.BUDDY_SPEECH_RESTART);
            sHandler.sendMessage(msg);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger = Logutil.getInstance();
        logger.info("Voice Service Created...");
        sHandler = msgHandler;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("Butler service started");
        initialiseEngine();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.info("In OnDestroy of ButlerService");

        mSpeechRecognizer.onDestroy();
        textEngine.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private void initialiseEngine() {

        // Initialize Logging
        logger.deviceInfo();

        // Initialize Speech Engine
        textEngine = new ButlerTextToSpeech(VoiceService.this, "");
        mSpeechRecognizer = ButlerSpeechRecognizer.getInstance(VoiceService.this, msgHandler);
        mSpeechRecognizer.setupspeech();
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mBroadcast, new IntentFilter(Constants.BROADCAST_SPEECH_COMPLETED));

        try {
            syncAgentContacts();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver mBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(Constants.BROADCAST_SPEECH_COMPLETED)) {
                textToSpeechComplete();
            }
        }
    };

    public void textToSpeechComplete() {

        logger.info("TTS completed.. Start the speech now");

        if(speechText.equalsIgnoreCase(Constants.HOTWORD) ||speechText.equalsIgnoreCase(Constants.HOTWORD2) || !mCommand.isEmpty())
        {
            if(mCommand.equals(Constants.SEND_MSG) && mSendMessage.mSequence == 3){

                if(mMessageIntermediate == null)
                    mMessageIntermediate = new BuddyMessageIntermediate(VoiceService.this, msgHandler);

                mMessageIntermediate.startRecognition();
            }
            else {
                if(mSpeechIntermediate == null)
                    mSpeechIntermediate = new ButlerSpeechIntermediate(VoiceService.this, msgHandler);

                mSpeechIntermediate.startRecognition();
            }

        } else {
            mSpeechRecognizer.setupspeech();
        }
    }

    /**
     * Sync agent contacts in local contacts database
     *
     * @param
     * @throws JSONException
     */
    private void syncAgentContacts() throws JSONException {

       /* if (model != null) {*/    //new Comment
            /*List<Contactsmodel> result = model.getResult();*/ //new comment

        if (reader == null)
            reader = new ContactsReader(this);
        ArrayList<Contactsmodel> result = reader.getContactsFromAddressBook();

        if (result == null)
            return;

        if (result.size() == 0) {
            reader.deleteAllContacts();
            return;
        }

        boolean success = reader.addContactsToDB(result);

        if (success == true) {
            logger.info("Contacts added to BUTLER database..");
            List<Contactsmodel> contactList = new ArrayList<Contactsmodel>();
            contactList = reader.getContacts();
            logger.info("Contacts list size: " + contactList.size());

            processTextToSpeech("I am ready.");

            JSONModel jsonModel = new JSONModel();
            jsonModel.setCommandName(Constants.CONTACTS_ADDED);
            jsonModel.setResult("Contacts Added to Butler");

        } else {
            return;
        }
       /* }*/   //new Comment
    }

    private void parseAndExecuteCommand(String jsonString) {
        JSONModel model = (JSONModel) JSONParser.parseJSON(jsonString);
        if (model != null) {
            processCommands(model.getCommandName(), jsonString);
        }
    }

    public void processCommands(String command, String jsonString) {

        JSONModel model = (JSONModel) JSONParser.parseJSON(jsonString);
        switch (command) {
            case Constants.INCOMING_CALL:
                mCommand = command;
                JSONCallModel callModel = JSONParser.parseCallJSON(jsonString);
                interceptIncomingCall(callModel);
                break;

            case Constants.INCOMING_MSG:
                mCommand = command;
                JSONMessageModel messageModel = JSONParser.parseMessageJSON(jsonString);
                interceptIncomingMessage(messageModel);
                break;

            case Constants.OUTGOING_CALL_ERROR:
                processTextToSpeech("Your phone sim card not detected. Please try again later");
                break;
        }
    }
    /**
     * Method to intercept incoming call and check phone contact.
     *
     * @param model
     */
    public void interceptIncomingCall(JSONCallModel model) {
        if (model != null) {
            if (mCallInterceptor == null) {
                mCallInterceptor = new CallInterceptor(this);
            }
            String name = mCallInterceptor.checkPhoneContact(model);
            processTextToSpeech("New incoming call from " + name + ". Do you wish to answer the call?");
        }
    }

    private void interceptIncomingMessage(JSONMessageModel model) {

        if (model != null) {
            if (mMessageInterceptor == null) {
                mMessageInterceptor = new MessageInterceptor(this);
            }

            String name = mMessageInterceptor.checkMessageContact(model);
            processTextToSpeech("New message from " + name + " Do you want to read?");
        }
    }

    /**
     * Check for "hey buddy" keyword in the specified speechText
     *
     * @param speechText
     * @return true if "ok auto" keyword present, false otherwise
     */
    private Boolean checkforHeyAuto(String speechText) {
        Boolean retVal = false;
        if (speechText.equalsIgnoreCase(Constants.HOTWORD) || speechText.equalsIgnoreCase(Constants.HOTWORD2)) {
            retVal = true;
            mainThreadHandler.post(new Runnable() {
                public void run() {

                    if (mSpeechIntermediate == null)
                        mSpeechIntermediate = new ButlerSpeechIntermediate(VoiceService.this, msgHandler);
                        mSpeechIntermediate = new ButlerSpeechIntermediate(VoiceService.this, msgHandler);

                    processTextToSpeech(TextUtils.stringFormatter(TextUtils.ASK_USER_TASK, ""));
                    mHeyAutoSpokenEarlier = true;
                }
            });
        }
        return retVal;
    }

    /**
     * Process the text message as speech using TTS engine
     *
     * @param message
     */
    public void processTextToSpeech(final String message) {

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSpeechRecognizer != null)
                    mSpeechRecognizer.shutdownSpeech();

                if((mCommand.equals(Constants.SEND_MSG) && mSendMessage.mSequence == 3) ||
                        (mSendMessage != null && mSendMessage.mSequence == 4)){

                    if(mMessageIntermediate != null) {
                        mMessageIntermediate.shutdown();
                        mMessageIntermediate=null;
                    }
                }
                else {

                    if(mSpeechIntermediate != null)
                        mSpeechIntermediate.shutdown();
                }
                if(mSpeechIntermediate != null)
                    mSpeechIntermediate.shutdown();
                if (textEngine != null)
                    textEngine.speakOut(message);
            }
        });
    }

    /**
     * Recognise speech and process the speechText for appropriate command.
     *
     * @param speechText
     */
    public void processSpeechRecognition(String speechText) {

        String text = speechText.toLowerCase();
        if (text.equalsIgnoreCase("error")) {
            mCommand = "";
            return;
        }
        if (!checkforHeyAuto(text)) {
            if (mCommand.isEmpty()) {
                if (!this.speechText.contains(Constants.HOTWORD) && mHeyAutoSpokenEarlier == true)
                    this.speechText = Constants.HOTWORD + " " + text.trim();
                else
                    this.speechText = text;

                getCommandType(this.speechText);
                processCommand(this.speechText);

                mHeyAutoSpokenEarlier = false;
            } else {
                getCommandType(text);
                processCommand(text);

                mHeyAutoSpokenEarlier = false;
            }
        }
    }

    /**
     * Get the type of command for particular speech
     *
     * @param speechText
     * @return integer as per the command.
     */
    private int getCommandType(String speechText) {
        int retVal = 0;
        if (!speechText.equals(null)) {

            int command = getCommand(speechText);
            switch (command) {
                case 0:
                    retVal = 0;
                    break;
                case 1:
                    mCommand = Constants.OUTGOING_CALL;
                    retVal = 1;
                    break;
                case 2:
                    mCommand = Constants.SEND_MSG;
                    retVal = 2;
                    break;
                case 3:
                    mCommand = Constants.INCOMING_MSG;
                    retVal = 3;
                    break;
                case 4:
                    mCommand = Constants.DISCONNECT_RECEIVED_CALL;
                    retVal = 4;
                    break;
            }
        }

        return retVal;
    }

    private int getCommand(String speechText) {
        String[] hotWords = {Constants.HOTWORD + " call", Constants.HOTWORD2 + " call"};
        String[] sendMessageHotWords = {Constants.HOTWORD + " send message", Constants.HOTWORD2 + " send message"};
        String[] disconnectCallHotWords = {Constants.HOTWORD + " disconnect call", Constants.HOTWORD2 + " disconnect call"};

        int command = 0;

        String speech = speechText.toLowerCase();
        if (speech.startsWith(Constants.HOTWORD) || speech.startsWith(Constants.HOTWORD2)) {
            // To check for outgoing message as well..
            if (!speechText.equals(null)) {

                for (int i = 0; i < hotWords.length; i++) {
                    if (speech.contains(hotWords[i])) {
                        command = 1;
                        break;
                    }
                }

                for (int i = 0; i < sendMessageHotWords.length; i++) {
                    if (speech.contains(sendMessageHotWords[i])) {
                        command = 2;
                        break;
                    }
                }


                for (int i = 0; i < disconnectCallHotWords.length; i++) {
                    if (speech.contains(disconnectCallHotWords[i])) {
                        command = 4;
                        break;
                    }
                }
            }
        }
        return command;
    }

    /**
     * Method to process commands for specified speech Text
     *
     * @param speechText
     */
    private void processCommand(String speechText) {
        if (speechText.equalsIgnoreCase(Constants.HOTWORD + " error -") || speechText.equalsIgnoreCase("error -")) {
            processTextToSpeech(ConstantErrorMessages.NOT_DETECT_INPUT);
            mCommand = "";
            return;
        }
        /*Boolean isConnected = butlerServerSocket.isConnected();*/ //New comment

        if (mCommand != null && !mCommand.isEmpty()) {
            switch (mCommand) {
                case Constants.INCOMING_CALL:
                    logger.info("INCOMING CALL command value: " + Constants.INCOMING_CALL);
                    if (!speechText.equals(null)) {
                        mCallInterceptor.processSpeechMessage(speechText, mCommand);
                    }
                    break;

                case Constants.DISCONNECT_RECEIVED_CALL:
                    logger.info("Disconnect Received CALL command value: " + Constants.DISCONNECT_RECEIVED_CALL);
                    if (!speechText.equals(null)) {
                        JSONModel model = new JSONModel();
                        model.setCommandName(Constants.DISCONNECT_CALL);
                        model.setResult(speechText);
                        setCommand("");
                    }
                    break;

                case Constants.INCOMING_MSG:
                    logger.info("Process Message value: " + Constants.INCOMING_MSG);
                    if (!speechText.equals(null)) {
                        mMessageInterceptor.processSpeechMessage(speechText);
                    }
                    break;

                case Constants.INCOMING_MSG_REPLY:
                {
                    logger.info("Processing the reply message");
                    if( !speechText.equals(null)){
                        mMessageInterceptor.processReplyMessage(speechText);
                    }
                    break;
                }

                case Constants.OUTGOING_CALL:
                    logger.info("OUTGOING CALL command value: " + Constants.OUTGOING_CALL);
                    if (mOutgoingCall == null) {
                        mOutgoingCall = new OutgoingcallHelper(this);
                    }
                    mOutgoingCall.processSpeechMessage(speechText, mCommand);
                    break;

                case Constants.SEND_MSG:
                    logger.info("OUTGOING MESSAGE command value: " + Constants.SEND_MSG);

                    if (mSendMessage == null) {
                        mSendMessage = new BuddyMessageSend(this);
                    }
                    mSendMessage.processSMSSpeech(speechText.trim(), mCommand);
                    break;
            }
        } else if (speechText.startsWith(Constants.HOTWORD)) {
            processTextToSpeech(ConstantErrorMessages.INVALID_COMMAND);
        }
    }

    /**
     * Method to set the agent command
     *
     * @param command
     */
    public void setCommand(String command) {
        mCommand = command;
    }

    public String getCommand() {
        return mCommand;
    }

    public void startTimer() {

        //set a new Timer
        mTimer = new Timer();
        initializeTimerTask();
        mTimer.schedule(mTimerTask, 60000, 60000);
    }

    public void stoptimertask(View v) {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void initializeTimerTask() {

        mTimerTask = new TimerTask() {
            public void run() {
                mainThreadHandler.post(new Runnable() {
                    public void run() {
                        processTextToSpeech("");
                    }
                });
            }
        };
    }

    public BuddyMessageSend getSendMessage()
    {
        if(mSendMessage == null)
            mSendMessage = new BuddyMessageSend(this);

        return mSendMessage;
    }


}
