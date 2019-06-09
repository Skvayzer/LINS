package com.example.mp11.videoplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SubtitleView extends android.support.v7.widget.AppCompatTextView implements Runnable {
    //яндекс апи словаря
    private static String DICT_URI_JSON = "https://dictionary.yandex.net/";
    private static final String SAMPLE_KEY = "dict.1.1.20190329T031256Z.531eac57eaa2f4eb.57b8133578fb75df82180bc54fe6602d41795051";

    //яндекс апи переводчика
    private static String TRLATE_URI_JSON = "https://translate.yandex.net/";
    private static String TRLATE_KEY = "trnsl.1.1.20190223T194026Z.b1b9b8f5c1c1c647.f16aa370569e624242bde6cb461e7e3d9c155685";
    //чтобы отображать время в субтитрах
    private static final boolean DEBUG = true;
    //интервал между обновлениями в миллисекундах
    public static final int UPDATE_INTERVAL = 300;
    //плеер из активности с видеоплеером
    private PlayerControl player;
    //здесь хранятся английские строки с субтитрами и временем
    private static TreeMap<Long, Line> track;
    //а здесь их русский перевод
    private static TreeMap<Long,Line> rus_track;
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
                if (isMoving && x > 0 && x + getWidth() < width  && y > 0 && y + getHeight() < height )
                    animate()
                            .x(event.getX()-dX)
                            .y(event.getY()-dY)
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

            //разделяю текст по пробелам
            final String[] s = curS.split(" ");
            for (int i = 0; i < s.length; i++) {
                final int e = i;
                //превращаем слова в ссылки
                final SpannableString link = makeLinkSpan(s[i] + " ", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //удаляем из слова все специальные символы
                        String result_word=s[e].replaceAll("[^a-zA-Z0-9_-]", "");
                        //если субтитры не двигают, то на клике
                        if (!isMoving) {
                            Toast.makeText(getContext(), result_word, Toast.LENGTH_SHORT).show();
                            //остановить плеер
                            if (player.isPlaying()) player.pause();
                            btn_pause.setVisibility(GONE);
                            btn_play.setVisibility(VISIBLE);
                            ispopupWindow = false;
                            //переводим слово во внешнем потоке
                            CallbackTask task=new CallbackTask();
                            task.execute(inflections(result_word));
                        }
                    }
                });
                //добавляем к субтитрам ссылку
                append(link);
            }
            //если включен режим с русскими субтитрами
            if(rus_mode){
                //взять текст с переводом
                String rus_cur=getRussianTimedText(curpos-dt);
                //добавить на другой строке перевод
                if(rus_cur.length()>1) {
                    append("\n" );
                    //сделать этот текст жёлтым цветом
                    appendColoredText(this,rus_cur,Color.YELLOW);
                }
            }
            //чтобы можно было кликать по ссылкам
            makeLinksFocusable(this);
        }
        postDelayed(this, UPDATE_INTERVAL);

    }
    //функция для изменения цвета textview
    public static void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();
        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }
    //разделение ссылок
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
    //функция для получения текста из переведённых на русский субтитров по времени
    private String getRussianTimedText(long currentPosition){
        String result = "";
        //ищем в мапе по времени нужный текст для субтитров
        for (Map.Entry<Long, Line> entry : rus_track.entrySet()) {
            if (currentPosition < entry.getKey()) break;
            if (currentPosition < entry.getValue().to) result = entry.getValue().text;
        }
        return result;
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
    //парсинг русских субтитров и их перевода
    public static void rus_parse()  {
        rus_track = new TreeMap<>();
        //пробегаемся по английским субтитрам и берем оттуда текст и время
        for(Map.Entry<Long,Line> entry: track.entrySet()){
            Line curline=entry.getValue();
            //функция перевода
            translate(curline.text,curline.from,curline.to);
        }
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
   public void getReport(final String text) {
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
                String meanings="";
                String syns="(";
                String ex="";

                final ArrayList<String> defmean=new ArrayList<>();
                final ArrayList<String> defsyns=new ArrayList<>();
                final ArrayList<String> defex=new ArrayList<>();

                final ArrayList<StringTranslation> stringDict=new ArrayList<>();
                String transcription="";

                for (int i = 0; i < response.body().def.length; i++) {
                    StringTranslation item = new StringTranslation();

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
                            for ( char c : now.toCharArray() ) {
                                if ( Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN ) {
                                    isEnglish = false;
                                    break;
                                }
                            }
                           if(isEnglish){

                               syns+=now+", ";
                           }
                           else {

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
                    transcription="["+response.body().def[i].ts+"]";
                    item.index=i+1;
                    item.word=text;

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

    //функция перевода строки с субтитрами, апи яндекс переводчика
   public static void translate(String text, final long from, final long to){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TRLATE_URI_JSON)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TranslateApi service = retrofit.create(TranslateApi.class);
        String res="";
        Call<CurrentTranslation> call = service.translate(TRLATE_KEY, text,"en-ru");
        call.enqueue(new Callback<CurrentTranslation>() {
            @Override
            public void onResponse(Call<CurrentTranslation> call, Response<CurrentTranslation> response) {
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
                //кладём переведённый текст в treemap с русскими субтитрами
                String s=response.body().text[0];
                rus_track.put(from,new Line(from,to,s));

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