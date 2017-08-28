package com.test.voice.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by DELL on 8/25/2017.
 */
public class PhoneHelper {

    /**
     * Check if the specified string is numeric
     * @param s string to check.
     * @return true- if string is numeric,false otherwise.
     */
    public static boolean isNumber(String s) {
        boolean numberFlag = false;
        try {
            String regexStr = "^[0-9]*$";
            s = s.trim();
            if(s.matches(regexStr)){
                numberFlag = true;
            }
            else {
                numberFlag = false;
            }
            System.out.println("Number Flag is: " +numberFlag);
        }
        catch (Exception e ){
            System.out.println(e);
        }
        return numberFlag;
    }

    public static boolean isSimSupport(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);

    }


}
