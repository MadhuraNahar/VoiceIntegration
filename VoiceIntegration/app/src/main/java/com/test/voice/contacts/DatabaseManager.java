package com.test.voice.contacts;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Madhura Nahar.
 */
public class DatabaseManager {

    private static DatabaseManager ourInstance = new DatabaseManager();

    private Integer mOpenCounter = 0;

    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static final String TABLE_CONTACTS = "contacts";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_FNAME = "F_name";
    public static final String KEY_LNAME = "L_name";
    public static final String KEY_PH_TYPE_MOBILE = "mobile_number";
    public static final String KEY_PH_TYPE_HOME = "home_number";
    public static final String KEY_PH_TYPE_WORK = "work_number";
    public static final String KEY_PH_TYPE_WORK_MOBILE = "work_mobile_number";
    public static final String KEY_PH_TYPE_OTHER = "other_number";

    private DatabaseManager() {
    }


    public static String createTable()
    {
        return "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_FNAME + " TEXT, "
                + KEY_LNAME + " TEXT,"
                + KEY_PH_TYPE_MOBILE + " TEXT,"
                + KEY_PH_TYPE_HOME + " TEXT,"
                + KEY_PH_TYPE_WORK + " TEXT,"
                + KEY_PH_TYPE_WORK_MOBILE + " TEXT,"
                + KEY_PH_TYPE_OTHER + " TEXT" + ")";
    }

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter+=1;
        if(mOpenCounter == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        mOpenCounter-=1;
        if(mOpenCounter == 0) {
            // Closing database
            mDatabase.close();

        }
    }

}
