package com.example.mp11.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CategDictionary {
    public String name,description;
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();

    DatabaseReference myRef = database.getReference("dictionaries").child(userID);
    MyDbHelper databaseHelper;
 //   boolean flag;
    public CategDictionary(Context context, String name){
        myRef=myRef.child(name).child("dictionary");
        databaseHelper=new MyDbHelper(context,name);
        this.name=name;

    }
    public CategDictionary(Context context, final String name, final String description){
        myRef=myRef.child(name).child("dictionary");
        databaseHelper=new MyDbHelper(context,name);
        this.name=name;
        this.description=description;
        final DatabaseReference dr=FirebaseDatabase.getInstance().getReference("dictionaries").child(userID).child(name)
                .child("description");
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()==null) dr.setValue(description);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final DatabaseReference dr2=FirebaseDatabase.getInstance().getReference("dictionaries").child(userID).child(name)
                .child("name");
        dr2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()==null) dr2.setValue(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final DatabaseReference dr3=FirebaseDatabase.getInstance().getReference("dictionaries").child(userID).child(name)
                .child("lastEdit");
        dr3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                Date todayDate = new Date();
                String thisDate = currentDate.format(todayDate);
                dr3.setValue(thisDate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
//    public CategDictionary(Context context, String name, boolean flag){
//        this.flag=flag;
//        if(flag) myRef=myRef.child(name);
//        databaseHelper=new MyDbHelper(context,name);
//        this.name=name;
//
//    }
    public static void downloadDict(String user, final String dict, final Context context){
        //DatabaseReference wholeDictRef=database.getReference("dictionaries").child(user).child(dict);
        DatabaseReference dictRef=database.getReference("dictionaries").child(user).child(dict);
        dictRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                DataSnapshot dataSnapshot=dataSnapshot1.child("dictionary");
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


                if(!nameslist.contains(dict)){
                    nameslist.add(dict);
                    DatabaseReference addToMeRef=database.getReference("dictionaries").child(userID).child(dict);
                    addToMeRef.setValue(dataSnapshot1.getValue());
                }
                else{
                    res_dict=dict+ " - copy";
                    while(true){

                        if(!nameslist.contains(res_dict)) break;
                        else res_dict+=" - copy";

                    }
                    DatabaseReference addToMeRef=database.getReference("dictionaries").child(userID).child(res_dict);
                    addToMeRef.setValue(dataSnapshot1.getValue());
                    nameslist.add(res_dict);
                }
                MyDbHelper dbhelp=new MyDbHelper(context,res_dict);
                for(StringTranslation tr: d) {
                    dbhelp.addWord(tr.word,tr.meaning,tr.syns,tr.ex);

                }


                json=gson.toJson(nameslist);
                editor.putString("dictionaries",json);
                editor.apply();
                Toast.makeText(context,"Скачано успешно!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context,"Что-то пошло не так",Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void addWord(String word,ArrayList<StringTranslation> list){
        myRef.child(word).setValue(list);
        DatabaseReference dateRef = database.getReference("dictionaries").child(userID).child(name).child("lastEdit");
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        dateRef.setValue(thisDate);


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
