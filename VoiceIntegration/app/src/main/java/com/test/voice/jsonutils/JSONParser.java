package com.test.voice.jsonutils;

/**
 * Created by Madhura Nahar.
 */

import com.google.gson.Gson;
import com.test.voice.model.Contactsmodel;
import com.test.voice.model.JSONCallModel;
import com.test.voice.model.JSONMessageModel;
import com.test.voice.model.JSONModel;


/**
 * Parse JSON format string to specific Class<T> object
 */
public class JSONParser {

    /**
     * Parse JSON as string to JSONModel Object
     * @param obj json string to parse.
     * @return parsed JSONModel object
     */
    public static Object parseJSON(Object obj) {
        JSONModel jsonObj = new JSONModel();
        Gson gson = new Gson();
        jsonObj = gson.fromJson(obj.toString(),JSONModel.class);
        return jsonObj;
    }

    /**
     * Parse JSON as string to Contactsmodel object
     * @param obj JSON string to parse
     * @return parsed Contactsmodel object
     */
    public static Contactsmodel parseContactJSON(Object obj){
        Contactsmodel contactModel = new Contactsmodel();
        Gson gson = new Gson();
        contactModel = gson.fromJson(obj.toString(),Contactsmodel.class);
        return contactModel;
    }

    /**
     * Parse JSON as string to JSONMessageModel object
     * @param obj JSON string to parse
     * @return parsed JSONMessageModel object
     */
    public static JSONMessageModel parseMessageJSON(Object obj) {
        JSONMessageModel jsonObj = new JSONMessageModel();
        Gson gson = new Gson();
        jsonObj = gson.fromJson(obj.toString(),JSONMessageModel.class);
        return jsonObj;
    }

    public static JSONCallModel parseCallJSON(Object obj) {
        JSONCallModel jsonObj = new JSONCallModel();
        Gson gson = new Gson();
        jsonObj = gson.fromJson(obj.toString(),JSONCallModel.class);
        return jsonObj;
    }

}
