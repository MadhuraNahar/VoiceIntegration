package com.test.voice;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.test.voice.contacts.DBHelper;
import com.test.voice.contacts.DatabaseManager;
import com.test.voice.services.VoiceService;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Madhura Nahar.
 */
public class VoiceApplication extends Application {

    private static DBHelper dbHelper;
    private static Context mContext;

    private Thread.UncaughtExceptionHandler defaultUEH;

    @Override
    public void onCreate() {
        super.onCreate();

        dbHelper = new DBHelper(this.getApplicationContext());
        DatabaseManager.initializeInstance(dbHelper);
        mContext = getApplicationContext();

        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }

    public static Context getContext() {
        return mContext;
    }

    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(final Thread thread, final Throwable ex) {

                    final ScheduledThreadPoolExecutor c = new ScheduledThreadPoolExecutor(1);
                    c.schedule(new Runnable() {
                        @Override
                        public void run() {

                            defaultUEH.uncaughtException(thread, ex);

                        }
                    }, 3, TimeUnit.MILLISECONDS);


                    final ScheduledThreadPoolExecutor c1 = new ScheduledThreadPoolExecutor(1);
                    c1.schedule(new Runnable() {
                        @Override
                        public void run() {

                            int pid = android.os.Process.myPid();
                            Intent i = new Intent(getApplicationContext(), VoiceService.class);

                            startService(i);

                            android.os.Process.killProcess(pid);

                        }
                    }, 3, TimeUnit.SECONDS);


                }
            };
}
