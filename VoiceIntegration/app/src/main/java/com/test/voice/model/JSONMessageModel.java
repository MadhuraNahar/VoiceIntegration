package com.test.voice.model;

/**
 * Created by Madhura Nahar.
 */

public class JSONMessageModel {

    private String commandName;
    private MessageModel result;

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public MessageModel getResult() {
        return result;
    }

    public void setResult(MessageModel result) {
        this.result = result;
    }
}
