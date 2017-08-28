package com.test.voice.utils;

/**
 * Created by Madhura Nahar. Copyrights reserved.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle application specific permission for Build.VERSION_CODES.M(api 23) or above
 */
public class PermissionHandler {

    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private Context mContext;
    static String[] permissions= new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_SMS
            };

    private static Logutil logger = Logutil.getInstance();

    public static void checkPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        } else {

            if (checkPermissions(activity, permissions)) {
                logger.info(" Permission granted");

            }
        }
    }

    /**
     *  check if the particular permission is granted or not
     * @param activity activity in which permission is to be checked
     * @param permissions name of the permission to check
     * @return true if granted, false otherwise
     */
    private  static boolean checkPermissions(Activity activity,String[] permissions) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(activity,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    /**
     * check if permission specified context has permission or not.
     * @param context
     * @return
     */
    public static boolean hasPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
