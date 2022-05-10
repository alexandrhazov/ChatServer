package com.javarush.task.task30.task3008.client;

import java.util.HashSet;
import java.util.Set;

public class ClientGuiModel {
    private final Set<String> allUserNames = new HashSet<>();
    private String newMessage;

    public Set<String> getAllUserNames() {
        return allUserNames;
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public void addUser(String newUserName) {
        getAllUserNames().add(newUserName);
    }

    public void deleteUser(String userName) {
        getAllUserNames().remove(userName);
    }
}
