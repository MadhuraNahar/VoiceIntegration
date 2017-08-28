package com.test.voice.receivers;

/**
 * Created by Madhura Nahar.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.test.voice.MainActivity;

public class BuddyRebootLoader extends BroadcastReceiver {
    public BuddyRebootLoader() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("RebootLoader", "Staring the main activity");
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

    }
}
