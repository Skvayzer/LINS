package com.example.mp11.views;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CategDictionary {

    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
    static DatabaseReference myRef = database.getReference(userID);
    public CategDictionary(String name){
        myRef=myRef.child(name);
    }
    public void addWord(String word,ArrayList<StringTranslation> list){
        myRef.child(word).setValue(list);
    }
    public void getAllWords(){

    }
}
