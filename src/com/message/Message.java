package com.message;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 0x0505;
    private String stringMessage;
    private double time = 0;

    public void setStringMessage(String stringMessage) {
        this.stringMessage = stringMessage;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public String getStringMessage() {
        return stringMessage;
    }

    public double getTime() {
        return time;
    }
}
