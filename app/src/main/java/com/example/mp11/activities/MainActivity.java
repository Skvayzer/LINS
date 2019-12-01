package com.example.mp11.activities;




import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mp11.AlarmReceiver;
import com.example.mp11.ForDatabases.MyDbHelper;
import com.example.mp11.ForDictionaries.CategDictionary;
import com.example.mp11.fragments.CardFragment;
import com.example.mp11.fragments.DictDescriptionFragment;
import com.example.mp11.fragments.DictionariesFragment;
import com.example.mp11.ForDictionaries.EasyWordsBtn;
import com.example.mp11.R;
import com.example.mp11.fragments.IOnBackPressed;
import com.example.mp11.fragments.SettingsFragment;
import com.example.mp11.fragments.SocialFragment;
import com.example.mp11.fragments.UsersProfileFragment;
import com.example.mp11.fragments.VideoPlayerFragment;
import com.example.mp11.fragments.ViewWordsFragment;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;


//главная активность с основным меню и т.д.
//она имплементирует InteractionListener'ы со всех фрагментов, потому что так сказала студия
public class MainActivity extends AppCompatActivity implements CardFragment.OnFragmentInteractionListener,
        SocialFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener,
        VideoPlayerFragment.OnFragmentInteractionListener, DictionariesFragment.OnFragmentInteractionListener,
        DictDescriptionFragment.OnFragmentInteractionListener, ViewWordsFragment.OnFragmentInteractionListener,
        UsersProfileFragment.OnFragmentInteractionListener {

        private SharedPreferences preferences;
        private Gson gson;
        private SharedPreferences.Editor editor;
        public static String userId;
        public Fragment selectedFragment;
        Bundle bundy = new Bundle();
        BroadcastReceiver broadcastReceiver;
        public ArrayList<LinkedHashMap<Long, HashMap<String, ArrayDeque<String>>>> ebbyCurve=new ArrayList<>();


        private final Fragment fragment1=SocialFragment.newInstance("","");
        private final Fragment fragment2=DictionariesFragment.newInstance("","");
        private final Fragment fragment3=CardFragment.newInstance("","");
        private final Fragment fragment4=VideoPlayerFragment.newInstance("","");
        private final Fragment fragment5=SettingsFragment.newInstance("","");

        public static HashMap<String,ArrayDeque<String>> further_container=new HashMap<>(),
            lost_container=new HashMap<>();
        public static ArrayDeque<String> current_session=null;
        FirebaseDatabase db;

        private final int REQUEST_SYSTEM_ALERT_PERMISSION=121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //достаем менеджер фрагментов
        FragmentManager fragmentManager = getFragmentManager();

        //создаём фрагмент с карточками, потому что он должен появляться первым при запуске приложения

        selectedFragment=fragment3;

        //нижнее меню с навигацией по фрагментам
        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        //переходим сначала на фрагмент с карточками
        bottomNavigationView.getMenu().getItem(2).setChecked(true);
        //заменяем этим фрагментом отведённый ему участок
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, selectedFragment);




        //при клике на нижнее меню
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction = getSupportFragmentManager().beginTransaction();
                        switch (item.getItemId()) {
                            case R.id.navigation_social:
                                //первый фрагмент со социальным

                                selectedFragment = fragment1;

                                bottomNavigationView.getMenu().getItem(0).setChecked(true);
                                break;
                            case R.id.navigation_dictionaties:
                                //второй со словарями на устройстве
                                selectedFragment = fragment2;
                                bottomNavigationView.getMenu().getItem(1).setChecked(true);
                                break;
                            case R.id.navigation_cards:
                                //третий с карточками
                                selectedFragment = fragment3;
                                bottomNavigationView.getMenu().getItem(2).setChecked(true);
                                break;
                            case R.id.navigation_video:
                                //четвёртый с видеоплеером
                                selectedFragment = fragment4;
                                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                                break;
                            case R.id.navigation_settings:
                                //пятый с настройками
                                selectedFragment =fragment5;

                                bottomNavigationView.getMenu().getItem(4).setChecked(true);
                                break;


                        }
                        //заменяем на выбранный фрагмент

                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commitAllowingStateLoss();

                        return true;
                    }
                });

        //применение операций

        transaction.commitAllowingStateLoss();

        //запуск сервиса с переводом слов вне приложения
        Intent serv=new Intent(getApplicationContext(), EasyWordsBtn.class);
        //startService(serv);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serv);
        } else {
            startService(serv);
        }


        //broadcast reciever, который завершает активность, когда его попросят другие активности(чтобы при выходе из аккаунта действительно выйти)
        broadcastReceiver = new BroadcastReceiver() {

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

        //инициализируем firebase и shared preferences
        db=FirebaseDatabase.getInstance();
        userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference ref=db.getReference().child("dictionaries").child(userId);
        gson=new Gson();
        preferences=getSharedPreferences("pref",MODE_PRIVATE);
        editor=preferences.edit();

        //достаём прогресс изученмя и полученные и неполученные сеансы повторений
        if(preferences.getString("ebbyCurve",null)!=null){
            Type t = new TypeToken<ArrayList<LinkedHashMap<Long, HashMap<String, ArrayDeque<String>>>>>() {
            }.getType();
            ebbyCurve=gson.fromJson(preferences.getString("ebbyCurve",null),t);
        }else{
            //создаём пустую кривую забывания
            for(int i=0;i<9;i++){
                ebbyCurve.add(new LinkedHashMap<Long, HashMap<String, ArrayDeque<String>>>());
            }
        }
        //достаём очереди с сеансами, которые нужно повторить и которые должны повториться через время, кладём их на fireBase
        Type t = new TypeToken<ArrayDeque<String>>() {
        }.getType();
        ArrayDeque<String> levelTimeDeque = gson.fromJson(preferences.getString("levelTimeDeque", null), t);
        t = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> ad = gson.fromJson(preferences.getString("levelTimeNotReceived", null), t);
        if(levelTimeDeque!=null&&ad!=null){
            DatabaseReference ref1=db.getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("ebbycurve");
            ref1.child("levelTimeDeque").setValue(gson.toJson(levelTimeDeque));
            ref1.child("levelTimeNotReceived").setValue(ad);
        }

        //спросить разрешение на показ поверх экрана для сервиса
        ActivityCompat.requestPermissions
                (MainActivity.this,
                        new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
                        REQUEST_SYSTEM_ALERT_PERMISSION);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onSaveInstanceState(bundy);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            onSaveInstanceState(bundy);
        }
    }
    //сохранить состояние активности
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("bundy", bundy);
    }
    //восстановить состояние активности
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.getBundle("bundy");
    }

    //переход к предыдущему фрагменту по кнопке назад на устройтсве
    @Override
    public void onBackPressed() {

        if (!(selectedFragment instanceof IOnBackPressed) || !((IOnBackPressed) selectedFragment).onBackPressed()) {
            //super.onBackPressed();
            int count = getSupportFragmentManager().getBackStackEntryCount();

            if (count == 0) {
                super.onBackPressed();

            } else if(!isFinishing()) {
                try {
                    getSupportFragmentManager().popBackStackImmediate();
                }catch (IllegalStateException e){
                    e.printStackTrace();
                    finish();
                }
            }

        }
    }
    //установка по уровню времени, через которое придёт уведомление и слово повторится
    private void setCalendar(Calendar calendar,int level){
        switch (level){
            case 0:

                calendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE)+20);
                break;
            case 1:

                calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)+1);
                break;
            case 2:

                calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)+8);
                break;
            case 3:

                calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)+1);
                break;
            case 4:

                calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)+2);
                break;
            case 5:

                calendar.set(Calendar.WEEK_OF_YEAR,calendar.get(Calendar.WEEK_OF_YEAR)+1);
                break;
            case 6:

                calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)+1);
                break;
            case 7:

                calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)+4);
                break;
            case 8:

                calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)+1);
                break;
        }
    }
    @Override
    public void onDestroy(){
        //когда приложение закрыли, надо сохранить весь прогресс
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if(CardFragment.tts!= null) {
            CardFragment.tts.stop();
            CardFragment.tts.shutdown();
        }
        saveLostWords();
        if(current_session!=null&&current_session.size()!=0
                &&CardFragment.levelTimeDeque!=null && CardFragment.levelTimeDeque.size()!=0){
            int level=Integer.parseInt(CardFragment.levelTimeDeque.peek().split(" ")[0]);
            saveFurtherWords(level);
        }


        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference ref=db.getReference().child("users").child(userId).child("ebbycurve");
        ref.child("progress").setValue(gson.toJson(ebbyCurve));
        ref.child("levelTimeDeque").setValue(gson.toJson(CardFragment.levelTimeDeque));

    }


    //сохранение слов, которые перемещаются на уровень выше, создание уведомления по времени
    public void saveFurtherWords(final int level){
        if(further_container!=null&&further_container.size()!=0&&level+1<ebbyCurve.size()) {
            LinkedHashMap<Long, HashMap<String, ArrayDeque<String>>> timeDictWords;

            timeDictWords = ebbyCurve.get(level + 1);

            //берем текущее время и устанавливаем его на уровень больше, кладём в SP кривую забывания с перемещенными словами
            final Calendar calendar = Calendar.getInstance();
            //calendar.set(Calendar.SECOND,0);
            setCalendar(calendar, level + 1);
            timeDictWords.put(calendar.getTimeInMillis(), further_container);
            editor.putString("ebbyCurve", gson.toJson(ebbyCurve));
            editor.apply();

            //интент для запуска триггера в AlarmReceiver'e, туда кладём уровень и время сеанса для повторения
            Intent myIntent = new Intent(MainActivity.this, AlarmReceiver.class);
            myIntent.putExtra("level", level + 1);
            myIntent.putExtra("time", calendar.getTimeInMillis());


            int id = preferences.getInt("idAlarm", 0);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, id++, myIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            editor.putInt("idAlarm", id);
            editor.apply();

            //стартуем
            if (Build.VERSION.SDK_INT < 19) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            //сохраняем в неполученные сеансыэтот сеанс, сохраняем значения на устройство и firebase
            FirebaseDatabase db=FirebaseDatabase.getInstance();
            final DatabaseReference ref=db.getReference().child("users").child(userId).child("ebbycurve");
            Type t = new TypeToken<ArrayList<String>>() {
            }.getType();
            ArrayList<String> ad=gson.fromJson(preferences.getString("levelTimeNotReceived",null),t);



            if (ad!=null){
                ad.add((level+1)+" "+calendar.getTimeInMillis());
            }else{
                ad=new ArrayList<>();
                ad.add((level+1)+" "+calendar.getTimeInMillis());
            }

            ref.child("levelTimeNotReceived").setValue(ad);
            editor.putString("levelTimeNotReceived",gson.toJson(ad));

            editor.apply();
        }
    }
    //сохранение слов, которые пользователь не вспомнил, они отправляются на первый уровень либо к последнему сеансу, либо к новому, если таких нет
    public void saveLostWords(){

        if(lost_container!=null&&lost_container.size()!=0) {
            try {
                //если без ошибок можно добавить к последнему позднему времени на 1 уровне, то неузнанные слова отправим туда
                LinkedHashMap<Long, HashMap<String, ArrayDeque<String>>> timeDictWords = ebbyCurve.get(0);
                Long last = null;
                for (Long item : timeDictWords.keySet()) last = item;
                HashMap<String, ArrayDeque<String>> dictWords = timeDictWords.get(last);
                dictWords.putAll(lost_container);
                editor.putString("ebbyCurve", gson.toJson(ebbyCurve));
                editor.apply();
            }catch (NullPointerException e){
                e.printStackTrace();
                //иначе создаем новый контейнер по времени на 1 уровне
                LinkedHashMap<Long, HashMap<String, ArrayDeque<String>>> timeDictWords = ebbyCurve.get(0);
                Calendar calendar=Calendar.getInstance();
                setCalendar(calendar,0);
                timeDictWords.put(calendar.getTimeInMillis(),lost_container);
                editor.putString("ebbyCurve", gson.toJson(ebbyCurve));
                editor.apply();



                //кладём будильнику ровень и время сеанса из кривой
                int id=preferences.getInt("idAlarm",0);
                Intent myIntent = new Intent(MainActivity.this, AlarmReceiver.class);
                myIntent.putExtra("level",0);
                myIntent.putExtra("time",calendar.getTimeInMillis());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, id++, myIntent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                editor.putInt("idAlarm",id);
                editor.apply();
                if (Build.VERSION.SDK_INT < 19) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }

                //так же сохраняем
                FirebaseDatabase db=FirebaseDatabase.getInstance();
                DatabaseReference ref=db.getReference().child("users").child(userId).child("ebbycurve");
                Type t = new TypeToken<ArrayList<String>>() {
                }.getType();
                ArrayList<String> ad=gson.fromJson(preferences.getString("levelTimeNotReceived",null),t);



                if (ad!=null){
                    ad.add(0+" "+calendar.getTimeInMillis());
                }else{
                    ad=new ArrayList<>();
                    ad.add(0+" "+calendar.getTimeInMillis());
                }

                ref.child("levelTimeNotReceived").setValue(ad);
                editor.putString("levelTimeNotReceived",gson.toJson(ad));

                editor.apply();
            }
        }
    }
    //дано ли разрешение?
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_SYSTEM_ALERT_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                return;

        }
    }
}
