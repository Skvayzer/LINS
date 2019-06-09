package com.example.mp11.activities;




import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.mp11.AlarmReceiver;
import com.example.mp11.fragments.CardFragment;
import com.example.mp11.fragments.DictDescriptionFragment;
import com.example.mp11.fragments.DictionariesFragment;
import com.example.mp11.ForDictionaries.EasyWordsBtn;
import com.example.mp11.R;
import com.example.mp11.fragments.SettingsFragment;
import com.example.mp11.fragments.SocialFragment;
import com.example.mp11.fragments.UsersProfileFragment;
import com.example.mp11.fragments.VideoPlayerFragment;
import com.example.mp11.fragments.ViewWordsFragment;


//главная активность с основным меню и т.д.
//она имплементирует InteractionListener'ы со всех фрагментов, потому что так сказала студия
public class MainActivity extends AppCompatActivity implements CardFragment.OnFragmentInteractionListener,
        SocialFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener,
        VideoPlayerFragment.OnFragmentInteractionListener, DictionariesFragment.OnFragmentInteractionListener,
        DictDescriptionFragment.OnFragmentInteractionListener, ViewWordsFragment.OnFragmentInteractionListener,
        UsersProfileFragment.OnFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //достаем менеджер фрагментов
        FragmentManager fragmentManager = getFragmentManager();

        //создаём фрагмент с карточками, потому что он должен появляться первым при запуске приложения
        Fragment first_frag=CardFragment.newInstance("kek","lol");

        //нижнее меню с навигацией по фрагментам
        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        //переходим сначала на фрагмент с карточками
        bottomNavigationView.getMenu().getItem(2).setChecked(true);
        //заменяем этим фрагментом отведённый ему участок
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, first_frag);

        //при клике на нижнее меню
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_social:
                                //первый фрагмент со социальным
                                selectedFragment = SocialFragment.newInstance("kek", "lol");
                                bottomNavigationView.getMenu().getItem(0).setChecked(true);
                                break;
                            case R.id.navigation_dictionaties:
                                //второй со словарями на устройстве
                                selectedFragment = DictionariesFragment.newInstance("kek","lol");
                                bottomNavigationView.getMenu().getItem(1).setChecked(true);
                                break;
                            case R.id.navigation_cards:
                                //третий с карточками
                                selectedFragment = CardFragment.newInstance("kek", "lol");
                                bottomNavigationView.getMenu().getItem(2).setChecked(true);
                                break;
                            case R.id.navigation_video:
                                //четвёртый с видеоплеером
                                selectedFragment = VideoPlayerFragment.newInstance("kek","lol");
                                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                                break;
                            case R.id.navigation_settings:
                                //пятый с настройками
                                selectedFragment = SettingsFragment.newInstance("kek","lol");

                                bottomNavigationView.getMenu().getItem(4).setChecked(true);
                                break;


                        }
                        //заменяем на выбранный фрагмент
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        //применение операций
        transaction.commit();
        //запуск сервиса с переводом слов вне приложения
        Intent serv=new Intent(getApplicationContext(), EasyWordsBtn.class);
        startService(serv);



        //уведомлялки
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        //установка времени, через которое придёт уведомление
        long startTime = 10;
        // устанавливаем период уведомлений
        PendingIntent pi = PendingIntent.getBroadcast(this, 001, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC, SystemClock.elapsedRealtime() +
                startTime, 10, pi);


        //broadcast reciever, который завершает активность, когда его попросят другие активности
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_activity")) {
                    finish();

                }
            }
        };
        //регистрируем его
        registerReceiver(broadcastReceiver, new IntentFilter("finish_activity"));

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }




    //переход к предыдущему фрагменту
    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();

        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

}
