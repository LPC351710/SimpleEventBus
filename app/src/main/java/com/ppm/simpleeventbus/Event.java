package com.ppm.simpleeventbus;

public class Event {
    private String msg;

    public Event(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
