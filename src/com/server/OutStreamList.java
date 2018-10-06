package com.server;

import com.message.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

public class OutStreamList {
    private Map<String , ObjectOutputStream> list;

    public OutStreamList(Map<String, ObjectOutputStream> list) {
        this.list = list;
    }

    public synchronized void send(Message message, String name) {
        list.forEach((k, v) -> {
            try {
                if(!k.equalsIgnoreCase(name)) v.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized void send(Message message) {
        list.forEach((k, v) -> {
            try {
                v.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void remove(String name) {
        list.remove(name);
    }

    public void put(String name, ObjectOutputStream output) {
        list.put(name, output);
    }
}
