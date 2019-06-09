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
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.R;
import com.example.mp11.fragments.ClickListener;
import com.example.mp11.fragments.DictionariesFragment;
import com.example.mp11.ForDictionaries.googleEmulatorApi.GoogleDictApi;
import com.example.mp11.ForDictionaries.googleEmulatorApi.GoogleTranslation;
import com.example.mp11.adapters.TranslationAdapter;
import com.example.mp11.ForDictionaries.yandexdictslate.Model;
import com.example.mp11.ForDictionaries.yandexdictslate.RestApi;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
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
        //спустя какое-то время
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +1, restartServicePI);
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

    }

    //на тыке
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
        //если включён перевод на английском, перевести на английском(пока отключено в связи с тем,
        //что настоящий гугл словарь упразднён как апи и использовался изначально его нефициальный эмулятор
        //но он работает медленно
        if(eng){
            googleDict(message);
        }else{
            //во внешнем потоке лемматизация слова и перевод
            CallbackTask task=new CallbackTask();
            task.execute(inflections(message));
        }



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
            Toast.makeText(getApplicationContext(),"onPrimaryClip",Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    overlayedButton.setVisibility(View.INVISIBLE);

                }
            }, 10000);


        }
    }
    //перевод эмулятором гугл слова на английском(уже не используется в связи с его медлительностью и
    //багом с парсингом json'a, возникшим накануне
    void googleDict(final String text){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://googledictionaryapi.eu-gb.mybluemix.net")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GoogleDictApi service = retrofit.create(GoogleDictApi.class);
        Call<GoogleTranslation> call = service.define(text);
        call.enqueue(new Callback<GoogleTranslation>() {
            @Override
            public void onResponse(Call<GoogleTranslation> call, Response<GoogleTranslation> response) {

                //String textWord = response.toString();
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

                //тут всё парсится в соответствующие строки
                String meanings="";
                String syns="(";
                String ex="";



                String transcription="["+response.body().phonetic+"]";
                //массив со строковыми переводами, куда всё запихнётся
                final ArrayList<StringTranslation> stlist=new ArrayList<>();
                //если есть определение
                if (response.body().meaning.noun!=null)
                for (int i = 0; i < response.body().meaning.noun.length; i++) {
                    //если существительное, спарсить
                    StringTranslation item=new StringTranslation();

                    //берём определения, синонимы, примеры
                    meanings=response.body().meaning.noun[i].definition;
                    ex=response.body().meaning.noun[i].example;
                    if(response.body().meaning.noun[i].synonyms!=null) {
                        for (String s : response.body().meaning.noun[i].synonyms) syns += s + ", ";
                        syns = syns.substring(0, syns.length() - 2) + ")";
                    }
                    item.word=response.body().word;

                    if(meanings.length()!=0)item.meaning=meanings;
                    meanings="";

                    if(syns.length()!=0) item.syns=syns;
                    syns="";

                    if(ex.length()!=0)item.ex=ex;
                    ex="";
                    //добавляем
                    stlist.add(item);


                }
                //если прилагательное есть, сделать то же самое
                if (response.body().meaning.adjective!=null)
                for (int i = 0; i < response.body().meaning.adjective.length; i++) {

                    StringTranslation item=new StringTranslation();
                    meanings=response.body().meaning.adjective[i].definition;
                    ex=response.body().meaning.adjective[i].example;
                    if(response.body().meaning.adjective[i].synonyms!=null) {
                        for (String s : response.body().meaning.adjective[i].synonyms)
                            syns += s + ", ";
                        syns = syns.substring(0, syns.length() - 2) + ")";
                    }
                    item.word=response.body().word;

                    if(meanings.length()!=0)item.meaning=meanings;
                    meanings="";

                    if(syns.length()!=0) item.syns=syns;
                    syns="";

                    if(ex.length()!=0)item.ex=ex;
                    ex="";
                    stlist.add(item);


                }
                //если есть глагол, то же самое
                if (response.body().meaning.verb!=null)
                for (int i = 0; i < response.body().meaning.verb.length; i++) {

                    StringTranslation item=new StringTranslation();
                    meanings=response.body().meaning.verb[i].definition;
                    ex=response.body().meaning.verb[i].example;
                    if(response.body().meaning.verb[i].synonyms!=null) {
                        for (String s : response.body().meaning.verb[i].synonyms) syns += s + ", ";
                        syns = syns.substring(0, syns.length() - 2) + ")";
                    }

                    item.word=response.body().word;

                    if(meanings.length()!=0)item.meaning=meanings;
                    meanings="";

                    if(syns.length()!=0) item.syns=syns;
                    syns="";

                    if(ex.length()!=0)item.ex=ex;
                    ex="";
                    stlist.add(item);


                }
                //если наречие, то же самое
                if (response.body().meaning.adverb!=null)
                for (int i = 0; i < response.body().meaning.adverb.length; i++) {

                    StringTranslation item=new StringTranslation();
                    meanings=response.body().meaning.adverb[i].definition;
                    ex=response.body().meaning.adverb[i].example;
                    if(response.body().meaning.adverb[i].synonyms!=null) {
                        for (String s : response.body().meaning.adverb[i].synonyms)
                            syns += s + ", ";
                        syns = syns.substring(0, syns.length() - 2) + ")";
                    }
                    item.word=response.body().word;

                    if(meanings.length()!=0)item.meaning=meanings;
                    meanings="";

                    if(syns.length()!=0) item.syns=syns;
                    syns="";

                    if(ex.length()!=0)item.ex=ex;
                    ex="";
                    stlist.add(item);


                }
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
                ((TextView)view.findViewById(R.id.current_word)).setText(text+" "+transcription);
                TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), stlist.toArray(new StringTranslation[stlist.size()]));
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

            @Override
            public void onFailure(Call<GoogleTranslation> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {

                    throwable.printStackTrace();
                }
            }
        });
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
                        item.syns=syns;
                    }
                    defsyns.add(syns);
                    syns="";

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
                ListView lv = (ListView) view.findViewById(R.id.word_list);
                ((TextView)view.findViewById(R.id.current_word)).setText(text+" "+transcription);
                //адаптер для этого списка, передаём туда строковый перевод
                TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), stringDict.toArray(new StringTranslation[stringDict.size()]));
                lv.setAdapter(adapter);

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

                        ArrayList<StringTranslation> stlist=new ArrayList<>();
                        for(int q=0;q<defmean.size();q++){
                            //кладём строковые переводы в массив
                            stlist.add(new StringTranslation(text.toLowerCase(), defmean.get(q), defsyns.get(q)+")",defex.get(q)));
                        }
                        //адаптер для словарей
                        DictionariesFragment.MyContentAdapter adapter = new DictionariesFragment.MyContentAdapter(recyclerView.getContext(),
                                stlist, lol,text);

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
            }
        }
    }
}
