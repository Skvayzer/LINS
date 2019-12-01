package com.example.mp11.videoplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.ForDictionaries.EasyWordsBtn;
import com.example.mp11.GoogleTranslate;
import com.example.mp11.activities.DynVideoPlayer;
import com.example.mp11.adapters.StringAdapter;
import com.example.mp11.adapters.TranslationAdapter;
import com.example.mp11.fragments.DictionariesFragment;
import com.example.mp11.R;
import com.example.mp11.ForDictionaries.StringTranslation;
import com.example.mp11.ForDictionaries.yandexdictslate.CurrentTranslation;
import com.example.mp11.ForDictionaries.yandexdictslate.Model;
import com.example.mp11.ForDictionaries.yandexdictslate.RestApi;
import com.example.mp11.ForDictionaries.yandexdictslate.TranslateApi;
import com.google.android.exoplayer.util.PlayerControl;
//import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SubtitleView extends android.support.v7.widget.AppCompatTextView implements Runnable {
    //яндекс апи словаря
    private static String DICT_URI_JSON="https://dictionary.yandex.net/";
    private static  String SAMPLE_KEY="dict.1.1.20190329T031256Z.531eac57eaa2f4eb.57b8133578fb75df82180bc54fe6602d41795051";

   
    //чтобы отображать время в субтитрах
    private static final boolean DEBUG = true;
    //интервал между обновлениями в миллисекундах
    public static final int UPDATE_INTERVAL = 300;
    //плеер из активности с видеоплеером
    private PlayerControl player;
    //здесь хранятся английские строки с субтитрами и временем
    private static TreeMap<Long, Line> track;
    //а здесь их русский перевод
  
    //двигается ли? перемещает ли пользоатеь субтитры?
    public boolean isMoving = false;


    public int height, width;

    public long last_position = 0;
    public long cur_position = 0;
    //дельта времени, чтобы при заморозке и разморозке субтитров переносить их по времени на эту дельту
    public int dt = 0;
    //остановлен ли плеер
    public boolean isStopped = false;
    //открыто ли окошко с переводом
    public boolean ispopupWindow = false;
    //включить русские субтитры?
    public boolean rus_mode=false;
    //активность с плеером
    public Activity activity;
    //кнопки паузы и плей
    public ImageButton btn_pause;
    public ImageButton btn_play;
    //layout inflater из активности
    public LayoutInflater inflater;
    //контекст активности
    private Context context;
    public TextToSpeech tts;

    public static boolean eng=false;

    private boolean EngTranslation=false;
    private boolean isPai=false;

    public boolean isWatchAndSpeakMode=false;
    public boolean isReadyToSpeak=false;
    boolean isReadyatAll=false;

    private String lastSub;

    public View fading;

    private String merriam_key="5e710d58-4d0b-4435-a025-d455b7437bfc";
    public View decorView;



    public SpeechRecognizer speech = null;
    public Intent recognizerIntent;

    public ImageButton micro;
    String russianText="";

    public SubtitleView(Context context) {
        super(context);
        this.context=context;

    }


    public SubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    //на касании
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x, y;
        float dX = 0, dY = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //вычисляем дельту координат перемещения субтитров,
                dX = getX() - event.getRawX();
                dY = getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                x = this.getX();
                y = this.getY();
                if (isMoving && x > 100 && x + getWidth() < width - 200 && y > 100 && y + getHeight() < height - 200)
                    animate()
                            .x(event.getX()-dX)
                            .y(event.getY()-dY)
                            .setDuration(0)
                            .start();
                else if (isMoving) animate()
                        .x(event.getRawX() )
                        .y(event.getRawY() )
                        .setDuration(0)
                        .start();

                break;
            case MotionEvent.ACTION_UP:
                //палец подняли субтитры не двигаются
                isMoving = false;
                break;
            default:
                return false;
        }
        return true;
    }
    //повторяется через интервал
    @Override
    public void run() {
        //если есть плеер, сабы не пустые и плеер не остановлен
        if (player != null && track != null && !isStopped) {

                seekForSubs();


        }
        postDelayed(this, UPDATE_INTERVAL);

    }

    private void seekForSubs(){
        //берем позицию на плеере
        int curpos = 0;
        try {
            curpos = player.getCurrentPosition();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        //преобразовываем миллисекунды в секунды
        int seconds = curpos / 1000;
        //устанавливаем время на субтитрах
        setText((DEBUG ? "[" + secondsToDuration(seconds) + "] " : ""));
        //берем текст субтитров с учётом дельты времени при заморозке
        String curS = getTimedText(curpos - dt);
        //некоторые субтитры поставляются с html-тегами, я их уберу
        curS = android.text.Html.fromHtml(curS).toString();
        //if(isWatchAndSpeakMode) rus_parse();

        if(isWatchAndSpeakMode&&isReadyToSpeak&&!curS.equals(lastSub)&&!curS.replaceAll("\\s","").equals("")){
            micro.setVisibility(VISIBLE);
            player.pause();
            speech.startListening(recognizerIntent);
            isStopped=true;
            Animation animFadeIn = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);
            animFadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    fading.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            fading.startAnimation(animFadeIn);

            btn_pause.setVisibility(GONE);
            btn_play.setVisibility(VISIBLE);
            //rus_parse();
            String job=curS.replaceAll("[A-Za-z]", "_");
            append(job);
            //взять текст с переводом
            try {


                    GoogleTranslate googleTranslate = new GoogleTranslate();
                    try {
                        russianText = googleTranslate.execute(curS.replaceAll("\"", ""), "en", "ru").get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }



                    append("\n");
                    //сделать этот текст жёлтым цветом
                    appendColoredText(russianText, Color.YELLOW);
            }catch (Exception e){
                e.printStackTrace();
            }

            Animation animFadeOut = AnimationUtils.loadAnimation(getContext(),R.anim.fade_out);
            animFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    fading.setVisibility(INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            fading.startAnimation(animFadeOut);
            isReadyToSpeak=false;
            isReadyatAll=false;

        }else {


            //разделяю текст по пробелам
            final String[] s = curS.split(" ");
            for (int i = 0; i < s.length; i++) {
                final int e = i;
                //превращаем слова в ссылки
                final String finalCurS = curS;
                final SpannableString link = makeLinkSpan(s[i] + " ", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //удаляем из слова все специальные символы
                        String result_word = s[e].replaceAll("[^a-zA-Z0-9_-]", "");
                        //если субтитры не двигают, то на клике
                        if (!isMoving) {
                            Toast.makeText(getContext(), result_word, Toast.LENGTH_SHORT).show();
                            //остановить плеер
                            if (player.isPlaying()) player.pause();
                            btn_pause.setVisibility(GONE);
                            btn_play.setVisibility(VISIBLE);
                            ispopupWindow = false;
                            //переводим слово во внешнем потоке
                            CallbackTask task = new CallbackTask(finalCurS);
                            task.execute(inflections(result_word));
                        }
                    }
                });
                //добавляем к субтитрам ссылку
                append(link);
            }

            //если включен режим с русскими субтитрами
            if (rus_mode) {
                //взять текст с переводом
                //String rus_cur = getRussianTimedText(curpos - dt);
                if(!curS.equals(lastSub)) {
                    russianText="";
                    if(!curS.replaceAll("\\s","").equals("")) {

                        GoogleTranslate googleTranslate = new GoogleTranslate();
                        try {
                            russianText = googleTranslate.execute(curS.replaceAll("\"", ""), "en", "ru").get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Log.i("rus", "done");

                    }
                }
                //добавить на другой строке перевод
                if(!russianText.equals(""))
                append("\n");
                //сделать этот текст жёлтым цветом
                appendColoredText(russianText, Color.YELLOW);

            }
            //чтобы можно было кликать по ссылкам
            makeLinksFocusable(this);
        }

        if(DynVideoPlayer.gravity_mode){
            if(DynVideoPlayer.shown_controls){
                setY(DynVideoPlayer.height - DynVideoPlayer.bottom_controls.getHeight() - DynVideoPlayer.onlySeekbar.getHeight() - getHeight());
            }else{
                setY(DynVideoPlayer.height-getHeight());
            }
        }
        if(isWatchAndSpeakMode&&!isReadyToSpeak&&!isReadyatAll){
            isReadyatAll=true;
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    isReadyToSpeak=true;

                }
            },60000);
        }
        if(!curS.replaceAll("\\s","").equals(""))
        lastSub=curS;

    }
    public void appendRussian(){

            append("\n");
            //сделать этот текст жёлтым цветом
            appendColoredText(russianText, Color.YELLOW);

    }
    //функция для изменения цвета textview
    private void appendColoredText(String text, int color) {
        int start = getText().length();
        append(text);
        int end = getText().length();
        Spannable spannableText = (Spannable) getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }
    //разделение ссылок и их кликабельность
    private SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
        SpannableString link = new SpannableString(text);
        link.setSpan(new ClickableString(listener), 0, text.length(),
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        return link;
    }
    //для кликабельности ссылок
    private void makeLinksFocusable(TextView tv) {
        MovementMethod m = tv.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (tv.getLinksClickable()) {
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }
    //класс кликабольной строки для субтритров
    private static class ClickableString extends ClickableSpan {
        private View.OnClickListener mListener;
        //прикручиваем листенер на клики
        public ClickableString(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
    }

    //функция для получения текста из субтитров по времени
    private String getTimedText(long currentPosition) {
        String result = "";
        //ищем в мапе по времени нужный текст для субтитров
        for (Map.Entry<Long, Line> entry : track.entrySet()) {
            if (currentPosition < entry.getKey()) break;
            if (currentPosition < entry.getValue().to) result = entry.getValue().text;
        }
        return result;
    }
    // чтобы отображать время в формате 00:00:00
    public String secondsToDuration(int seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600,
                (seconds % 3600) / 60, (seconds % 60), Locale.US);
    }
    //для интервального повторения поиска сабов
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        postDelayed(this, UPDATE_INTERVAL);
    }
    //удаление привязки и интервала
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this);
    }
    //копируем плеер из активности с видеоплеером в субтитры
    public void setPlayer(PlayerControl player) {
        this.player = player;
    }
    //устанавливаем источник субтитров по ссылке
    public void setSubSource(String url, String mime) {
        if (mime.equals(MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP))
            track = getSubtitleFile(url);
        else
            throw new UnsupportedOperationException("Субтитры в формате srt");
    }
    //устанавливаем источник субтитров по uri файла субтитров
    public void setSubSource(Uri uri, String mime,Context context) {
        if (mime.equals(MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP))
            track = getSubtitleFile(uri,context);
        else
            throw new UnsupportedOperationException("Субтитры в формате srt");
    }
    public void setSubSource(int ResID, String mime) {
        if (mime.equals(MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP))
            track = getSubtitleFile(ResID);
        else
            throw new UnsupportedOperationException("Parser only built for SRT subs");
    }

    //парсим субтитры по inputstream
    public static TreeMap<Long, Line> parse(InputStream is) throws IOException {
        LineNumberReader r = new LineNumberReader(new InputStreamReader(is, "UTF-8"));
        //здесь хранятся строки и время субтитров
        TreeMap<Long, Line> track = new TreeMap<>();
        //читаем по строчке
        while ((r.readLine()) != null)  {
            String timeString = r.readLine();
            String lineString = "";
            String s;
            //если строка не пустая, добавляем её в финальную строку
            while (!((s = r.readLine()) == null || s.trim().equals(""))) {
                lineString += s + "\n";
            }
            //берем время начала текста и конца с краёв от символа -->
            long startTime = parse(timeString.split("-->")[0]);
            long endTime = parse(timeString.split("-->")[1]);
            //кладём в treemap с субтитрами
            track.put(startTime, new Line(startTime, endTime, lineString));
        }
        return track;
    }
    //то же самое, но читает по bufferedReader'у, это для субтитров по ссылке
    public static TreeMap<Long, Line> parse(BufferedReader is) throws IOException {
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

        }
        return track;
    }
    //парсим формат времи из текста субтитров и переводим его в миллисекунды
    private static long parse(String in) {
        long hours = Long.parseLong(in.split(":")[0].trim());
        long minutes = Long.parseLong(in.split(":")[1].trim());
        long seconds = Long.parseLong(in.split(":")[2].split(",")[0].trim());
        long millies = Long.parseLong(in.split(":")[2].split(",")[1].trim());

        return hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + millies;

    }
    //получаем файл субтитров по uri файла
    private TreeMap<Long, Line> getSubtitleFile(Uri uri,Context context) {
        InputStream inputStream = null;
        try {
            //открываем поток с файлов по uri
            inputStream = context.getContentResolver().openInputStream(uri);
            //передаем его в функцию парсинга сабов
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
    //получаем файл с субтитрами по ссылке
    private TreeMap<Long, Line> getSubtitleFile(String s)  {
        URL url=null;
        try {
            url=new URL(s);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        InputStream urlConnection=null;
        BufferedReader bufferedReader=null;
        InputStream in=null;
        try {
            //открываем соединение и bufferedReader
            urlConnection = url.openConnection().getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConnection));
            //по нему парсим сабы
            return parse(bufferedReader);

        } catch (IOException e) {
            e.printStackTrace();
        }
         finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    //класс для хранения строки с субтитрами, времени её начала и конца
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
    //апи яндекс словаря
    //почти как обычно, посмотрите комментарии в классе EasyWordsBtn
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
                AlertDialog.Builder pop = new AlertDialog.Builder(getContext());
                final AlertDialog kek=pop.create();
                kek.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                View view=LayoutInflater.from(getContext()).inflate(R.layout.pop_word,null);
                view.setBackgroundColor(Color.WHITE);
                kek.setView(view);
                //список определений слова
                final ListView lv = (ListView) view.findViewById(R.id.word_list);
                final ListView pai_lv=(ListView)view.findViewById(R.id.pai_list);
                final TextView header=(TextView)view.findViewById(R.id.current_word);
                header.setText(text+"\n"+transcription);
                //адаптер для этого списка, передаём туда строковый перевод
                final TranslationAdapter adapter = new TranslationAdapter(getContext(), stringDict.toArray(new StringTranslation[stringDict.size()]),false);
                final Button addToDict=(Button)view.findViewById(R.id.addToDict_btn);

                if(tts!=null){
                    tts.stop();
                    tts.shutdown();
                }
                ImageButton sound_btn=(ImageButton)view.findViewById(R.id.sound_btn);
                tts=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status == TextToSpeech.SUCCESS){
                            int result=tts.setLanguage(Locale.US);
                            if(result==TextToSpeech.LANG_MISSING_DATA ||
                                    result==TextToSpeech.LANG_NOT_SUPPORTED){
                                Toast.makeText(getContext(),"Язык не поддерживается",Toast.LENGTH_SHORT).show();
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
                        AlertDialog.Builder pop = new AlertDialog.Builder(getContext());
                        final AlertDialog lol=pop.create();
                        lol.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                        //recycleview, на котором будут словари
                        LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                                it.ex = android.text.Html.fromHtml(a.ex).toString();
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
                        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));



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

                                            String s=(String)adapterView.getItemAtPosition(i);
                                            pai_eng_list.clear();
                                            isPai=true;
                                            pai_word[0] =s;
                                            addToDict.setVisibility(View.VISIBLE);
                                            pai_eng_list.addAll(map.get(s));
                                            header.setText(s);
                                            TranslationAdapter adapta=new TranslationAdapter(getContext(), true,map.get(s).toArray(new StringTranslation[map.get(s).size()]),false);
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
        private String curS;
        public CallbackTask(String curS){
            this.curS=curS;
        }
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
            TranslationAdapter adapter = new TranslationAdapter(getContext(), stlist.toArray(new StringTranslation[stlist.size()]),true);
            lv.setAdapter(adapter);
            pb.setVisibility(View.INVISIBLE);
            lv.setVisibility(View.VISIBLE);
        }
    }
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
//
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
                                                                // builder += vises.getJSONObject(y).getString("t") + '\n';
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
                StringTranslation fail=new StringTranslation();
                fail.meaning="";
                ArrayList<StringTranslation> stlist=new ArrayList<>();
                stlist.add(fail);
                TranslationAdapter adapter = new TranslationAdapter(getContext(), stlist.toArray(new StringTranslation[stlist.size()]),true,true);
                lv.setAdapter(adapter);
            }else{
                StringAdapter adapter=new StringAdapter(getContext(),word_list.toArray(new String[word_list.size()]));
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