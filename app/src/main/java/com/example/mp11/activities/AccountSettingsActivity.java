package com.example.mp11.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;

import com.example.mp11.R;
import com.google.firebase.auth.FirebaseAuth;


//активность для настроек аккаунта пользователя
public class AccountSettingsActivity extends PreferenceActivity {
    SharedPreferences sp;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //добавление xml с настройками на экран
        addPreferencesFromResource(R.xml.account_preferences);
        //shared preferences для хранения настроек
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        context=this;
        //поле, при клике на которое можно выйти из аккаунта
        Preference logout=(Preference) findPreference("log_out");
        //на клике
        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference preference) {
                //выход из аккаунта firebase
                FirebaseAuth.getInstance().signOut();
                //интент для активности с входом в аккаунт
                Intent i=new Intent(context,LoginActivity.class);
                //для brodcast reciever'a передаём сообщение, чтобы завершить MainActivity
                //и нельзя было нажатием кнопки назад вернуться к ней
                Intent intent = new Intent("finish_activity");
                sendBroadcast(intent);

                startActivity(i);
                //завершение текущей активности
                finish();
                return true;
            }
        });
        //поле настроек для замены пароля
        Preference resetPassword=(Preference) findPreference("reset_password");
        resetPassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference preference) {
                //выход из аккаунта firebase
                FirebaseAuth.getInstance().signOut();
                //интент на активность с восстановлением пароля
                Intent i=new Intent(context,ResetPasswordActivity.class);
                //для brodcast reciever'a передаём сообщение, чтобы завершить MainActivity
                //и нельзя было нажатием кнопки назад вернуться к ней
                Intent intent = new Intent("finish_activity");
                sendBroadcast(intent);
                startActivity(i);
                //завершение текущей активности
                finish();
                return true;
            }
        });

//        Preference synchronizeServerWithDevice=(Preference)findPreference("server");
//        synchronizeServerWithDevice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//
//                return false;
//            }
//        });
        Preference synchronizeDeviceWithServer=(Preference)findPreference("device");
        synchronizeDeviceWithServer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i=new Intent(context,SynchronizerService.class);

                startService(i);
                return true;
            }
        });
    }
}
