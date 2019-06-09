package com.example.mp11;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.mp11.activities.MainActivity;

public class AlarmReceiver extends BroadcastReceiver {
    int mNotificationId = 001;
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context);

        mBuilder.setSmallIcon(R.drawable.diam) //иконка оповещения
                .setContentTitle("lins")
                .setContentText("Время повторять слова!")
                .setAutoCancel(true); //убрать оповещение после клика

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, mNotificationId, resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }


}