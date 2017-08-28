package com.test.voice.contacts;

/**
 * Created by Madhura Nahar.
 */

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

import com.test.voice.MainActivity;
import com.test.voice.model.Contactsmodel;
import com.test.voice.utils.Logutil;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermediate between ContactsDatabaseHandler and other classes querying database.
 */
public class ContactsReader {

    private Context mContext;
    private List<Contactsmodel> mPhoneContactList = new ArrayList<Contactsmodel>();
    private ContactsDatabaseHandler mContactsHandler;

    private static Logutil logger;
    private Cursor cur;


    public ContactsReader(Context context) {
        mContext = context;
        logger = Logutil.getInstance();
        mContactsHandler = new ContactsDatabaseHandler(mContext);
    }

    public synchronized ArrayList<Contactsmodel> getContactsFromAddressBook() {

        ArrayList<Contactsmodel> phones = new ArrayList<Contactsmodel>();

        Boolean cancel = false;
        if(cur != null && !cur.isClosed())
        {
            cancel = true;
            cur.close();
        }

        cur = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        int contactsCount = cur.getCount();
        if (contactsCount > 0) {

            while (cur.moveToNext()) {
                try {

                    Contactsmodel myContacts = new Contactsmodel();
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    myContacts.setID(id);
                    String conName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    myContacts.setName(conName);

                    if(cancel == true)
                        return null;

                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        // get the phone number

                        Cursor pCur = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            int phType = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                            switch (phType) {
                                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
//                                logger.info(conName + ": TYPE_MOBILE :" + phone);
                                    myContacts.setMobile_num(phone);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
//                                logger.info(conName + ": TYPE_HOME: "+ phone);
                                    myContacts.setHome_num(phone);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
//                                logger.info(conName + ": TYPE_WORK: " + phone);
                                    myContacts.setWork_num(phone);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
//                                logger.info(conName + ": TYPE_WORK_MOBILE: " + phone);
                                    myContacts.setWorkMobile_num(phone);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
//                                logger.info(conName + ": TYPE_OTHER: " + phone);
                                    myContacts.setOther_num(phone);
                                    break;
                                case 0:
//                                logger.info(conName + ": TYPE_SIM :" + phone);
                                    myContacts.setMobile_num(phone);

                                default:
                                    break;
                            }
                        }
                        pCur.close();

                        //get Contact name
                        String contactName = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                        String[] contactParams = new String[]{id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
                        Cursor nameCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                                null, contactName, contactParams, null);
                        while (nameCur.moveToNext()) {
                            String firstName = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                            String lastName = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                            myContacts.setFirstName(firstName);
                            myContacts.setLastName(lastName);
                        }
                        nameCur.close();
                        phones.add(myContacts);
                    }
                } catch (Exception e) {

                    if(cancel == true)
                        return null;

                    logger.info("ContactReader Exception " + e.getMessage());
                }
            }
            cur.close();
        }
        return phones;
    }

    /**
     * Add contacts to database from specified list.
     *
     * @param contactsList list of contacts to add.
     * @return
     */
    public boolean addContactsToDB(final List<Contactsmodel> contactsList) {
        if (contactsList != null && contactsList.size() > 0) {
            mPhoneContactList = contactsList;

            mContactsHandler.deleteAllContacts();
            mContactsHandler.addContacts(contactsList);

//            mAsyncContacts.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

           /* if(addBookThread != null)
                addBookThread.interrupt();

//            synchronized ()
            addBookThread = new Thread(new AddressBookThread(), "AddressBookThread");
            addBookThread.start();*/    //new comment

            logger.info("contacts added successfully..");
            return true;
        }
        return false;
    }

    public void deleteAllContacts()
    {
        mContactsHandler.deleteAllContacts();
        mPhoneContactList.clear();
    }

    /**
     * gets all contacts from Contacts database.
     *
     * @return list of all contacts
     */
    public List<Contactsmodel> getContacts() {
        List<Contactsmodel> contacts = new ArrayList<Contactsmodel>();
        contacts = mContactsHandler.getAllContacts();
        return contacts;
    }

    /**
     * Returns name from database for specified number.
     *
     * @param number
     * @return
     */
    public String getContactName(String number) {
        String name = null;
        name = mContactsHandler.getName(number.trim());
        return name;
    }


    public List<Contactsmodel> getContactsWithName(String contactName) {
        if (mPhoneContactList != null || mPhoneContactList.size() != 0)
        {
            mPhoneContactList.clear();
        }

        mPhoneContactList = getContacts();

        if (mPhoneContactList != null && mPhoneContactList.size() > 0) {
            List<Contactsmodel> nameList = new ArrayList<Contactsmodel>();
            List<Contactsmodel> modellist =new ArrayList<>();
            getContactListByName(contactName, modellist);
            if (modellist.size() > 0) {
                nameList.addAll(modellist);
                return nameList;
            }


            for (int i = 0; i < mPhoneContactList.size(); i++) {
                Contactsmodel model = mPhoneContactList.get(i);

                if (model != null) {
                    if (contactName.equalsIgnoreCase(model.getFirstName()) ||
                            contactName.equalsIgnoreCase(model.getLastName()) ||
                            contactName.equalsIgnoreCase(model.getName())) {
                        nameList.add(model);
                    }
                    }
                }

            if (nameList.size()<=0)
            {
                for (int i = 0; i < mPhoneContactList.size(); i++) {
                    Contactsmodel model = mPhoneContactList.get(i);

                    if (model != null) {
                        if (soundex(contactName).equalsIgnoreCase(soundex(model.getFirstName())) ||
                                soundex(contactName).equalsIgnoreCase(soundex(model.getLastName())) ||
                                soundex(contactName).equalsIgnoreCase(soundex(model.getName()))) {
                            model.setSoundex(true);
                            nameList.add(model);
                        }
                    }
                }
            }

            if (nameList.size() > 0) {
                return nameList;
            }

        }
        return null;
    }

    public List<Contactsmodel> getContactListByName_Soundex(String contactName, ArrayList<Contactsmodel> listAllContacts, List<Contactsmodel> models) {
        List<Contactsmodel> resultList = new ArrayList<>();
        for (Contactsmodel conmod : listAllContacts) {

            //if (contactName.contains(" "))
            {
                resultList = GetMatchingContactList(contactName, conmod);

                if (resultList==null)
                    continue;

                if(resultList.size() > 0)
                    break;
            }
        }
        models.addAll(resultList);
        return resultList;
    }

    private List<Contactsmodel> GetMatchingContactList(String contactName, Contactsmodel conmod) {
        String[] arr_splitname=contactName.split(" ");
        ArrayList<Contactsmodel> resultList=new ArrayList<>();

        Boolean hasSpace;
        if (contactName.contains(" "))
            hasSpace=true;
        else
            hasSpace=false;

        if(compareNames(arr_splitname, conmod.getName(),hasSpace) == false)
            return null;


        String[] arr_contactName = conmod.getName().split(" ");
        Boolean bSame = false;

        for (int i=0;i<arr_splitname.length;i++)
        {
            try {
                if (soundex(arr_splitname[i]).equalsIgnoreCase(soundex(arr_contactName[i])))
                {
                    bSame = true;
                }
                else
                {
                    bSame = false;
                    break;
                }
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if(bSame == true)
            resultList.add(conmod);

        return resultList;

    }

    Boolean compareNames(String[] spokenWord, String contactName, boolean hasSpace)
    {
        if(spokenWord == null && spokenWord.length == 0)
            return false;

        if(contactName.isEmpty())
            return false;

        if (hasSpace)
        {
            String[] contactNameSplit = contactName.split(" ");
            if(contactNameSplit.length != spokenWord.length)
                return false;
        }

        return true;
    }

    public List<Contactsmodel> getContactListByName(String contactName, List<Contactsmodel> models) {
        models.addAll(mContactsHandler.getContactByName(contactName));
        return mContactsHandler.getContactByName(contactName);
    }

    public Contactsmodel getContactNumbersForName(String contactName, List<Contactsmodel> contactsList) {
        if (contactsList == null || contactsList.size() == 0)
            return null;

        if (contactsList != null && contactsList.size() > 0) {
            for (int i = 0; i < contactsList.size(); i++) {
                Contactsmodel model = contactsList.get(i);

                if (model != null) {
                    if (contactName.equalsIgnoreCase(model.getFirstName()) &&
                            contactName.equalsIgnoreCase(model.getLastName()) ||
                            contactName.equalsIgnoreCase(model.getName())) {
                        return model;
                    }
                    else if (soundex(contactName).equalsIgnoreCase(soundex(model.getFirstName())) &&
                            soundex(contactName).equalsIgnoreCase(soundex(model.getLastName())) ||
                            soundex(contactName).equalsIgnoreCase(soundex(model.getName()))) {
                        List<Contactsmodel> resultList = GetMatchingContactList(contactName, model);

                    if (resultList !=null && resultList.size()>0)
                        return resultList.get(0);

                    }
                }
            }
            return null;
        }
        return null;
    }


    public int getContactCount()
    {
        return mContactsHandler.getContactsCount();
    }

    public static String soundex(String s) {
      try {
          char[] x = s.toUpperCase().toCharArray();
          char firstLetter = x[0];

          // convert letters to numeric code
          for (int i = 0; i < x.length; i++) {
              switch (x[i]) {

                  case 'B':
                  case 'F':
                  case 'P':
                  case 'V':
                      x[i] = '1';
                      break;

                  case 'C':
                  case 'G':
                  case 'J':
                  case 'K':
                  case 'Q':
                  case 'S':
                  case 'X':
                  case 'Z':
                      x[i] = '2';
                      break;

                  case 'D':
                  case 'T':
                      x[i] = '3';
                      break;

                  case 'L':
                      x[i] = '4';
                      break;

                  case 'M':
                  case 'N':
                      x[i] = '5';
                      break;

                  case 'R':
                      x[i] = '6';
                      break;

                  default:
                      x[i] = '0';
                      break;
              }
          }

          // remove duplicates
          String output = "" + firstLetter;
          for (int i = 1; i < x.length; i++)
              if (x[i] != x[i - 1] && x[i] != '0')
                  output += x[i];

          // pad with 0's or truncate
          output = output + "0000";
          return output.substring(0, 4);
      }catch (Exception ex)
      {
          logger.info("Soundex Exception - "+ex.getMessage());
      }

        return "";
    }
}
