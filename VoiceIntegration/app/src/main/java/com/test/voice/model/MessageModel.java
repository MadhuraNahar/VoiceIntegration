package com.test.voice.model;

/**
 * Created by Madhura Nahar.
 */
public class MessageModel {

    private String number = "";
    private String messageContent = "";
    String Name;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        if(number != null){
            this.number = number.trim();
        }

    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        if(messageContent != null){
            this.messageContent = messageContent.trim();
        }

    }
}
