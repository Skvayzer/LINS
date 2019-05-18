package com.example.mp11;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    int mNotificationId = 001;
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
       // Toast.makeText(context, "AlarmReceiver", Toast.LENGTH_LONG).show();
        // Gets an instance of the NotificationManager service
        final NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context);

        mBuilder.setSmallIcon(R.drawable.diam) // notification icon
                .setContentTitle("lins")
                .setContentText("Время повторять слова!")
                .setAutoCancel(true); // clear notification after click

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, mNotificationId, resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }


//    public static String NOTIFICATION_ID = "notification-id";
//    public static String NOTIFICATION = "notification";
//
//    public void onReceive(Context context, Intent intent) {
//
//        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Notification notification = intent.getParcelableExtra(NOTIFICATION);
//        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
//        notificationManager.notify(id, notification);
//
//    }
}