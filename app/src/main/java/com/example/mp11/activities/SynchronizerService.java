package com.example.mp11.activities;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.mp11.ForDatabases.MyDbHelper;
import com.example.mp11.ForDictionaries.CategDictionary;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


//сервис для синхронизации устройства с сервером
public class SynchronizerService extends Service {

    public SynchronizerService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    //когда сервис убили(при закрытии приложения), надобно его воскресить
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        //спустя какое-то время
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()+1000, restartServicePI);

    }
    @Override
    public void onCreate() {
        new MyTask().execute();
    }

    private class MyTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {

              FirebaseDatabase db=FirebaseDatabase.getInstance();

              final Gson gson = new Gson();
              final SharedPreferences preferences = getApplicationContext().getSharedPreferences("pref", MODE_PRIVATE);
              final SharedPreferences.Editor editor = preferences.edit();



            final String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
            //собираем словари и новодобавленные слова
            final DatabaseReference ref=db.getReference().child("dictionaries").child(userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        Type type = new TypeToken<List<String>>() {
                        }.getType();
                        ArrayList<String> dictNamesHere=gson.fromJson(preferences.getString("dictionaries",null),type);
                        if(dictNamesHere==null)dictNamesHere=new ArrayList<>();
                        if(!dictNamesHere.contains(ds.getKey())){

                                CategDictionary.downloadDict(userId, ds.getKey(), getApplicationContext(), false);

                                dictNamesHere.add(ds.getKey());
                                editor.putString("dictionaries", gson.toJson(dictNamesHere));
                                editor.apply();
                            //}
                        }else {
                            MyDbHelper helper = new MyDbHelper(getApplicationContext(), ds.getKey());
                            for (DataSnapshot ds1 : ds.child("dictionary").getChildren()) {
                                if (helper.getWord(ds1.getKey()).size() == 0) {
                                    for (DataSnapshot ds2 : ds1.getChildren()) {
                                        if(ds2.child("definition").getValue()!=null&&ds2.child("syns").getValue()!=null&&ds2.child("ex").getValue()!=null&&ds1.getKey()!=null){
                                            helper.addWord(ds1.getKey(), ds2.child("definition").getValue().toString(), ds2.child("syns").getValue().toString(), ds2.child("ex").getValue().toString());

                                        }

                                    }
                                }
                            }
                        }


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            DatabaseReference ref1=db.getReference().child("users").child(userId).child("ebbycurve");

            //собираем прогресс изучения по кривой забывания Эббингауза, очередь с уровнями и временем, которые уже надо повторить и такую же очередь, но с теми, которые надо
            // повторить через время по уведомлению

            ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("progress").getValue()!=null) editor.putString("ebbyCurve",dataSnapshot.child("progress").getValue().toString());
                    else editor.remove("ebbyCurve");
                    if(dataSnapshot.child("levelTimeDeque").getValue()!=null) editor.putString("levelTimeDeque",dataSnapshot.child("levelTimeDeque").getValue().toString());
                    else editor.remove("levelTimeDeque");

                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                    if(dataSnapshot.child("levelTimeNotReceived").getValue()!=null) editor.putString("levelTimeNotReceived",gson.toJson(dataSnapshot.child("levelTimeNotReceived").getValue(t)));
                    else editor.remove("levelTimeNotReceived");
                    editor.apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            SynchronizerService.this.stopSelf();
            return null;
        }
    }

}
