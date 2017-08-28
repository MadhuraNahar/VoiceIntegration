package com.test.voice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.test.voice.services.VoiceService;
import com.test.voice.utils.Logutil;
import com.test.voice.utils.PermissionHandler;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView mListUserMessages;
    private ArrayList<String> mArrListUserMessages;
    private ArrayAdapter<String> msgsAdapter;
    public static String BROADCAST_USER_SPEECH = "com.buttler.receiver.userSpeech";
    private IntentFilter inf;
    private Intent intentService, mIntentPolling, mInteractiveService;
    int titleList = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initializeView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void initializeView() {
//        crashButton = (Button) findViewById(R.id.button);
        mListUserMessages = (ListView) findViewById(R.id.listScanResults);
        mArrListUserMessages = new ArrayList<String>();
        msgsAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, mArrListUserMessages);
        mListUserMessages.setAdapter(msgsAdapter);


        if(Build.VERSION.SDK_INT >= 23)
        {
            Boolean bPermission = PermissionHandler.hasPermissions(this);
            if(bPermission == true){
                if(! isMyServiceRunning(VoiceService.class)){
                    Log.i("MainActivity", "Starting the service");
                    intentService = new Intent(MainActivity.this, VoiceService.class);
                    startService(intentService);
                }
            }
            else {
                PermissionHandler.checkPermission(this);
            }
        }
        else
        {
            if(! isMyServiceRunning(VoiceService.class)){
                intentService = new Intent(MainActivity.this, VoiceService.class);
                startService(intentService);
            }
        }

        inf = new IntentFilter();
        inf.addAction(BROADCAST_USER_SPEECH);
        this.registerReceiver(speechReceiver, inf);

        Log.i("MainActivity", "In End of Initialize view");
    }

    /**
     * Checks if the specified service is running or not
     * @param serviceClass name of the Service class
     * @return true if service is running, false otherwise.
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public BroadcastReceiver speechReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(MainActivity.BROADCAST_USER_SPEECH)) {

                    getSupportActionBar().setTitle(getString(R.string.app_name));
                    mArrListUserMessages.add(intent.getStringExtra("UserSpeechData"));
                    msgsAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        if(Build.VERSION.SDK_INT >= 23) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("com.voice.activity.stop");
            sendBroadcast(broadcastIntent);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionHandler.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                for(int i = 0;i< grantResults.length;i++){
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                        if(! isMyServiceRunning(VoiceService.class)){
                            intentService = new Intent(MainActivity.this, VoiceService.class);
                            startService(intentService);
                        }
                    } else {
                        Toast.makeText(MainActivity.this,"Permission Denied, You cannot start app.",Toast.LENGTH_LONG).show();
                    }
                }

                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(speechReceiver);
    }
}
