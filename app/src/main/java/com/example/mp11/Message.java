package com.example.mp11;

public class Message {
    private String text; //тело сообщения
    private MemberData memberData; //информация опользователе, который отправил сообщение
    private boolean belongsToCurrentUser; //текущий ли пользователь отправил?
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
