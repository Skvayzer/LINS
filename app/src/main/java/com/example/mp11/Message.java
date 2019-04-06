package com.example.mp11;

public class Message {
    private String text; // message body
    private MemberData memberData; // data of the user that sent this message
    private boolean belongsToCurrentUser; // is this message sent by us?
    public String date;

    public Message(String text, MemberData memberData, boolean belongsToCurrentUser, String date) {
        this.text = text;
        this.memberData = memberData;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.date=date;
    }

    public String getText() {
        return text;
    }

    public MemberData getMemberData() {
        return memberData;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

}
class MemberData {
    private String name;
    private String color;

    public MemberData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    // Add an empty constructor so we can later parse JSON into MemberData using Jackson
    public MemberData() {
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}