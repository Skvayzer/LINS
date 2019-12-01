package com.example.mp11.ForDictionaries;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.mp11.ForDatabases.MyDbHelper;
import com.example.mp11.ForDatabases.WordModel;
import com.example.mp11.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//класс, который реализует словарь по категориям
//через него, а не помошник бд, можно добавлять слова
//и тут же они и отправляются на Firebase
//структуру данных в бд Firebase смотрите в папке с проектом
public class CategDictionary {
    public String name,description;
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    //id пользователя в Firebase
    static String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();

    private DatabaseReference myRef = database.getReference("dictionaries").child(userID);
    private MyDbHelper databaseHelper;
    //по переданному названию создаём словарь и бд
    public CategDictionary(Context context, String name){
        myRef=myRef.child(name).child("dictionary");
       // сюда завернут помошника sqlite бд
        databaseHelper=new MyDbHelper(context,name);
        this.name=name;

    }
    //конструктор словаря, но ещё с добавлением его описания на Firebase
    public CategDictionary(Context context, final String name, final String description){
        myRef=myRef.child(name).child("dictionary");
        //сюда завернут помошника sqlite бд
        databaseHelper=new MyDbHelper(context,name);
        this.name=name;
        this.description=description;
        //просто кладём на Firebase имя словаря, его описание и дату создания
        FirebaseDatabase.getInstance().getReference("dictionaries").child(userID).child(name)
                .child("description").setValue(description);
        FirebaseDatabase.getInstance().getReference("search_list").child(name+"4el2psy97congree"+userID).setValue("");
        FirebaseDatabase.getInstance().getReference("dictionaries").child(userID).child(name)
                .child("name").setValue(name);
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        FirebaseDatabase.getInstance().getReference("dictionaries").child(userID).child(name)
                .child("lastEdit").setValue(thisDate);

    }
    //скачать словарь по id пользователя и названию словаря из Firebase
    public static void downloadDict(String user, final String dict, final Context context){
        //просто ссылка на нужную часть Firebase бд
        DatabaseReference dictRef=database.getReference("dictionaries").child(user).child(dict);
        dictRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                DataSnapshot dataSnapshot=dataSnapshot1.child("dictionary");
                //для хранения переводов
                ArrayList<StringTranslation> d=new ArrayList<>();
                //пробегаемся по словам из словаря и добавляем их в d
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    for(DataSnapshot dsna: ds.getChildren()) d.add(dsna.getValue(StringTranslation.class));

                }
                Gson gson=new Gson();
                //берем из shared preferences массив с названиями словарей, которые есть на стройстве
                SharedPreferences preferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                String json=preferences.getString("dictionaries",null);
                Type type = new TypeToken<List<String>>() {
                }.getType();
                ArrayList<String> nameslist=gson.fromJson(json,type);
                if(nameslist==null) nameslist=new ArrayList<>();

                //имя выбранного словаря
                String res_dict=dict;
                //если на устройстве ещё нет такого словаря, добавить
                if(!nameslist.contains(dict)){
                    nameslist.add(dict);
                    DatabaseReference addToMeRef=database.getReference("dictionaries").child(userID).child(dict);
                    addToMeRef.setValue(dataSnapshot1.getValue());
                }
                else{
                    //если есть, то добавить его с постфиксом "- copy"
                    res_dict=dict+ " - copy";
                    //цикл, потому что, возможно, у вас уже есть неколько копий
                    while(true){
                        //в итоге добавляются эти постфиксы, пока такого словаря с таким названием у вас не будет
                        if(!nameslist.contains(res_dict)) break;
                        else res_dict+=" - copy";

                    }
                    //копируем его к СЕБЕ на Firebase

                    DatabaseReference addToMeRef=database.getReference("dictionaries").child(userID).child(res_dict);
                    addToMeRef.setValue(dataSnapshot1.getValue());
                    //добавляем к названиям словарей устройства
                    nameslist.add(res_dict);
                }
                //создаём словарь на устройстве
                MyDbHelper dbhelp=new MyDbHelper(context,res_dict);
                ArrayList<String> rest_words=new ArrayList<>();
                for(StringTranslation tr: d) {
                    dbhelp.addWord(tr.word,tr.meaning,tr.syns,tr.ex);
                    rest_words.add(tr.word);
                }
                //кладём массив слов как неизученных
                editor.putString(res_dict+"-rest",gson.toJson(rest_words));

                FirebaseDatabase db=FirebaseDatabase.getInstance();
                String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                final DatabaseReference ref=db.getReference().child("dictionaries").child(userId).child(res_dict).child("rest");
                ref.setValue(rest_words);

                //пихаем массиив с названиями словарей обратно в shared preferences
                json=gson.toJson(nameslist);
                editor.putString("dictionaries",json);
                editor.apply();
                Toast.makeText(context,context.getString(R.string.downloaded_successfully),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context,context.getString(R.string.somethings_wrong_error),Toast.LENGTH_SHORT).show();
            }
        });
    }
    //скачать словарь по id пользователя и названию словаря из Firebase, но не добавляя к себе
    public static void downloadDict(String user, final String dict, final Context context, boolean flag){
        //просто ссылка на нужную часть Firebase бд
        DatabaseReference dictRef=database.getReference("dictionaries").child(user).child(dict);
        dictRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                DataSnapshot dataSnapshot=dataSnapshot1.child("dictionary");
                //для хранения переводов
                ArrayList<StringTranslation> d=new ArrayList<>();
                //пробегаемся по словам из словаря и добавляем их в d
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    for(DataSnapshot dsna: ds.getChildren()){
                        StringTranslation item=new StringTranslation();
                        item.word=ds.getKey();
                        item.index=Integer.parseInt(dsna.getKey());
                        item.meaning=dsna.child("definition").getValue().toString();
                        if(dsna.child("syns").getValue()!=null)
                        item.syns=dsna.child("syns").getValue().toString();
                        if(dsna.child("ex").getValue()!=null)
                        item.ex=dsna.child("ex").getValue().toString();


                        d.add(item);
                    }

                }
                Gson gson=new Gson();
                //берем из shared preferences массив с названиями словарей, которые есть на стройстве
                SharedPreferences preferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();

                //имя выбранного словаря
                  String res_dict=dict;
                //создаём словарь на устройстве
                MyDbHelper dbhelp=new MyDbHelper(context,res_dict);
                ArrayList<String> rest_words=new ArrayList<>();
                for(StringTranslation tr: d) {
                    dbhelp.addWord(tr.word,tr.meaning,tr.syns,tr.ex);
                    rest_words.add(tr.word);
                }
                //кладём массив слов как неизученных
                editor.putString(res_dict+"-rest",gson.toJson(rest_words));
                //пихаем массиив с названиями словарей обратно в shared preferences
                editor.apply();
                Toast.makeText(context,context.getString(R.string.downloaded_successfully),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context,context.getString(R.string.somethings_wrong_error),Toast.LENGTH_SHORT).show();
            }
        });
    }
    //добавление слова по массиву со строковыми переводами
    public void addWord(String word,ArrayList<StringTranslation> list){
        myRef.child(word).setValue(list);
        DatabaseReference dateRef = database.getReference("dictionaries").child(userID).child(name).child("lastEdit");
        //кладём дату изменения словаря
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        dateRef.setValue(thisDate);


        //кладём переводы в бд на устройстве
        for(StringTranslation a: list){
            databaseHelper.addWord(word,a.meaning,a.syns,a.ex);
        }

    }
    //удаление слова с устройства
    public void deleteWord(String word){
        databaseHelper.deleteWord(word);
    }
    //получить слова из бд устройства в разных видах
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

}
