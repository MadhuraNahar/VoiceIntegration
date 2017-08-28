package com.test.voice.utils;


/**
 * Created by Madhura Nahar. Copyrights reserved.
 */
public class Constants {

    public static final String HOTWORD = "hey buddy";
    public static final String HOTWORD2 = "okay buddy";

    public static String BROADCAST_SPEECH_COMPLETED = "com.voice.TextToSpeech";
    public static String UTTERANCEID = "buddyutteranceId";

    // Constants meant for Handler callbacks
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int BUDDY_SPEECH_RECEIVE = 3;
    public static final int BUDDY_SPEECH_ACTIVITY_DATA = 4;
    public static final int BUDDY_SPEECH_RESTART = 5;

    public static final String USER_MESSAGE = "userMessageString";
    public static final String USER_SPEECH_DATA = "speechRecognitionText";


    public static final String INCOMING_MSG = "0x102";
    public static final String INCOMING_MSG_REPLY = "0x115";
    public static final String SEND_MSG = "0x103";
    public static final String INCOMING_CALL = "0x104";
    public static final String DISCONNECT_RECEIVED_CALL = "0x106";

    public static final String OUTGOING_CALL = "0x105";

    public static final String DISCONNECT_CALL = "0x108";
    public static final String CONTACTS_ADDED = "0x109";

    public static final String OUTGOING_CALL_ERROR = "0x113";

    // Outgoing call options..

    public static final String mobile = "Mobile";
    public static final String work = "Work";
    public static final String other = "Other";
    public static final String home = "Home";
    public static final String workmobile = "Workmobile";

    public static Boolean BOOL_WELCOME_MESG = false;
}
