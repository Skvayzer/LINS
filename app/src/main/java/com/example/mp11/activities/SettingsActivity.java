package com.example.mp11.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.os.Bundle;

import com.example.mp11.ForDictionaries.EasyWordsBtn;
import com.example.mp11.R;

//активность для настроек приложения
public class SettingsActivity extends PreferenceActivity {
    SharedPreferences sp;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //добавление xml с настройками на экран
        addPreferencesFromResource(R.xml.app_preferences);
        //shared preferences для хранения настроек
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        context=this;
//
        //если включён в настройках английский перевод, говорим об этом сервису
        boolean service=sp.getBoolean("switch_translation",false);
        if(service) EasyWordsBtn.eng=true;
        else EasyWordsBtn.eng=false;

    }
}
