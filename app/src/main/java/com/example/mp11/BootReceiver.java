package com.example.mp11;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.mp11.activities.SynchronizerService;

public class BootReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context, SynchronizerService.class);
        ComponentName service = context.startService(i);
        Toast.makeText(context, "Booting Completed", Toast.LENGTH_LONG).show();
    }
}
