package com.test.voice.contacts;

/**
 * Created by Madhura Nahar.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.test.voice.model.Contactsmodel;
import com.test.voice.utils.Logutil;
import java.util.ArrayList;
import java.util.List;

/**
 * Database handler.
 */
public class ContactsDatabaseHandler {


    Logutil logger = Logutil.getInstance();

    public ContactsDatabaseHandler(Context context) {
    }

    /**
     * add contacts to database
     * @param contacts
     */
    void addContacts(List<Contactsmodel> contacts)
    {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        try{
            db.beginTransaction();

            String sql = "Insert or Replace into " + DatabaseManager.TABLE_CONTACTS+"(" + DatabaseManager.KEY_ID
                    + "," + DatabaseManager.KEY_NAME
                    + "," + DatabaseManager.KEY_FNAME
                    + "," + DatabaseManager.KEY_LNAME
                    + "," + DatabaseManager.KEY_PH_TYPE_MOBILE
                    + "," + DatabaseManager.KEY_PH_TYPE_HOME
                    + "," + DatabaseManager.KEY_PH_TYPE_WORK
                    + "," + DatabaseManager.KEY_PH_TYPE_WORK_MOBILE
                    + "," + DatabaseManager.KEY_PH_TYPE_OTHER
                    + ") values(?,?,?,?,?,?,?,?,?)";
            SQLiteStatement insert = db.compileStatement(sql);

            for(int i=0;i<contacts.size();i++){
                Contactsmodel contact = contacts.get(i);
                try {
                    insert.bindString(1, contacts.get(i).getID());
                    insert.bindString(2, contacts.get(i).getName());
                    insert.bindString(3, contacts.get(i).getFirstName());
                    insert.bindString(4, contacts.get(i).getLastName());
                    insert.bindString(5, contacts.get(i).getMobile_num());
                    insert.bindString(6, contacts.get(i).getHome_num());
                    insert.bindString(7, contacts.get(i).getWork_num());
                    insert.bindString(8, contacts.get(i).getWorkMobile_num());
                    insert.bindString(9, contacts.get(i).getOther_num());
                    insert.execute();
                } catch (Exception ex) {
                    logger.exception(ex);
                }
            }
            db.setTransactionSuccessful();
        }
        catch (Exception e) {
            logger.exception(e);
        }
        finally {
            db.endTransaction();
        }
    }

    /**
     * Method for deleting all rows in the database table.
     */
    public void deleteAllContacts()
    {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(DatabaseManager.TABLE_CONTACTS, null, null);
        DatabaseManager.getInstance().closeDatabase();
    }

    // code to get the single contact
    public Contactsmodel getContact(int id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        Cursor cursor = db.query(DatabaseManager.TABLE_CONTACTS, new String[]{DatabaseManager.KEY_ID,
                        DatabaseManager.KEY_NAME,
                        DatabaseManager.KEY_FNAME,
                        DatabaseManager.KEY_LNAME,
                        DatabaseManager.KEY_PH_TYPE_MOBILE,
                        DatabaseManager.KEY_PH_TYPE_HOME,
                        DatabaseManager.KEY_PH_TYPE_WORK,
                        DatabaseManager.KEY_PH_TYPE_WORK_MOBILE,
                        DatabaseManager.KEY_PH_TYPE_OTHER},
                DatabaseManager.KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Contactsmodel contact = new Contactsmodel(cursor.getString(0),cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4), cursor.getString(5),
                cursor.getString(6), cursor.getString(7), cursor.getString(8));

        return contact;
    }

    /**
     * get all contacts from contactsTable
     */

    public List<Contactsmodel> getAllContacts() {
        List<Contactsmodel> contactList = new ArrayList<Contactsmodel>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DatabaseManager.TABLE_CONTACTS;

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contactsmodel contact = new Contactsmodel();
                contact.setID(cursor.getString(0));
                contact.setName(cursor.getString(1));
                contact.setFirstName(cursor.getString(2));
                contact.setLastName(cursor.getString(3));
                contact.setMobile_num(cursor.getString(4));
                contact.setHome_num(cursor.getString(5));
                contact.setWork_num(cursor.getString(6));
                contact.setWorkMobile_num(cursor.getString(7));
                contact.setOther_num(cursor.getString(8));

                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        // return contact list
        return contactList;
    }

    /**
     * Method to update the single contact
     * @param contact
     */
    public int updateContact(Contactsmodel contact) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseManager.KEY_NAME, contact.getName());
//        values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        return db.update(DatabaseManager.TABLE_CONTACTS, values, DatabaseManager.KEY_ID + " = ?",new String[] { String.valueOf(contact.getID()) });
    }

    /**
     *  Delete single contact
     * @param contact
     */
    public void deleteContact(Contactsmodel contact) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(DatabaseManager.TABLE_CONTACTS, DatabaseManager.KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
        DatabaseManager.getInstance().closeDatabase();
    }

    /**
     * Get contacts Count
     * @return count of total contacts
     */
    public int getContactsCount() {

        int nCount = 0;
        String countQuery = "SELECT  * FROM " + DatabaseManager.TABLE_CONTACTS;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        nCount = cursor.getCount();
        cursor.close();

        return nCount;
    }

    /**
     * Find name from database for specified number.
     * @param contactNumber phone number
     * @return name associated with contactNumber
     */
    public String getName(String contactNumber) {
        String contactName = null;

//        String sqlQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " +KEY_PH_TYPE_HOME + " = "+contactNumber +
//                " OR " +KEY_PH_TYPE_MOBILE + " = "+contactNumber +
//                " OR " +KEY_PH_TYPE_OTHER + " = "+contactNumber +
//                " OR " +KEY_PH_TYPE_WORK + " = "+contactNumber +
//                " OR " +KEY_PH_TYPE_WORK_MOBILE + " = "+contactNumber + ";" ;

        String sqlQuery = "SELECT * FROM " + DatabaseManager.TABLE_CONTACTS;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, null);

        if (cursor != null) {
            if (!cursor.isFirst())
                cursor.moveToFirst();
            do {
                try {
                    if (cursor.getString(4).equals(contactNumber) ||
                            cursor.getString(5).equals(contactNumber) ||
                            cursor.getString(6).equals(contactNumber) ||
                            cursor.getString(7).equals(contactNumber) ||
                            cursor.getString(8).equals(contactNumber)) {
                        contactName = cursor.getString(1);

                    }
                } catch (Exception e) {
                    logger.exception(e);
                }
            } while (cursor.moveToNext());
        }

        if (contactName == null) {
            return null;
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();

        return contactName;
    }

    /**
     * Find number from database for specified contactName.
     * @param contactName phone number
     * @return number associated with contactName
     */
    public String getContactNumber(String contactName){
        String contactNumber = null;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
//       Cursor cursor =  db.query(TABLE_CONTACTS,null,KEY_NAME + "=?",new String[]{contactName} ,null,null,null);
        String sqlQuery = "SELECT * FROM " + DatabaseManager.TABLE_CONTACTS;
        Cursor cursor = db.rawQuery(sqlQuery, null);

        if(cursor != null && cursor.moveToFirst()){
            do {
                try {

                }catch (Exception e){
                    logger.exception(e);
                }
                if(cursor.getString(1).equals(contactName)){
                    if( !cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_HOME)).equals("null")){
                        contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_HOME));
                    }
                    else if( !cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_MOBILE)).equals("null")){
                        contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_MOBILE));
                    }
                    else if( !cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_OTHER)).equals("null")){
                        contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_OTHER));
                    }
                    else if( !cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_WORK)).equals("null")){
                        contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_WORK));
                    }
                    else if( !cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_WORK_MOBILE)).equals("null")){
                        contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.KEY_PH_TYPE_WORK_MOBILE));
                    }
                }


            }while (cursor.moveToNext());
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();

        return contactNumber.trim();


    }

    public List<Contactsmodel> getContactByName(String contactName) {
        String sqlQuery = "SELECT * FROM " + DatabaseManager.TABLE_CONTACTS + " where " + DatabaseManager.KEY_NAME + " like '% " + contactName + "' or " + DatabaseManager.KEY_NAME + " like '" + contactName + " %' or " + DatabaseManager.KEY_NAME + " like '% " + contactName + " %' or "+ DatabaseManager.KEY_NAME+"='"+contactName+"';";
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, null);
        List<Contactsmodel> listmodels = new ArrayList<>();
        if (cursor != null) {
            if (!cursor.isFirst())
                cursor.moveToFirst();
            do {
                try {
                    Contactsmodel contact = new Contactsmodel(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5),
                            cursor.getString(6), cursor.getString(7), cursor.getString(8));

                    listmodels.add(contact);

                } catch (Exception e) {
                    logger.exception(e);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        DatabaseManager.getInstance().closeDatabase();

        return listmodels;
    }
}
