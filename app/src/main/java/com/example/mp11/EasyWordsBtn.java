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

import com.example.mp11.ForDatabases.DbHelper;
import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.views.TranslationAdapter;
import com.example.mp11.views.TranslationItem;
import com.example.mp11.yandex.dictslate.Model;
import com.example.mp11.yandex.dictslate.RestApi;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EasyWordsBtn extends Service implements View.OnTouchListener, View.OnClickListener{

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
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);
        Toast.makeText(this,"onTaskRemoved",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onCreate() {
        super.onCreate();
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
        getReport(message);

    }
    class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener
    {
        public void onPrimaryClipChanged()
        {
            ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);

            // Gets the clipboard as text.
            message = item.getText().toString();
            overlayedButton.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(),"onPrimaryClip",Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    overlayedButton.setVisibility(View.INVISIBLE);

                }
            }, 10000);


        }
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
                final MyDbHelper databaseHelper = new MyDbHelper(getApplicationContext());


                String meanings="";
                String syns="(";
                String ex="";

                final ArrayList<String> defmean=new ArrayList<>();
                final ArrayList<String> defsyns=new ArrayList<>();
                final ArrayList<String> defex=new ArrayList<>();

                final ArrayList<TranslationItem> result = new ArrayList<>();
                String transcription="";
                for (int i = 0; i < response.body().def.length; i++) {
                    TranslationItem item = new TranslationItem();

                    for (int j = 0; j < response.body().def[i].tr.length; j++) {

                        Model.Def.Tr cur = response.body().def[i].tr[j];
                        item.meanings.add(cur.text);
                        meanings+=cur.text + ", ";
                        if(cur.syn!=null)
                            for (Model.Def.Tr.Syn a : cur.syn) {
                                item.meanings.add(a.text);
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
                                    item.syn.add(now);
                                    syns+=now+", ";
                                }
                                else{
                                    item.meanings.add(now);
                                    meanings+=now+", ";
                                }

                            }
                        if(cur.ex!=null)
                            for (Model.Def.Ex a : cur.ex) {
                                for (Model.Def.Tr b : a.tr){
                                    item.ex.put(a.text, b.text);
                                    ex+=a.text + " - " + b.text + '\n';
                                }

                            }


                    }
                    transcription="["+response.body().def[i].ts+"]";
                    item.index=i+1;
                    result.add(item);
                    defmean.add(meanings);
                    meanings="";
                    defsyns.add(syns);
                    syns="";
                    defex.add(ex);
                    ex="";



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

                TranslationAdapter adapter = new TranslationAdapter(getApplicationContext(), result.toArray(new TranslationItem[result.size()]));
                lv.setAdapter(adapter);

                Button btn=(Button)view.findViewById(R.id.addToDict_btn);
                final ArrayList<TranslationItem> result1 = result;
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        for(int q=0;q<defmean.size();q++){
                            databaseHelper.addWord(text, defmean.get(q), defsyns.get(q),defex.get(q));
                        }


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
