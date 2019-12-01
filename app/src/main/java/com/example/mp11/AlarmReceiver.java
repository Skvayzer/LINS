package com.example.mp11;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.example.mp11.activities.MainActivity;
import com.example.mp11.activities.SynchronizerService;
import com.example.mp11.fragments.CardFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

//для временных уведовлений
public class AlarmReceiver extends BroadcastReceiver {
    int mNotificationId = 001;
    public static DatabaseReference ref;
    private NotificationManager alarmNotificationManager;
    public AlarmReceiver() {
    }
    //эта функция вызывается для повторения слов через некоторое время
    @Override
    public void onReceive(Context context, Intent intent) {

            //уведомление
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

            mBuilder.setSmallIcon(R.drawable.diam) //иконка оповещения
                    .setContentTitle("Lins")
                    .setContentText(context.getString(R.string.notification_message))
                    .setAutoCancel(true); //убрать оповещение после клика

            Intent resultIntent = new Intent(context, MainActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, mNotificationId, resultIntent, PendingIntent.FLAG_ONE_SHOT);
            Gson gson = new Gson();
            SharedPreferences preferences = context.getSharedPreferences("pref", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            int level = intent.getExtras().getInt("level");
            long time = intent.getExtras().getLong("time");




            //сохраннение в очереди сеанса
            Type type = new TypeToken<ArrayDeque<String>>() {
            }.getType();
            ArrayDeque<String> levelTimeDeque = gson.fromJson(preferences.getString("levelTimeDeque", null), type);
            if (levelTimeDeque == null) {
                levelTimeDeque = new ArrayDeque<>();

            }


                levelTimeDeque.offer(level+" "+time);
                editor.putString("levelTimeDeque", gson.toJson(levelTimeDeque));



            Type t = new TypeToken<ArrayList<String>>() {
            }.getType();
            ArrayList<String> ad = gson.fromJson(preferences.getString("levelTimeNotReceived", null), t);
            if (ad != null && ad.size() != 0) {
                ad.remove(level+" "+time);
                editor.putString("levelTimeNotReceived", gson.toJson(ad));


            }
        editor.apply();

            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(mNotificationId, mBuilder.build());

    }
}