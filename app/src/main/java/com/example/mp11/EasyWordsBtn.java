package com.example.mp11;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.FirebaseDbHelper.FirebaseDbHelper;
import com.example.mp11.ForDatabases.DbHelper;
import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.googleEmulatorApi.GoogleDictApi;
import com.example.mp11.googleEmulatorApi.GoogleTranslation;
import com.example.mp11.views.StringTranslation;
import com.example.mp11.views.TranslationAdapter;
import com.example.mp11.views.TranslationItem;
import com.example.mp11.yandex.dictslate.Model;
import com.example.mp11.yandex.dictslate.RestApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.Inflater;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

    private static String DICT_URI_JSON = "https://dictionary.yandex.net/";
    private static final String SAMPLE_KEY = "dict.1.1.20190329T031256Z.531eac57eaa2f4eb.57b8133578fb75df82180bc54fe6602d41795051";
    private static String PREDICTOR_URI_JSON = "https://predictor.yandex.net/";
    private static String PREDICTOR_KEY = "pdct.1.1.20190329T025447Z.ddc959b4c0378977.ad7eb16cc17f103f41d07f0ec3709053ba8f98af";

    private static String TRLATE_URI_JSON = "https://translate.yandex.net/";
    private static String TRLATE_KEY = "trnsl.1.1.20190223T194026Z.b1b9b8f5c1c1c647.f16aa370569e624242bde6cb461e7e3d9c155685";
    public static boolean eng=false;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Toast.makeText(this,"onStartCommand",Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android


        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +10, restartServicePI);
        Toast.makeText(this,"onTaskRemoved",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this,"Service created",Toast.LENGTH_LONG).show();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        overlayedButton = new ImageButton(this);
        overlayedButton.setVisibility(View.INVISIBLE);

        overlayedButton.setOnTouchListener(this);
        Bitmap img= BitmapFactory.decodeResource(getResources(),R.drawable.curious);
        overlayedButton.setImageBitmap(img);
        overlayedButton.setBackgroundColor(Color.TRANSPARENT);
        //overlayedButton.setBackgroundColor(Color.argb(180,50,120,255));
        overlayedButton.setOnClickListener(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        wm.addView(overlayedButton, params);

        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 0;
        topLeftParams.height = 0;
        wm.addView(topLeftView, topLeftParams);

        clipBoard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener( new ClipboardListener() );
    }

    @Override
    public void onDestroy() {
        // super.onDestroy();
//        if (overlayedButton != null) {
//            wm.removeView(overlayedButton);
//            wm.removeView(topLeftView);
//            overlayedButton = null;
//            topLeftView = null;
//        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            overlayedButton.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int[] topLeftLocationOnScreen = new int[2];
            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

            System.out.println("topLeftY="+topLeftLocationOnScreen[1]);
            System.out.println("originalY="+originalYPos);

            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayedButton.getLayoutParams();

            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                return false;
            }

            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);

            wm.updateViewLayout(overlayedButton, params);
            moving = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (moving) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if(eng){
            googleDict(message);
        }else{
            getReport(message);
        }

        //googleDict(message);

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

    class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener
    {
        public void onPrimaryClipChanged()
        {
            ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);

            // Gets the clipboard as text.
            try {
                message = item.getText().toString();
            }catch (NullPointerException e){
                message="Что-то пошло не так";
            }
            overlayedButton.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(),"onPrimaryClip",Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    overlayedButton.setVisibility(View.INVISIBLE);

                }
            }, 10000);


        }
    }
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



                String meanings="";
                String syns="(";
                String ex="";

                final ArrayList<String> defmean=new ArrayList<>();
                final ArrayList<String> defsyns=new ArrayList<>();
                final ArrayList<String> defex=new ArrayList<>();

                //final ArrayList<TranslationItem> result = new ArrayList<>();
               // final ArrayList<StringTranslation> stringDict=new ArrayList<>();
                String transcription="["+response.body().phonetic[0]+"]";
                final ArrayList<StringTranslation> stlist=new ArrayList<>();
                // String strmeanings="",strex="",strsyns="";
                if (response.body().meaning.noun!=null)
                for (int i = 0; i < response.body().meaning.noun.length; i++) {

                    StringTranslation item=new StringTranslation();


                    //item.meanings.add(cur.text);
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
                    stlist.add(item);


                }
                if (response.body().meaning.adjective!=null)
                for (int i = 0; i < response.body().meaning.adjective.length; i++) {

                    StringTranslation item=new StringTranslation();


                    //item.meanings.add(cur.text);
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
                if (response.body().meaning.verb!=null)
                for (int i = 0; i < response.body().meaning.verb.length; i++) {

                    StringTranslation item=new StringTranslation();


                    //item.meanings.add(cur.text);
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
                if (response.body().meaning.adverb!=null)
                for (int i = 0; i < response.body().meaning.adverb.length; i++) {

                    StringTranslation item=new StringTranslation();


                    //item.meanings.add(cur.text);
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
//                final String meanin=meanings;
//                final String synin=syns;
//                final String exin=ex;

                AlertDialog.Builder pop = new AlertDialog.Builder(getApplicationContext());
                final AlertDialog kek=pop.create();
                kek.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

//                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                //View view = inflater.inflate(R.layout.pop_word, null);
                View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_word,null);

                view.setBackgroundColor(Color.WHITE);

                kek.setView(view);

                ListView lv = (ListView) view.findViewById(R.id.word_list);
                ((TextView)view.findViewById(R.id.current_word)).setText(text+" "+transcription);


//                strmeanings=strmeanings.substring(0,strmeanings.length()-2);
//                strex=strex.substring(0,strex.length()-2);
//                strsyns=strsyns.substring(0,strsyns.length()-2);
                //TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), result.toArray(new TranslationItem[result.size()]));
                TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), stlist.toArray(new StringTranslation[stlist.size()]));
                lv.setAdapter(adapter);


                //myRef.setValue("TEST MESSAGE");
                Button btn=(Button)view.findViewById(R.id.addToDict_btn);
                //final ArrayList<TranslationItem> result1 = result;
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder pop = new AlertDialog.Builder(getApplicationContext());
                        final AlertDialog lol=pop.create();
                        lol.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

//                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                        //View view = inflater.inflate(R.layout.pop_word, null);
                        //View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_word,null);
                        LayoutInflater inflater = (LayoutInflater)   getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                                R.layout.recycler_view, null, false);



                        DictionariesFragment.MyContentAdapter adapter = new DictionariesFragment.MyContentAdapter(recyclerView.getContext(),
                                stlist, lol,text);
                        //FirebaseDbHelper.AddWord(text,stlist);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setHasFixedSize(true);
                        // отступ для плитки
                        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
                        recyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
                        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
                        //layoutManager =  ((GridLayoutManager)recyclerView.getLayoutManager());


                        recyclerView.setBackgroundColor(Color.WHITE);
                        lol.setView(recyclerView);
                        lol.show();
//                        MyDbHelper databaseHelper = new MyDbHelper(getApplicationContext(), "TED");
//                        for(int q=0;q<defmean.size();q++){
//
//                            databaseHelper.addWord(text, defmean.get(q), defsyns.get(q),defex.get(q));
//                        }
//
//
//                        FirebaseDbHelper.AddWord(text,stringDict);




                        kek.hide();
                    }
                });
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

//                WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
//                params.gravity = Gravity.CENTER;
//                params.x = 500;
//                params.y = 500;
//                wm.addView(view,params);
                //Toast.makeText(getApplicationContext(),"Вот дерьмо",Toast.LENGTH_SHORT).show();

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

    void getReport(final String text) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DICT_URI_JSON)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi service = retrofit.create(RestApi.class);
        Call<Model> call = service.lookup(SAMPLE_KEY, "en-ru", text);
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {

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



                String meanings="";
                String syns="(";
                String ex="";

                final ArrayList<String> defmean=new ArrayList<>();
                final ArrayList<String> defsyns=new ArrayList<>();
                final ArrayList<String> defex=new ArrayList<>();

                //final ArrayList<TranslationItem> result = new ArrayList<>();
                final ArrayList<StringTranslation> stringDict=new ArrayList<>();
                String transcription="";
               // String strmeanings="",strex="",strsyns="";
                for (int i = 0; i < response.body().def.length; i++) {
                    syns="(";
                    //TranslationItem item = new TranslationItem();
                    StringTranslation item=new StringTranslation();

                    for (int j = 0; j < response.body().def[i].tr.length; j++) {

                        Model.Def.Tr cur = response.body().def[i].tr[j];
                        //item.meanings.add(cur.text);
                        meanings+=cur.text + ", ";
                        if(cur.syn!=null)
                            for (Model.Def.Tr.Syn a : cur.syn) {
                               // item.meanings.add(a.text);
                                meanings+=a.text+", ";
                            }
                        if(cur.mean!=null)
                            for (Model.Def.Tr.Mean a : cur.mean) {
                                boolean isEnglish = true;
                                String now=a.text;
                                for ( char c : now.toCharArray() ) {
                                    if ( Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN ) {
                                        isEnglish = false;
                                        break;
                                    }
                                }
                                if(isEnglish){
                                    //item.syn.add(now);
                                    syns+=now+", ";
                                }
                                else{
                                    //item.meanings.add(now);
                                    meanings+=now+", ";
                                }

                            }
                        if(cur.ex!=null)
                            for (Model.Def.Ex a : cur.ex) {
                                for (Model.Def.Tr b : a.tr){
                                    //item.ex.put(a.text, b.text);
                                    ex+=a.text + " - " + b.text + '\n';
                                }

                            }


                    }
                    transcription="["+response.body().def[i].ts+"]";
                    item.index=i+1;
                    item.word=text;
                    //result.add(item);

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
//                final String meanin=meanings;
//                final String synin=syns;
//                final String exin=ex;

                AlertDialog.Builder pop = new AlertDialog.Builder(getApplicationContext());
                final AlertDialog kek=pop.create();
                kek.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

//                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                //View view = inflater.inflate(R.layout.pop_word, null);
                View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_word,null);

                view.setBackgroundColor(Color.WHITE);

                kek.setView(view);

                ListView lv = (ListView) view.findViewById(R.id.word_list);
                ((TextView)view.findViewById(R.id.current_word)).setText(text+" "+transcription);

//                strmeanings=strmeanings.substring(0,strmeanings.length()-2);
//                strex=strex.substring(0,strex.length()-2);
//                strsyns=strsyns.substring(0,strsyns.length()-2);
                //TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), result.toArray(new TranslationItem[result.size()]));
                TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), stringDict.toArray(new StringTranslation[stringDict.size()]));
                lv.setAdapter(adapter);


                //myRef.setValue("TEST MESSAGE");
                Button btn=(Button)view.findViewById(R.id.addToDict_btn);
                //final ArrayList<TranslationItem> result1 = result;
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder pop = new AlertDialog.Builder(getApplicationContext());
                        final AlertDialog lol=pop.create();
                        lol.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

//                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                        //View view = inflater.inflate(R.layout.pop_word, null);
                        //View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_word,null);
                        LayoutInflater inflater = (LayoutInflater)   getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                                R.layout.recycler_view, null, false);

                        ArrayList<StringTranslation> stlist=new ArrayList<>();
                        for(int q=0;q<defmean.size();q++){

                            stlist.add(new StringTranslation(text, defmean.get(q), defsyns.get(q),defex.get(q)));
                        }
                        DictionariesFragment.MyContentAdapter adapter = new DictionariesFragment.MyContentAdapter(recyclerView.getContext(),
                                stlist, lol,text);
                       // FirebaseDbHelper.AddWord(text,stlist);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setHasFixedSize(true);
                        // отступ для плитки
                        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
                        recyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
                        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
                        //layoutManager =  ((GridLayoutManager)recyclerView.getLayoutManager());


                        recyclerView.setBackgroundColor(Color.WHITE);
                        lol.setView(recyclerView);
                        lol.show();
//                        MyDbHelper databaseHelper = new MyDbHelper(getApplicationContext(), "TED");
//                        for(int q=0;q<defmean.size();q++){
//
//                            databaseHelper.addWord(text, defmean.get(q), defsyns.get(q),defex.get(q));
//                        }
//
//
//                        FirebaseDbHelper.AddWord(text,stringDict);




                        kek.hide();
                    }
                });

                  kek.show();

//                WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
//                params.gravity = Gravity.CENTER;
//                params.x = 500;
//                params.y = 500;
//                wm.addView(view,params);
                //Toast.makeText(getApplicationContext(),"Вот дерьмо",Toast.LENGTH_SHORT).show();

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
}
