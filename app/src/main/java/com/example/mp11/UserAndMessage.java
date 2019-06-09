package com.example.mp11;
//классы для хранения информации о сообщении и его отправителе
public class UserAndMessage {
    Message message;
    User user;
    public User getSender(){
        return user;
    }
    public String getMessage(){
        return message.message;
    }

    class Message {
        String message;
        User sender;
        long createdAt;

        public String getmessage() {
            return message;
        }

        public User getSender() {
            return sender;
        }

        public void setText(String message) {
            this.message = message;
        }
    }

    class User {
        String nickname;
        String profileUrl;

        public String getNickname() {
            return nickname;
        }

        public String getProfileUrl() {
            return profileUrl;
        }
    }
}