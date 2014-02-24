package org.javacint.sms;

public class Message {

    private final String phone, content;

    public Message(String phone, String content) {
        this.phone = phone;
        this.content = content;
    }

    public String getPhone() {
        return phone;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        return "Message{phone=" + phone + "/" + content + "}";
    }
}
