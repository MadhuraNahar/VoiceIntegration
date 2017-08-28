package com.test.voice.call_engine;

/**
 * Created by Madhura Nahar.
 */
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.test.voice.utils.Logutil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


public class CallActions {

    TelephonyManager mTelephonyManager;
    Context mContext;
    Logutil logger = Logutil.getInstance();

    public CallActions(Context context) {
        this.mContext = context;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void disconnectCall() {

        try {
            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";

            Class telephonyClass;
            Class telephonyStubClass;
            Class serviceManagerClass;
            Class serviceManagerNativeClass;
            Class serviceManagerNativeStubClass;

            Method telephonyEndCall;
            Method telephonyAnswerCall;
            Method getDefault;

            Method[] temps;
            Constructor[] serviceManagerConstructor;

            // Method getService;
            Object telephonyObject;
            Object serviceManagerObject;

            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);

            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);

            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");

            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);

            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");

            telephonyEndCall.invoke(telephonyObject);

        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Call disconnected...");
    }

    public void answerCall() {

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            new Thread(new Runnable() {

                @Override
                public void run() {

                    try {

                        Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_HEADSETHOOK);
                        logger.info("Call recived for 5.0 & above");
                    } catch (Throwable t) {
                        logger.info(t.getMessage());

                    }

                }
            }).start();
        }
//        else
        {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        String enforcedPerm = "android.permission.CALL_PRIVILEGED";
                        Intent btnDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                                Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,
                                        KeyEvent.KEYCODE_HEADSETHOOK));
                        Intent btnUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                                Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                                        KeyEvent.KEYCODE_HEADSETHOOK));

                        mContext.sendOrderedBroadcast(btnDown, enforcedPerm);
                        mContext.sendOrderedBroadcast(btnUp, enforcedPerm);
                        logger.info("Call recived for 4.4 & below");

                    } catch (Exception e) {
                        // Runtime.exec(String) had an I/O problem, try to fall back
                    }
                }
            }).start();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mContext, AcceptCallActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    mContext.startActivity(intent);
                }
            }).start();
        }

    }

    public void callToNumber(String phone) {

        if (phone.isEmpty()) {
            logger.info("Could not call to provided phone number as the number is empty");
            return;
        }

        logger.info("Number found for contact is: " + phone);
        int callState = mTelephonyManager.getCallState();
        logger.info("Outgoing call state: " + callState);

        if(callState == TelephonyManager.CALL_STATE_IDLE){
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone.trim()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
            mContext.startActivity(intent);
        }
        else {
            logger.info("Cannot place new call");
        }
    }
}
