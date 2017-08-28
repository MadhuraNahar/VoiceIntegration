package com.test.voice.utils;

import android.util.Log;

/**
 * Created by Madhura Nahar. Copyrights reserved.
 */
public class TextUtils {

    public final static String OUTGOINGCALLMSG_NAMES_FOUND_COUNT = "Buddy has identified %d names ";
    public final static String OUTGOINGCALLMSG_SYNONYMOUS_NAMES_FOUND = "Buddy has identified name %s for %s. Do you want to call %s?";
    public final static String OUTGOINGCALLMSG_SYNONYMOUS_MULTIPLE_NAMES_FOUND = "Buddy has identified %s synonymous names for %s";
    public final static String OUTGOINGCALLMSG_NOS_FOUND_COUNT = "Buddy has identified %s numbers for name %s ";
    public static final String CALLING_NUMBER_SELECT = "Please let us know which %s to call";
    public static final String SYN_CALLING_NUMBER_SELECT = " Please choose appropriate name to call.";
    public static final String CALLING_TYPE_SELECT = "Please let us know which to call";
    public static final String NAME_NOT_FOUND_ERROR = "Buddy could not find  %s. Please try again later";
    public static final String NUM_NOT_FOUND_ERROR = "Buddy could not find phone number for %s.";
    public static final String EMPTY_PHONE_NUM = "Buddy could not find provided phone option. Please try again later";
    public static final String ASK_USER_TASK = "What you would like buddy to do ?";

    public static final String MSGING_NUMBER_SELECT = "Please let us know which %s to send message.";
    public static final String SPEAK_MESSAGE = "Speak your message.";
    public static final String MSGING_NUMBER_TYPE_SELECT = "Please let us know which number to message";

    public static final String USER_MESSAGE_CONFIRM = "Your message is %s .    Do you want to send message ?";
    public static final String CALLING_CONFIRMATION = "Dialing to %s";
    public static final String CALL_FAILURE = "Your call to %s could not be completed. Please check.";



    public static String stringFormatter(String content, String... arguments)
    {
        return String.format(content, arguments);
    }

    public static String stringFormatter(String content, Integer... arguments)
    {
        String t = "";
        try {
            t = String.format(content, arguments);
        }
        catch (Exception e)
        {
            Log.e("Buddy", e.getMessage());
            e.printStackTrace();
        }

        return t;
    }
}
