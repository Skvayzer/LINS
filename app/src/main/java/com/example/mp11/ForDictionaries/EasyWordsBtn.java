package com.example.mp11.ForDictionaries;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.R;
import com.example.mp11.activities.GetAllWordsActivity;
import com.example.mp11.adapters.StringAdapter;
import com.example.mp11.fragments.ClickListener;
import com.example.mp11.fragments.DictionariesFragment;

import com.example.mp11.adapters.TranslationAdapter;
import com.example.mp11.ForDictionaries.yandexdictslate.Model;
import com.example.mp11.ForDictionaries.yandexdictslate.RestApi;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//сервис, чтобы вне приложения слова переводить
public class EasyWordsBtn extends Service implements View.OnTouchListener, View.OnClickListener, ClickListener, TextToSpeech.OnInitListener {

    private View topLeftView;

    private ImageButton overlayedButton;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private WindowManager wm;

    String message="ДА ТЫ НЕ ТЫКАЙ";
    ClipboardManager clipBoard;

    //для REST api с яндекс словарём
    private static String DICT_URI_JSON = "https://dictionary.yandex.net/";
    private static final String SAMPLE_KEY = "dict.1.1.20190329T031256Z.531eac57eaa2f4eb.57b8133578fb75df82180bc54fe6602d41795051";
    //английский ли перевод?
    public static boolean eng=false;

    private boolean EngTranslation=false;
    private boolean isPai=false;

    private TextToSpeech tts;

    private String merriam_key="5e710d58-4d0b-4435-a025-d455b7437bfc";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
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
        //спустя 2 cek
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()+2000, restartServicePI);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //для кнопки поверх всего экрана
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        overlayedButton = new ImageButton(this);
        overlayedButton.setVisibility(View.INVISIBLE);
        overlayedButton.setOnTouchListener(this);
        //картинка кнопки
        Bitmap img= BitmapFactory.decodeResource(getResources(), R.drawable.curious);
        overlayedButton.setImageBitmap(img);
        overlayedButton.setBackgroundColor(Color.TRANSPARENT);
        overlayedButton.setOnClickListener(this);

        //параметры для добавления вьюшки на экран
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        wm.addView(overlayedButton, params);

        //создаём вьюшку в левом верхнем углу
        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 0;
        topLeftParams.height = 0;
        wm.addView(topLeftView, topLeftParams);

        //менеджер буфера обмена клавиатуры, чтобы при копировании слова показывать кнопку
        clipBoard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener( new ClipboardListener() );
    }

    @Override
    public void onDestroy() {
//        Intent restartService = new Intent(getApplicationContext(),
//                this.getClass());
//        restartService.setPackage(getPackageName());
//        PendingIntent restartServicePI = PendingIntent.getService(
//                getApplicationContext(), 1, restartService,
//                PendingIntent.FLAG_ONE_SHOT);
//        //спустя какое-то время
//        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        alarmService.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()+1000, restartServicePI);
    }

    //на тыке перемещаем кнопку
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //берем координаты касания
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            //берём позицию кнопки на экране на момент касания
            overlayedButton.getLocationOnScreen(location);
            //пихаем координаты
            originalXPos = location[0];
            originalYPos = location[1];

            //изменение координаты
            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int[] topLeftLocationOnScreen = new int[2];
            //берём координаты левого верхнего угла
            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);
            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayedButton.getLayoutParams();

            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            //если изменения координат слишком малы и кнопка пока не двигается, то ничего не делать
            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                return false;
            }

            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);
            //перемещаем кнопку
            wm.updateViewLayout(overlayedButton, params);
            moving = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (moving) {
                return true;
            }
        }

        return false;
    }

    //на клике переводить
    @Override
    public void onClick(View v) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();


            //во внешнем потоке лемматизация слова и перевод
            CallbackTask task=new CallbackTask();
            task.execute(inflections(message));




    }

    @Override
    public void onItemClick(int position, View v) {

    }

    @Override
    public void onItemLongClick(int position, View v) {

    }

    @Override
    public void onInit(int status) {

    }

    //для действий при копировании слова
    class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener
    {
        public void onPrimaryClipChanged()
        {
            ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);

            //берём скопированное слово
            try {
                //берём скопированное слово
                message = item.getText().toString();
            }catch (NullPointerException e){
                message="Что-то пошло не так";
            }
            //появляется кнопка на 10 секунд
            overlayedButton.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    overlayedButton.setVisibility(View.INVISIBLE);

                }
            }, 10000);


        }
    }
    //перевод эмулятором гугл слова на английском(уже не используется в связи с его медлительностью и
    //багом с парсингом json'a, возникшим накануне
    void merriamDict(final String text){




                //массив со строковыми переводами, куда всё запихнётся
                final ArrayList<StringTranslation> stlist=new ArrayList<>();
                //если есть определение





                //всплывающее окошко для перевода
                AlertDialog.Builder pop = new AlertDialog.Builder(getApplicationContext());
                final AlertDialog kek=pop.create();
                kek.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_word,null);

                view.setBackgroundColor(Color.WHITE);

                kek.setView(view);
                //список с определениями, которых может быть несколько(как существительное, глагол и т.д.)
                ListView lv = (ListView) view.findViewById(R.id.word_list);
                //показать транскрипцию и слово
                ((TextView)view.findViewById(R.id.current_word)).setText(text);
                TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), stlist.toArray(new StringTranslation[stlist.size()]),false);
                lv.setAdapter(adapter);
                Button btn=(Button)view.findViewById(R.id.addToDict_btn);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //если хочет пользователь добавить в словарь, показать существующие словари
                        AlertDialog.Builder pop = new AlertDialog.Builder(getApplicationContext());
                        final AlertDialog lol=pop.create();
                        lol.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        LayoutInflater inflater = (LayoutInflater)   getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        //здесь хранятся словари
                        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                                R.layout.recycler_view, null, false);

                        //адаптер для отображения словарей
                        DictionariesFragment.MyContentAdapter adapter = new DictionariesFragment.MyContentAdapter(recyclerView.getContext(),
                                stlist, lol,text);

                        recyclerView.setAdapter(adapter);
                        recyclerView.setHasFixedSize(true);
                        // отступ для плитки
                        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
                        recyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
                        //три словаря в строке
                        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
                        recyclerView.setBackgroundColor(Color.WHITE);
                        lol.setView(recyclerView);
                        lol.show();
                        kek.hide();
                    }
                });
                //как звучит слово?
                ImageButton sound=(ImageButton)view.findViewById(R.id.sound_btn);
                sound.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextToSpeech  tts = new TextToSpeech(getApplicationContext(), EasyWordsBtn.this);
                        tts.setLanguage(Locale.US);
                        tts.speak(message,TextToSpeech.QUEUE_ADD, null);

                    }
                });

                kek.show();



    }
    //перевод в яндекс словаре, в качестве толкового словаря(перевода с английского на английский) он не очень
    void getReport(final String text) {
        //ретрофит для rest api, указываем api ключ словаря
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DICT_URI_JSON)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi service = retrofit.create(RestApi.class);
        Call<Model> call = service.lookup(SAMPLE_KEY, "en-ru", text);
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {


                Gson gson = new Gson();
                if (response.isSuccessful()) {

                    String successResponse = gson.toJson(response.body());
                    Log.d("RESULT", "successResponse: " + successResponse);
                } else {

                    if (null != response.errorBody()) {
                        String errorResponse = null;
                        try {
                            errorResponse = response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("RUSELT", "errorResponse: " + errorResponse);
                    }

                }


                //парсится перевод
                String meanings="";
                String syns="(";
                String ex="";

                final ArrayList<String> defmean=new ArrayList<>();
                final ArrayList<String> defsyns=new ArrayList<>();
                final ArrayList<String> defex=new ArrayList<>();

                //сюда пихаются строковые определения
                final ArrayList<StringTranslation> stringDict=new ArrayList<>();
                String transcription="";

                for (int i = 0; i < response.body().def.length; i++) {
                    syns="(";

                    StringTranslation item=new StringTranslation();
                    //тут просто парсинг и красивый вывод в строки определений, синонимов, примеров
                    for (int j = 0; j < response.body().def[i].tr.length; j++) {

                        Model.Def.Tr cur = response.body().def[i].tr[j];

                        meanings+=cur.text + ", ";
                        if(cur.syn!=null)
                            for (Model.Def.Tr.Syn a : cur.syn) {

                                meanings+=a.text+", ";
                            }
                        if(cur.mean!=null)
                            for (Model.Def.Tr.Mean a : cur.mean) {
                                boolean isEnglish = true;
                                String now=a.text;
                                //если словарь дал в определении английское слово, то суём его в синонимы
                                for ( char c : now.toCharArray() ) {
                                    if ( Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN ) {
                                        isEnglish = false;
                                        break;
                                    }
                                }
                                if(isEnglish){

                                    syns+=now+", ";
                                }
                                else{

                                    meanings+=now+", ";
                                }

                            }
                        if(cur.ex!=null)
                            for (Model.Def.Ex a : cur.ex) {
                                for (Model.Def.Tr b : a.tr){
                                    ex+=a.text + " - " + b.text + '\n';
                                }

                            }


                    }
                    //транскрипция слова
                    transcription="["+response.body().def[i].ts+"]";
                    item.index=i+1;
                    item.word=text;

                    //кладём всё в соответствущие структуры данных и очищаем строки
                    if(meanings.length()!=0&& meanings.length()>=4){

                        meanings=meanings.substring(0,meanings.length()-2);
                        item.meaning=meanings;
                    }
                    defmean.add(meanings);
                    meanings="";

                    if(syns.length()!=0 && syns.length()>=4) {
                        syns=syns.substring(0,syns.length()-2);
                        item.syns=syns+")";
                    }
                    defsyns.add(syns+")");
                    syns="(";

                    if(ex.length()!=0){
                        item.ex=ex;
                    }
                    defex.add(ex);
                    ex="";
                    stringDict.add(item);


                }
                //всплывающее окошко для отображения слова
                AlertDialog.Builder pop = new AlertDialog.Builder(getApplicationContext());
                final AlertDialog kek=pop.create();
                kek.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_word,null);
                view.setBackgroundColor(Color.WHITE);
                kek.setView(view);
                //список определений слова
                final ListView lv = (ListView) view.findViewById(R.id.word_list);
                final ListView pai_lv=(ListView)view.findViewById(R.id.pai_list);
                final TextView header=(TextView)view.findViewById(R.id.current_word);
                header.setText(text+"\n"+transcription);
                //адаптер для этого списка, передаём туда строковый перевод
                final TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), stringDict.toArray(new StringTranslation[stringDict.size()]),false);
                final Button addToDict=(Button)view.findViewById(R.id.addToDict_btn);

                ImageButton sound_btn=(ImageButton)view.findViewById(R.id.sound_btn);
                tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status == TextToSpeech.SUCCESS){
                            int result=tts.setLanguage(Locale.US);
                            if(result==TextToSpeech.LANG_MISSING_DATA ||
                                    result==TextToSpeech.LANG_NOT_SUPPORTED){
                                Toast.makeText(getApplicationContext(),"Язык не поддерживается",Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
                tts.setLanguage(Locale.US);
                sound_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String s=header.getText().toString();
                        if(s.contains("[")) tts.speak(s.substring(0,s.indexOf("[")), TextToSpeech.QUEUE_FLUSH, null);
                        else tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                    }

                });
                lv.setAdapter(adapter);

                //final ArrayList<StringTranslation> stlist=stringDict;
                final ArrayList<StringTranslation> eng_list=new ArrayList<>(), pai_eng_list=new ArrayList<>();
                final String[] pai_word = {text};
                final ArrayList<String> PAI_list=new ArrayList<>();
                final HashMap<String,ArrayList<StringTranslation>> map=new HashMap<>();

                //кнопка "добавить в словарь"
                Button btn=(Button)view.findViewById(R.id.addToDict_btn);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //новое окошка для списка существущих словарей
                        AlertDialog.Builder pop = new AlertDialog.Builder(getApplicationContext());
                        final AlertDialog lol=pop.create();
                        lol.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                        //recycleview, на котором будут словари
                        LayoutInflater inflater = (LayoutInflater)   getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                                R.layout.recycler_view, null, false);

                        //адаптер для словарей
                        DictionariesFragment.MyContentAdapter adapter=null;
                        if(EngTranslation) {
                            ArrayList<StringTranslation> f=null;
                            if(!isPai) {
                                f=eng_list;
                            }else{
                                f=pai_eng_list;
                            }
                            ArrayList<StringTranslation> noHtml = new ArrayList<>();
                            for (StringTranslation a : f) {
                                StringTranslation it = new StringTranslation();
                                String s=android.text.Html.fromHtml(a.meaning).toString();
                                while(s.endsWith("\n")){
                                    s=s.substring(0,s.length()-1);
                                }
                                it.meaning = s;
                                Object b=android.text.Html.fromHtml(a.ex);
                                if(b!=null)
                                it.ex = b.toString();
                                else it.ex="";
                                it.syns = "";
                                noHtml.add(it);
                            }
                            adapter = new DictionariesFragment.MyContentAdapter(recyclerView.getContext(),
                                    noHtml, lol, pai_word[0]);
                        }else{
                            adapter = new DictionariesFragment.MyContentAdapter(recyclerView.getContext(),
                                    stringDict, lol, pai_word[0]);
                        }

                        recyclerView.setAdapter(adapter);
                        recyclerView.setHasFixedSize(true);
                        // отступ для плитки
                        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
                        recyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
                        //3 словаря в ряду
                        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));



                        recyclerView.setBackgroundColor(Color.WHITE);
                        lol.setView(recyclerView);
                        lol.show();
                        kek.hide();
                    }
                });
                ImageButton goToEng=(ImageButton)view.findViewById(R.id.goToEng);
                final View v=view;

                final ProgressBar pb=(ProgressBar)view.findViewById(R.id.load_eng_bar);

                final String finalTranscription = transcription;
                //если режим английского, то спарсить определения,сохранить в массив, чтобы не загрузать снова при переходе
                goToEng.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lv.setOnItemClickListener(null);
                        isPai=false;
                        pai_word[0]=text;
                        addToDict.setVisibility(View.VISIBLE);
                        ImageButton phraseid=(ImageButton)v.findViewById(R.id.phrasesIdioms);
                        if(!EngTranslation){
                            MerriamWebsterApi task=new MerriamWebsterApi(text, eng_list,lv,pb);
                            task.execute(requestBuilder(text,merriam_key));

                            phraseid.setVisibility(View.VISIBLE);
                            //если режим фразовых глаголов и идиом, спарсить их, сохранить в массив, чтобы не загрузать снова при переходе
                            phraseid.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    addToDict.setVisibility(View.GONE);
                                    MerriamWebsterApiPhrasVAndIdioms parse_task=new MerriamWebsterApiPhrasVAndIdioms(text,PAI_list,map,lv,pb);
                                    parse_task.execute(requestBuilder(text,merriam_key));
                                    header.setText(text+"\n"+ finalTranscription);

                                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            //онклик - открыть определение идиомы или фразового глагола
                                            String s=(String)adapterView.getItemAtPosition(i);
                                            pai_eng_list.clear();
                                            isPai=true;
                                            pai_word[0] =s;
                                            addToDict.setVisibility(View.VISIBLE);
                                            pai_eng_list.addAll(map.get(s));
                                            header.setText(s);
                                            TranslationAdapter adapta=new TranslationAdapter(getApplicationContext(), true,map.get(s).toArray(new StringTranslation[map.get(s).size()]),false);
                                            lv.setAdapter(adapta);
                                            lv.setOnItemClickListener(null);
                                        }
                                    });
                                }
                            });
                            EngTranslation=true;
                        }else{
                            header.setText(text+"\n"+finalTranscription);
                            phraseid.setVisibility(View.GONE);
                            lv.setAdapter(adapter);
                            EngTranslation=false;
                        }

                    }
                });

                  kek.show();

            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {

                    throwable.printStackTrace();
                }
            }
        });
    }
    //тут используется апи оксфордского словаря, чтобы сделать лемматизацию
    public String inflections(String w) {
        final String language = "en";
        final String word = w;
        final String word_id = word.toLowerCase();
        return "https://od-api.oxforddictionaries.com:443/api/v2/lemmas/" + language + "/" + word_id;
    }
    //оксфордский словарь делает лемматизацию, т.е. приводит слово в начальную форму
    //т.е. не created, а create
    //не communities, a community
    //и т.д., потому что яндекс словарь иногда такое не понимает, да показывает он тогда всё это в основном как
    //употреблённую часть речи, и трудно уловить значения самого слова, если не даны определения его начальной формы
    public class CallbackTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            //тут получаем json
            final String app_id = "6532a039";
            final String app_key = "b866d663c9393fa1ec6970fb989d31c3";
            try {
                URL url = new URL(params[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setRequestProperty("app_id",app_id);
                urlConnection.setRequestProperty("app_key",app_key);

                // читаем ответ сервера и кладём его в строку
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                return stringBuilder.toString();

            }
            catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //тут json парсим
            try {
                String lemma=new JSONObject(result)
                .getJSONArray("results")
                .getJSONObject(0)
                .getJSONArray("lexicalEntries")
                .getJSONObject(0)
                .getJSONArray("inflectionOf").getJSONObject(0)
                .getString("text");
             getReport(lemma);
            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    getReport(new JSONArray(result).getString(0));
                }catch (JSONException q){
                    q.printStackTrace();
                }
            }
        }
    }
//особый парсинг слова
public class MerriamWebsterApi extends AsyncTask<String, Integer, Object> {
    String word;
    ArrayList<StringTranslation> stlist=new ArrayList<>();
    ListView lv;
    ProgressBar pb;
    public MerriamWebsterApi(String s, ArrayList<StringTranslation> stlist, ListView lv, ProgressBar pb){
        word=s;
        if(stlist!=null) this.stlist=stlist;
        this.lv=lv;
        this.pb=pb;
    }

    @Override
    protected void onPreExecute() {
        lv.setVisibility(View.INVISIBLE);
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    protected Object doInBackground(String... params) {

        //тут получаем json

        if(stlist.size()==0)
        try {
            URL url = new URL(params[0]);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

            // читаем ответ сервера и кладём его в строку
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            //тут json парсим
            try {

                JSONArray root=new JSONArray(stringBuilder.toString());
                for(int i=0;i<root.length();i++){
                    if(!root.getJSONObject(i).getJSONObject("meta").getString("id").contains(word)) continue;

                    if(root.getJSONObject(i).has("def")) {
                        JSONArray def = root.getJSONObject(i).getJSONArray("def");
                        for (int j = 0; j < def.length(); j++) {
                            JSONArray sseq = def.getJSONObject(j).getJSONArray("sseq");
                            for (int q = 0; q < sseq.length(); q++) {
                                StringTranslation item=new StringTranslation();

                                item.index=stlist.size()+1;
                                item.meaning="";
                                item.syns="";
                                item.ex="";

                                JSONArray sseq2 = sseq.getJSONArray(q);
                                for (int w = 0; w < sseq2.length(); w++) {
                                    JSONObject sseq3 = sseq2.getJSONArray(w).getJSONObject(1);
                                    if (sseq3.has("dt")) {
                                        JSONArray dt = sseq3.getJSONArray("dt");
                                        for (int e = 0; e < dt.length(); e++) {
                                            JSONArray inner = dt.getJSONArray(e);
                                            if (inner.getString(0).equals("text")) {
                                                String bc= inner.getString(1).substring(4);
                                                String without_bc=bc.replaceAll("\\{bc\\}","<br/>")+ "<br/>";;//cutTag(bc,"{bc}","{bc}","<br/>","")+ "<br/>";
                                                without_bc=without_bc.replaceAll("\\{it\\}","<b>").replaceAll("\\{/it\\}","</b>");
                                                without_bc=without_bc.replaceAll("\\{dxt\\|","<b>");
                                                without_bc=without_bc.replaceAll("\\|\\|\\}\\{/dx\\}}","</b>");
                                                item.meaning+=without_bc;
                                            } else if (inner.getString(0).equals("vis")) {
                                                JSONArray vises = inner.getJSONArray(1);
                                                for (int y = 0; y < vises.length(); y++) {
                                                    String current = vises.getJSONObject(y).getString("t") +"<br/>";
                                                    current=current.replaceAll("\\{it\\}","<b>").replaceAll("\\{/it\\}","</b>"); //cutTag(current,"{it}","{/it}","<b>","</b>");
                                                    current=current.replaceAll("\\{ldquo\\}","<br/>-").replaceAll("\\{rdquo\\}","");//cutTag(bold,"{ldquo}","{rdquo}","<br/>-","");
                                                    current=current.replaceAll("\\{bc\\}","<br/>");//cutTag(quote,"{bc}","{bc}","<br/>","");
                                                    current=current.replaceAll("\\{phrase\\}","<b>").replaceAll("\\{/phrase\\}","</b>");
                                                    item.ex += current;
                                                    // builder += vises.getJSONObject(y).getString("t") + '\n';
                                                }

                                            }
                                        }

                                    }
                                }
                                if(item.meaning!=null&&!item.meaning.equals("")) stlist.add(item);
                            }

                        }

                    }

                }



                //  tv.setText(Html.fromHtml(builder));

            } catch (JSONException e) {
                e.printStackTrace();
            }






        }
        catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return stlist;
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        if(stlist.size()==0){
            StringTranslation fail=new StringTranslation();
            fail.meaning="";
            stlist.add(fail);
        }
        TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), stlist.toArray(new StringTranslation[stlist.size()]),true);
        lv.setAdapter(adapter);
        pb.setVisibility(View.INVISIBLE);
        lv.setVisibility(View.VISIBLE);
    }
}
//для фразовых глаголов и идиом
    public class MerriamWebsterApiPhrasVAndIdioms extends AsyncTask<String, Integer, Object> {
        String word;

        ArrayList<String> word_list=new ArrayList<>();
        HashMap<String, ArrayList<StringTranslation>> map=new HashMap<>();
        ListView lv;
        ProgressBar pb;
        public MerriamWebsterApiPhrasVAndIdioms(String s,  ArrayList<String> word_list,HashMap<String, ArrayList<StringTranslation>> map, ListView lv, ProgressBar pb){
            word=s;
            if(map!=null) this.map=map;
            if(word_list!=null) this.word_list=word_list;
            this.lv=lv;
            this.pb=pb;
        }

        @Override
        protected void onPreExecute() {
            lv.setVisibility(View.INVISIBLE);
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(String... params) {

            //тут получаем json

            if(map.size()==0)
                try {
                    URL url = new URL(params[0]);
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

                    // читаем ответ сервера и кладём его в строку
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                    }

                    //тут json парсим
                    try {

                        JSONArray root=new JSONArray(stringBuilder.toString());
                        for(int i=0;i<root.length();i++){
                            if(!root.getJSONObject(i).getJSONObject("meta").getString("id").contains(word)) continue;
                    if(root.getJSONObject(i).has("dros")){


                        JSONArray obj=root.getJSONObject(i).getJSONArray("dros");
                        for(int j=0;j<obj.length();j++) {

                            JSONObject def = obj.getJSONObject(j);

                            String word=def.getString("drp");
                            ArrayList<StringTranslation> stlist=new ArrayList<>();
                            word_list.add(word);
                            JSONArray subdef = def.getJSONArray("def");
                            for (int q = 0; q < subdef.length(); q++) {
                                JSONObject mean = subdef.getJSONObject(q);
                                JSONArray sseq = mean.getJSONArray("sseq");
                                for (int w = 0; w < sseq.length(); w++) {
                                    StringTranslation it=new StringTranslation();
                                    it.meaning="";
                                    it.syns="";
                                    it.ex="";
                                    it.word=word;
                                    JSONArray sseq1 = sseq.getJSONArray(w);
                                    for (int e = 0; e < sseq1.length(); e++) {
                                        JSONArray sseq2 = sseq1.getJSONArray(e);

                                        JSONObject sense = sseq2.getJSONObject(1);

                                        JSONArray phrasev = null;
                                        if (sense.has("phrasev")) {
                                            phrasev = sense.getJSONArray("phrasev");
                                            for (int t = 0; t < phrasev.length(); t++) {
                                                it.word="";
                                                if (phrasev.getJSONObject(t).has("pvl"))
                                                    it.word+= phrasev.getJSONObject(t).getString("pvl") + " ";
                                                it.word += phrasev.getJSONObject(t).getString("pva") + " ";

                                            }
                                            //item.meaning += "<br/>"+"<br/>";
                                        }
                                        if (sense.has("dt")) {
                                            phrasev = sense.getJSONArray("dt");
                                            for (int t = 0; t < phrasev.length(); t++) {
                                                JSONArray dt = phrasev.getJSONArray(t);
                                                if (dt.getString(0).equals("text")) {
                                                    it.meaning += dt.getString(1).substring(4) +"<br/>"+"<br/>";
                                                } else if (dt.getString(0).equals("vis")) {
                                                    JSONArray vises = dt.getJSONArray(1);
                                                    for (int y = 0; y < vises.length(); y++) {
                                                        String current= vises.getJSONObject(y).getString("t")+"<br/>";

                                                        current=current.replaceAll("\\{it\\}","<b>").replaceAll("\\{/it\\}","</b>"); //cutTag(current,"{it}","{/it}","<b>","</b>");
                                                        current=current.replaceAll("\\{ldquo\\}","<br/>-").replaceAll("\\{rdquo\\}","");//cutTag(bold,"{ldquo}","{rdquo}","<br/>-","");
                                                        current=current.replaceAll("\\{dxt\\|","<b>");
                                                        current=current.replaceAll("\\|\\|\\}\\{/dx\\}}","</b>");
                                                        it.ex+=current;

                                                    }
                                                    //item+="<br/>";
                                                }else if(dt.getString(0).equals("uns")){
                                                    JSONArray unses = dt.getJSONArray(1);
                                                    for (int y = 0; y < unses.length(); y++) {
                                                        JSONArray unses1=unses.getJSONArray(y);
                                                        for(int u=0;u<unses1.length();u++){
                                                            JSONArray dt1 = unses1.getJSONArray(u);
                                                            if (dt1.getString(0).equals("text")) {
                                                                it.meaning += dt1.getString(1) +"<br/>"+"<br/>";
                                                            } else if (dt1.getString(0).equals("vis")) {
                                                                JSONArray vises = dt1.getJSONArray(1);
                                                                for (int o = 0; o < vises.length(); o++) {
                                                                    String current = vises.getJSONObject(o).getString("t") + "<br/>";
                                                                    current=current.replaceAll("\\{it\\}","<b>").replaceAll("\\{/it\\}","</b>"); //cutTag(current,"{it}","{/it}","<b>","</b>");
                                                                    current=current.replaceAll("\\{ldquo\\}","<br/>-").replaceAll("\\{rdquo\\}","");//cutTag(bold,"{ldquo}","{rdquo}","<br/>-","");
                                                                    current=current.replaceAll("\\{bc\\}","<br/>");//cutTag(quote,"{bc}","{bc}","<br/>","");
                                                                    current=current.replaceAll("\\{phrase\\}","<b>").replaceAll("\\{/phrase\\}","</b>");
                                                                    it.ex+=current;
                                                                    // builder += vises.getJSONObject(y).getString("t") + '\n';
                                                                }
                                                               // builder += "<br/>";
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                        }

                                    }
                                    stlist.add(it);
                                }
                            }
                            //stlist.add(item);
                            map.put(word,stlist);
                        }

                    }


                        }



                        //  tv.setText(Html.fromHtml(builder));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }






                }
                catch (Exception e) {
                    e.printStackTrace();
                    return e.toString();
                }
            return map;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if(map.size()==0){
                //устанавливаем переводы на листвью
                StringTranslation fail=new StringTranslation();
                fail.meaning="";
                ArrayList<StringTranslation> stlist=new ArrayList<>();
                stlist.add(fail);
                TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), stlist.toArray(new StringTranslation[stlist.size()]),true,true);
                lv.setAdapter(adapter);
            }else{
                StringAdapter adapter=new StringAdapter(getApplicationContext(),word_list.toArray(new String[word_list.size()]));
                lv.setAdapter(adapter);
            }

            pb.setVisibility(View.INVISIBLE);
            lv.setVisibility(View.VISIBLE);
        }
    }
    public String requestBuilder(String word, String api_key){
        String request="https://www.dictionaryapi.com/api/v3/references/learners/json/"+word+"?key="+api_key;
        return request;
    }



}
