package com.test.voice.contacts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Madhura Nahar.
 */
public class DBHelper extends SQLiteOpenHelper{

    //version number to upgrade database version
    //each time if you Add, Edit table, you need to change the
    //version number.
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "BuddyVoice.db";
    private static final String TAG = DBHelper.class.getSimpleName().toString();

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //All necessary tables you like to create will create here
        sqLiteDatabase.execSQL(DatabaseManager.createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        Log.d(TAG, String.format("SQLiteDatabase.onUpgrade(%d -> %d)", oldVersion, newVersion));

        // Drop table if existed, all data will be gone!!!
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseManager.TABLE_CONTACTS);
        onCreate(sqLiteDatabase);

    }
}
