package com.example.mp11.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.DictionariesFragment;
import com.example.mp11.FirebaseDbHelper.FirebaseDbHelper;
import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.PopActivity;
import com.example.mp11.R;
import com.example.mp11.VideoPlayer;
import com.example.mp11.yandex.dictslate.CurrentTranslation;
import com.example.mp11.yandex.dictslate.Model;
import com.example.mp11.yandex.dictslate.RestApi;
import com.example.mp11.yandex.dictslate.TranslateApi;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;


public class SubtitleView extends android.support.v7.widget.AppCompatTextView implements Runnable {
    private static String DICT_URI_JSON = "https://dictionary.yandex.net/";
    private static final String SAMPLE_KEY = "dict.1.1.20190329T031256Z.531eac57eaa2f4eb.57b8133578fb75df82180bc54fe6602d41795051";
    private static String PREDICTOR_URI_JSON = "https://predictor.yandex.net/";
    private static String PREDICTOR_KEY = "pdct.1.1.20190329T025447Z.ddc959b4c0378977.ad7eb16cc17f103f41d07f0ec3709053ba8f98af";

    private static String TRLATE_URI_JSON = "https://translate.yandex.net/";
    private static String TRLATE_KEY = "trnsl.1.1.20190223T194026Z.b1b9b8f5c1c1c647.f16aa370569e624242bde6cb461e7e3d9c155685";

    private static final String TAG = "SubtitleView";
    private static final boolean DEBUG = true;
    private static final int UPDATE_INTERVAL = 300;
    //private MediaPlayer player;
    PlayerControl player;
    private TreeMap<Long, Line> track;

    public boolean isMoving = false;

    public int height, width;

    public long last_position = 0;
    public long cur_position = 0;
    public int dt = 22500;
    public boolean isStopped = false;
    public boolean ispopupWindow = false;

    public Activity activity;
    public ImageButton btn_pause;
    public ImageButton btn_play;

    public LayoutInflater inflater;


    static TreeMap<Long, Line> trlate = new TreeMap<>();
    static String translation;

    public SubtitleView(Context context) {
        super(context);
//        setOnTouchListener(new View.OnTouchListener(){
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                float dX = 0, dY = 0;
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//
//                        dX = getX() - event.getRawX();
//                        dY = getY() - event.getRawY();
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
//
//                        if(isMoving) {
//                            animate()
//                                    .x(event.getRawX() + dX)
//                                    .y(event.getRawY() + dY)
//                                    .setDuration(0)
//                                    .start();
//                        }
//                        break;
//                    case MotionEvent.ACTION_UP:
//                       isMoving=false;
//                        break;
//                    default:
//                        return false;
//
//                }
//
//
//                return true;
//            }
//        });


//        setOnLongClickListener(new View.OnLongClickListener(){
//            @Override
//            public boolean onLongClick(View v) {
//                if(!isMoving){
//                    isMoving=true;
//                }
//                Log.d("Mes","Long");
//                return true;
//            }
//        });


    }


    public SubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x, y;
        float dX = 0, dY = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                dX = getX() - event.getRawX();
                dY = getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                x = this.getX();
                y = this.getY();
                if (isMoving && x > 100 && x + getWidth() < width - 200 && y > 100 && y + getHeight() < height - 200)
                    animate()
                            .x(event.getX() -400)
                            .y(event.getY() -400)
                            .setDuration(0)
                            .start();
                else if (isMoving) animate()
                        .x(event.getRawX() )
                        .y(event.getRawY() )
                        .setDuration(0)
                        .start();
                break;
            case MotionEvent.ACTION_UP:
                isMoving = false;
                break;
            default:

                return false;

        }


        return true;
    }

    @Override
    public void run() {


        if (player != null && track != null && !isStopped) {
            int curpos = 0;
            try {
                curpos = player.getCurrentPosition();

            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            int seconds = curpos / 1000;
            setText((DEBUG ? "[" + secondsToDuration(seconds) + "] " : ""));
            String curS = getTimedText(curpos - dt);
            //String translation;
            curS = android.text.Html.fromHtml(curS).toString();

            final String[] s = curS.split(" ");
            for (int i = 0; i < s.length; i++) {
                final int e = i;
                final SpannableString link = makeLinkSpan(s[i] + " ", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {


                        if (!isMoving) {
                            Toast.makeText(getContext(), s[e], Toast.LENGTH_SHORT).show();
                            if (player.isPlaying()) player.pause();
                            btn_pause.setVisibility(GONE);
                            btn_play.setVisibility(VISIBLE);
                            ispopupWindow = false;
                            Intent i = new Intent(getContext(), PopActivity.class);
                            i.putExtra("word", s[e]);

                            getReport(s[e]);
                            //showPop();

                            //getContext().startActivity(i);
                        }


                    }
                });
                // + getTimedText(player.getCurrentPosition()));
                append(link);

            }
            // append("\n"+translation);
            makeLinksFocusable(this);
            //if(curS!=null && !curS.equals("")) translate(curS,this);
        }
        postDelayed(this, UPDATE_INTERVAL);

    }

    private SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
        SpannableString link = new SpannableString(text);
        link.setSpan(new ClickableString(listener), 0, text.length(),
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        return link;
    }

    private void makeLinksFocusable(TextView tv) {
        MovementMethod m = tv.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (tv.getLinksClickable()) {
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    private static class ClickableString extends ClickableSpan {
        private View.OnClickListener mListener;

        public ClickableString(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
    }

    private String getTimedText(long currentPosition) {
        String result = "";
        for (Map.Entry<Long, Line> entry : track.entrySet()) {
            if (currentPosition < entry.getKey()) break;
            if (currentPosition < entry.getValue().to) result = entry.getValue().text;
        }
//        if(result!=null && !result.equals(" ")) {
//            translate(result);
//        }

        return result;
    }

    // To display the seconds in the duration format 00:00:00
    public String secondsToDuration(int seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600,
                (seconds % 3600) / 60, (seconds % 60), Locale.US);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        postDelayed(this, 300);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this);
    }

    //    public void setPlayer(MediaPlayer player) {
//        this.player = player;
//    }
    public void setPlayer(PlayerControl player) {
        this.player = player;

    }

    public void setSubSource(int ResID, String mime) {
        if (mime.equals(MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP))
            track = getSubtitleFile(ResID);
        else
            throw new UnsupportedOperationException("Parser only built for SRT subs");
    }
    public void setSubSource(Uri uri, String mime) {
        if (mime.equals(MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP))
            track = getSubtitleFile(uri);
        else
            throw new UnsupportedOperationException("Parser only built for SRT subs");
    }

    public static TreeMap<Long, Line> parse(InputStream is) throws IOException {
        LineNumberReader r = new LineNumberReader(new InputStreamReader(is, "UTF-8"));
        TreeMap<Long, Line> track = new TreeMap<>();

        while ((r.readLine()) != null) /*Read cue number*/ {
            String timeString = r.readLine();
            String lineString = "";
            String s;
            while (!((s = r.readLine()) == null || s.trim().equals(""))) {
                lineString += s + "\n";
            }
            long startTime = parse(timeString.split("-->")[0]);
            long endTime = parse(timeString.split("-->")[1]);
            track.put(startTime, new Line(startTime, endTime, lineString));
            //if(lineString!=null&&lineString!="") translate(lineString);
            //trlate.put(startTime,new Line(startTime,endTime,translation));
        }
        return track;
    }
    public static TreeMap<Long, Line> parse(InputStreamReader is) throws IOException {
        LineNumberReader r = new LineNumberReader(is);
        TreeMap<Long, Line> track = new TreeMap<>();

        while ((r.readLine()) != null) /*Read cue number*/ {
            String timeString = r.readLine();
            String lineString = "";
            String s;
            while (!((s = r.readLine()) == null || s.trim().equals(""))) {
                lineString += s + "\n";
            }
            long startTime = parse(timeString.split("-->")[0]);
            long endTime = parse(timeString.split("-->")[1]);
            track.put(startTime, new Line(startTime, endTime, lineString));
            //if(lineString!=null&&lineString!="") translate(lineString);
            //trlate.put(startTime,new Line(startTime,endTime,translation));
        }
        return track;
    }
    private static long parse(String in) {
        long hours = Long.parseLong(in.split(":")[0].trim());
        long minutes = Long.parseLong(in.split(":")[1].trim());
        long seconds = Long.parseLong(in.split(":")[2].split(",")[0].trim());
        long millies = Long.parseLong(in.split(":")[2].split(",")[1].trim());

        return hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + millies;

    }

    private TreeMap<Long, Line> getSubtitleFile(int resId) {
        InputStream inputStream = null;
        try {
            inputStream = getResources().openRawResource(resId);

            return parse(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    private TreeMap<Long, Line> getSubtitleFile(Uri uri)  {

        File file=new File(uri.toString());
        FileInputStream fileInputStream=null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        InputStream inputStream=null;
        try {
            inputStream = fileInputStream;
            return parse(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    public static class Line {
        long from;
        long to;
        String text;


        public Line(long from, long to, String text) {
            this.from = from;
            this.to = to;
            this.text = text;
        }
    }


    public void showPop() {
        AlertDialog.Builder pop = new AlertDialog.Builder(getContext());

        View view = inflater.inflate(R.layout.pop_word, null);

        pop.setView(view);
        pop.show();
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

                //final MyDbHelper databaseHelper = new MyDbHelper(getContext(), "TED");
                String meanings="";
                String syns="(";
                String ex="";

                final ArrayList<String> defmean=new ArrayList<>();
                final ArrayList<String> defsyns=new ArrayList<>();
                final ArrayList<String> defex=new ArrayList<>();
                //ArrayList<TranslationItem> result = new ArrayList<>();
                final ArrayList<StringTranslation> stringDict=new ArrayList<>();
                String transcription="";
                //String strmeanings="",strex="",strsyns="";
                for (int i = 0; i < response.body().def.length; i++) {
                    StringTranslation item = new StringTranslation();

                    for (int j = 0; j < response.body().def[i].tr.length; j++) {

                        Model.Def.Tr cur = response.body().def[i].tr[j];
                       // item.meanings.add(cur.text);
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
                           else {
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
                    defmean.add(meanings);
                    if(meanings.length()!=0)item.meaning=meanings.substring(0,meanings.length()-2);
                    meanings="";
                    defsyns.add(syns);
                    if(syns.length()!=0) item.syns=syns.substring(0,syns.length()-2);
                    syns="";
                    defex.add(ex);
                    if(ex.length()!=0)item.ex=ex;
                    ex="";
                    stringDict.add(item);

                }
                AlertDialog.Builder pop = new AlertDialog.Builder(getContext());
                final AlertDialog kek=pop.create();
                View view = inflater.inflate(R.layout.pop_word, null);

                kek.setView(view);

                ListView lv = (ListView) view.findViewById(R.id.word_list);
                ((TextView)view.findViewById(R.id.current_word)).setText(text+" "+transcription);

                TranslationAdapter adapter = new TranslationAdapter(getContext(), stringDict.toArray(new StringTranslation[stringDict.size()]));
                lv.setAdapter(adapter);
                Button btn=(Button)view.findViewById(R.id.addToDict_btn);
                btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder pop = new AlertDialog.Builder(getContext());
                        final AlertDialog lol=pop.create();
                        lol.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

//                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                        //View view = inflater.inflate(R.layout.pop_word, null);
                        //View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_word,null);
                        LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                                R.layout.recycler_view, null, false);

                        ArrayList<StringTranslation> stlist=new ArrayList<>();
                        for(int q=0;q<defmean.size();q++){

                            stlist.add(new StringTranslation(text, defmean.get(q), defsyns.get(q),defex.get(q)));
                        }
                        DictionariesFragment.MyContentAdapter adapter = new DictionariesFragment.MyContentAdapter(recyclerView.getContext(),
                                stlist, lol,text);

                        recyclerView.setAdapter(adapter);
                        recyclerView.setHasFixedSize(true);
                        // отступ для плитки
                        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
                        recyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
                        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                        //layoutManager =  ((GridLayoutManager)recyclerView.getLayoutManager());


                        recyclerView.setBackgroundColor(Color.WHITE);
                        lol.setView(recyclerView);
                        lol.show();


//                        for(int q=0;q<defmean.size();q++){
//                            databaseHelper.addWord(text, defmean.get(q), defsyns.get(q),defex.get(q));
//                        }
//
//                        FirebaseDbHelper.AddWord(name,text,stringDict);


                        kek.hide();


                    }
                });

                kek.show();
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


    void translate(String text){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TRLATE_URI_JSON)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TranslateApi service = retrofit.create(TranslateApi.class);
        Call<CurrentTranslation> call = service.translate(TRLATE_KEY, text,"en-ru");
        call.enqueue(new Callback<CurrentTranslation>() {
            @Override
            public void onResponse(Call<CurrentTranslation> call, Response<CurrentTranslation> response) {

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

                String s=response.body().text[0];




                //Toast.makeText(getApplicationContext(),"Вот дерьмо",Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onFailure(Call<CurrentTranslation> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {

                    throwable.printStackTrace();
                }
            }
        });
    }


}