package com.test.voice.jsonutils;

/**
 * Created by Madhura Nahar.
 */
import com.google.gson.Gson;

/**
 * Create JSON format string from specific Class<T> object.
 */
public class JSONCreator {

    /**
     *  convert model object to JSON string.
     * @param obj to be converted to JSON
     * @return string in JSON format.
     */
    public static String createJSON(Object obj) {
        String jsonString = null;
        Gson gson = new Gson();
        jsonString = gson.toJson(obj);
        return jsonString;
    }
}
