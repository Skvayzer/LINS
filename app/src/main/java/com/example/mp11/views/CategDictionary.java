package com.example.mp11.views;

import android.content.Context;

import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.MyDatabase.WordModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CategDictionary {
    public String name;
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
    static DatabaseReference myRef = database.getReference(userID);
    static MyDbHelper databaseHelper;
    public CategDictionary(Context context, String name){
        myRef=myRef.child(name);
        databaseHelper=new MyDbHelper(context,name);
        this.name=name;
    }
    public void addWord(String word,ArrayList<StringTranslation> list){
        myRef.child(word).setValue(list);
        for(StringTranslation a: list){
            databaseHelper.addWord(word,a.meaning,a.syns,a.ex);
        }

    }


    public ArrayList<WordModel> getAllWords(){
        return databaseHelper.getAllWords();
    }
    public ArrayList<StringTranslation> getAllWords(int k){
        return databaseHelper.getAllWords(1);
    }
    public ArrayList<WordModel>  getWord(String word, int k){
        return databaseHelper.getWord(word,1);
    }
    public ArrayList<StringTranslation>  getWord(String word){
        return databaseHelper.getWord(word);
    }
    public void updateWord(String word){
        //databaseHelper.updateWord(););
    }
}
