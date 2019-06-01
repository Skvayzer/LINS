package com.example.mp11.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.MyDatabase.WordModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CategDictionary {
    public String name;
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference myRef = database.getReference(userID);
    MyDbHelper databaseHelper;
    public CategDictionary(Context context, String name){
        myRef=myRef.child(name);
        databaseHelper=new MyDbHelper(context,name);
        this.name=name;

    }

    public static void downloadDict(String user, final String dict, final Context context){
        DatabaseReference dictRef=database.getReference(user).child(dict);
        dictRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //GenericTypeIndicator<ArrayList<Dictionary>> t = new GenericTypeIndicator<ArrayList<Dictionary>>() {};
                ArrayList<StringTranslation> d=new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    for(DataSnapshot dsna: ds.getChildren()) d.add(dsna.getValue(StringTranslation.class));

                }
                Gson gson=new Gson();
                SharedPreferences preferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                String json=preferences.getString("dictionaries",null);
                final String[] names=gson.fromJson(json,String[].class);
                ArrayList<String> nameslist = new ArrayList<String>(Arrays.asList(names));
                String res_dict=dict;
                if(!nameslist.contains(dict)) nameslist.add(dict);
                else{
                    res_dict=dict+ " - copy";
                    while(true){

                        if(!nameslist.contains(res_dict)) break;
                        else res_dict+=" - copy";

                    }
                    nameslist.add(res_dict);
                }
                MyDbHelper dbhelp=new MyDbHelper(context,res_dict);
                for(StringTranslation tr: d) {
                    dbhelp.addWord(tr.word,tr.meaning,tr.syns,tr.ex);

                }


                json=gson.toJson(nameslist);
                editor.putString("dictionaries",json);
                editor.apply();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void addWord(String word,ArrayList<StringTranslation> list){
        myRef.child(word).setValue(list);
        for(StringTranslation a: list){
            databaseHelper.addWord(word,a.meaning,a.syns,a.ex);
        }

    }
    public void addWord(String word,ArrayList<StringTranslation> list,boolean b){

        for(StringTranslation a: list){
            databaseHelper.addWord(word,a.meaning,a.syns,a.ex);
        }

    }

    public void deleteWord(String word){
        databaseHelper.deleteWord(word);
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
