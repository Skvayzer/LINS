package com.example.mp11.activities;



import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.PopupMenu;

import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;

import android.widget.ImageButton;

import android.widget.LinearLayout;

import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.R;

import com.example.mp11.videoplayer.mydynvideoplayer.HpLib_ExtractorHpLibRendererBuilder;
import com.example.mp11.videoplayer.mydynvideoplayer.HpLib_HlsHpLibRendererBuilder;

import com.example.mp11.videoplayer.mydynvideoplayer.HpLib_RendererBuilder;
import com.example.mp11.videoplayer.mydynvideoplayer.MediaItem;
import com.example.mp11.videoplayer.SubtitleView;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.MediaFormat;

import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

//активность с видеоплеером для фильма с субтитрами
public class DynVideoPlayer extends AppCompatActivity implements HlsSampleSource.EventListener, View.OnClickListener, RecognitionListener {

    public static final int RENDERER_COUNT = 2;
    public static final int TYPE_AUDIO = 1;
    //exoplayer для создание кастомного плеера
    private ExoPlayer player;
    //SurfaceView, на который проецируется видео
    private SurfaceView surface;
    //источники видео, субтитров и т.д.
    private String video_url, video_type, video_title,subs_source;

    public Handler mainHandler;
    private HpLib_RendererBuilder hpLibRendererBuilder;
    private TrackRenderer videoRenderer;
    //панельки плеера
    public static LinearLayout root,top_controls, bottom_controls,onlySeekbar;
    private double seekSpeed = 0;
    public static final int TYPE_VIDEO = 0;
    private View decorView;
    private int uiImmersiveOptions;
    private RelativeLayout loadingPanel;
    private Runnable updatePlayer,hideControls;

    //верхняя полоска с названием видео и кнопкой выхода
    private ImageButton btn_back;
    private TextView txt_title;

    //ползунок, текущее проигранное время в плеере и общее время длительности видео
    private TextView txt_ct,txt_td;
    private SeekBar seekBar;
    private PlayerControl playerControl;


    //статус плеера
    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }
    //состояние настроек(показать или нет)
    public enum ControlsMode {
        LOCK, FULLCONTORLS
    }
    private ControlsMode controlsState;

    //кнопки
    private ImageButton btn_play;
    private ImageButton btn_pause;
    private ImageButton btn_fwd;
    private ImageButton btn_rev;
    private ImageButton btn_next;
    private ImageButton btn_prev;


    private ImageButton btn_settings;
    private ImageButton btnsubs;


    private Display display;
    private Point size;

    private int sWidth,sHeight;
    private float baseX, baseY;
    private long diffX, diffY;
    private int calculatedTime;
    private String seekDur;
    private Boolean tested_ok = false;
    private Boolean screen_swipe_move = false;
    private boolean immersiveMode, intLeft, intRight, intTop, intBottom, finLeft, finRight, finTop, finBottom;

    public static int height, width;



    public static Uri subsUri, videoUri;

    //вьюшка с субтитрами
    SubtitleView subView;
    //заморожены ли субтитры?
    public boolean isSubsFrozen=false;
    //для изменения размера субтитров
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    Bundle bundy = new Bundle();


    private static final int REQUEST_RECORD_PERMISSION = 100;
    public SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    ImageButton micro;


    public static boolean gravity_mode=false;
    public static boolean shown_controls=false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==200){
            int currTime = data.getIntExtra("currTime",0);
            player.seekTo(currTime);
        }
    }
    {
        //на изменении состояния плеера
        updatePlayer = new Runnable() {
            @Override
            public void run() {
                switch (player.getPlaybackState()) {
                    case ExoPlayer.STATE_BUFFERING:
                        loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    case ExoPlayer.STATE_ENDED:
                        finish();
                        break;
                    case ExoPlayer.STATE_IDLE:
                        loadingPanel.setVisibility(View.GONE);
                        break;
                    case ExoPlayer.STATE_PREPARING:
                        loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    case ExoPlayer.STATE_READY:
                        loadingPanel.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }

                String totDur = String.format("%02d.%02d.%02d",
                        TimeUnit.MILLISECONDS.toHours(player.getDuration()),
                        TimeUnit.MILLISECONDS.toMinutes(player.getDuration()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getDuration())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
                String curDur = String.format("%02d.%02d.%02d",
                        TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition()),
                        TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition())));
                txt_ct.setText(curDur);
                txt_td.setText(totDur);
                seekBar.setMax((int) player.getDuration());
                seekBar.setProgress((int) player.getCurrentPosition());

                mainHandler.postDelayed(updatePlayer, 200);
            }
        };
    }
    {
        hideControls = new Runnable() {
            @Override
            public void run() {
                hideAllControls();
            }
        };
    }
    //спрятать все меню и настройки
    private void hideAllControls(){
        //если меню плеера показано, скрыть
        if(controlsState==ControlsMode.FULLCONTORLS){
            if(root.getVisibility()==View.VISIBLE){
                root.setVisibility(View.GONE);
            }
        }else if(controlsState==ControlsMode.LOCK){

        }
        if(gravity_mode){
            subView.setY(height-subView.getHeight());
        }

        shown_controls=false;
        //полноэкранный режим
        decorView.setSystemUiVisibility(uiImmersiveOptions);
    }
    //показать меню плеера
    private void showControls(){
        //если спрятано, показать
        if(controlsState==ControlsMode.FULLCONTORLS){
            if(root.getVisibility()==View.GONE){
                root.setVisibility(View.VISIBLE);
            }
        }else if(controlsState==ControlsMode.LOCK){

        }
        if(gravity_mode) {
            subView.setY(height - bottom_controls.getHeight() - onlySeekbar.getHeight() - subView.getHeight());
        }
        shown_controls=true;

        mainHandler.removeCallbacks(hideControls);
        mainHandler.postDelayed(hideControls, 3000);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()){
            //если опустил палец
            case MotionEvent.ACTION_DOWN:
                tested_ok=false;
                if (event.getX() < (sWidth / 2)) {
                    intLeft = true;
                    intRight = false;
                } else if (event.getX() > (sWidth / 2)) {
                    intLeft = false;
                    intRight = true;
                }
                int upperLimit = (sHeight / 4) + 100;
                int lowerLimit = ((sHeight / 4) * 3) - 150;
                if (event.getY() < upperLimit) {
                    intBottom = false;
                    intTop = true;
                } else if (event.getY() > lowerLimit) {
                    intBottom = true;
                    intTop = false;
                } else {
                    intBottom = false;
                    intTop = false;
                }
                seekSpeed = (TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) * 0.1);
                diffX = 0;
                calculatedTime = 0;
                seekDur = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(diffX) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffX)),
                        TimeUnit.MILLISECONDS.toSeconds(diffX) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diffX)));

                //берём координаты касания
                baseX = event.getX();
                baseY = event.getY();
                //если открыто окно с переводом слова
                if(subView.ispopupWindow){
                    //показать меню плеера на заднем плане


                    onlySeekbar.setVisibility(View.VISIBLE);
                    top_controls.setVisibility(View.VISIBLE);
                    bottom_controls.setVisibility(View.VISIBLE);
                    root.setVisibility(View.VISIBLE);
                    showControls();
                    //надо бы закрыть окно
                    subView.ispopupWindow=false;
                }
                break;
            //если палец поднялся вверх, показать плеер
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //показать меню плеера
                screen_swipe_move=false;
                tested_ok = false;



                onlySeekbar.setVisibility(View.VISIBLE);
                top_controls.setVisibility(View.VISIBLE);
                bottom_controls.setVisibility(View.VISIBLE);
                root.setVisibility(View.VISIBLE);
                calculatedTime = (int) (player.getCurrentPosition() + (calculatedTime));
                player.seekTo(calculatedTime);
                showControls();
                break;

        }
        return super.onTouchEvent(event);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        //тип видео, который по умолчанию не hls
        video_type = "others";
        //достаём ссылки на видео, субтитры...
        video_url =getIntent().getExtras().getString("videourl");

        video_title="";
        subs_source=getIntent().getExtras().getString("subsurl");
        if (getIntent().getExtras().getString("subsuri")!=null)
        subsUri=Uri.parse(getIntent().getExtras().getString("subsuri"));

        subView = (SubtitleView)findViewById(R.id.subs_box);
        //настраиваем дизайн вьюшки с субтитрами
        subView.setTextColor(Color.WHITE);
        subView.setHighlightColor(Color.BLACK);
        subView.setLinkTextColor(Color.WHITE);
        subView.setBackgroundColor(Color.argb(75,0,0,0));
        subView.bringToFront();
        subView.dt=getIntent().getExtras().getInt("dt",0);

        subView.decorView=getWindow().getDecorView();


        //когда зажимаешь, можно двигать субтитры по экрану
        subView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!subView.isMoving){
                    subView.isMoving=true;
                }
                return false;
            }
        });

        //детектор движения масштабирования для изменения размера субтитров
        mScaleGestureDetector = new ScaleGestureDetector(this, new DynVideoPlayer.ScaleListener());

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        sWidth = size.x;
        sHeight = size.y;
        //параметры экрана(как полноэкранность и т.д.)
//        uiImmersiveOptions = (
//                 View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//
//
//
//
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//        );

        uiImmersiveOptions=(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        decorView = getWindow().getDecorView();
        if(Build.VERSION.SDK_INT < 19){

            decorView.setSystemUiVisibility(View.GONE);
        } else {
            decorView.setSystemUiVisibility(uiImmersiveOptions);
        }

        loadingPanel = (RelativeLayout) findViewById(R.id.loadingVPanel);
        txt_ct = (TextView) findViewById(R.id.txt_currentTime);
        txt_td = (TextView) findViewById(R.id.txt_totalDuration);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        //если перевели ползунок видео
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //переводим плеер в указанное положение
                player.seekTo(seekBar.getProgress());
            }
        });


        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_play = (ImageButton) findViewById(R.id.btn_play);
        btn_pause = (ImageButton) findViewById(R.id.btn_pause);
        btn_fwd = (ImageButton) findViewById(R.id.btn_fwd);
        btn_rev = (ImageButton) findViewById(R.id.btn_rev);
        btn_prev = (ImageButton) findViewById(R.id.btn_prev);
        btn_next = (ImageButton) findViewById(R.id.btn_next);
        btn_settings = (ImageButton) findViewById(R.id.btn_settings);
        btnsubs=(ImageButton)findViewById(R.id.btn_sub);

        onlySeekbar = (LinearLayout) findViewById(R.id.seekbar_time);
        top_controls = (LinearLayout) findViewById(R.id.top);
        bottom_controls = (LinearLayout) findViewById(R.id.controls);



        btn_back.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_fwd.setOnClickListener(this);
        btn_rev.setOnClickListener(this);
        btn_prev.setOnClickListener(this);
        btn_next.setOnClickListener(this);


        btn_settings.setOnClickListener(this);
        btnsubs.setOnClickListener(this);





        txt_title = (TextView) findViewById(R.id.txt_title);


        root = (LinearLayout) findViewById(R.id.root);
        root.setVisibility(View.VISIBLE);

        surface = (SurfaceView) findViewById(R.id.surface_view);




        txt_title.setText(video_title);

        mainHandler = new Handler();


        btn_next.setVisibility(View.INVISIBLE);
        btn_prev.setVisibility(View.INVISIBLE);


        subView.fading=findViewById(R.id.fading);

        //фунция инициализации работы плеера
        execute();
        //передаём субтитрам кнопки паузы и плея
        subView.btn_pause=btn_pause;
        subView.btn_play=btn_play;


        //устанавливаем субтитрам текущий плеер и inflater отсюда
        subView.setPlayer(playerControl);
        subView.inflater=getLayoutInflater();

        micro=(ImageButton) findViewById(R.id.micro);
        subView.micro=micro;

        speech = SpeechRecognizer.createSpeechRecognizer(this);

        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);



        micro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speech.startListening(recognizerIntent);
            }
        });





        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }
    //обработка нажатий на кнопки
    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.btn_back) {
            //если назад, завершить активность
            killPlayer();
            finish();
        }
        if (i1 == R.id.btn_pause) {
            //пауза
            if (playerControl.isPlaying()) {
                playerControl.pause();
                subView.isStopped=true;
                btn_pause.setVisibility(View.GONE);
                btn_play.setVisibility(View.VISIBLE);
            }
        }
        if (i1 == R.id.btn_play) {
            //проигрывание
            if (!playerControl.isPlaying()) {
                playerControl.start();
                //subView.isReadyToSpeak=false;
                subView.isStopped=false;
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
                if(micro.getVisibility()==View.VISIBLE){
                    micro.setVisibility(View.GONE);
                }
            }
        }
        if (i1 == R.id.btn_fwd) {
            //перемотать вперёд на немного
            player.seekTo(player.getCurrentPosition() + 10000);
        }
        if (i1 == R.id.btn_rev) {
            //перемотать назад на немного
            player.seekTo(player.getCurrentPosition() - 10000);
        }
        if (i1 == R.id.btn_next) {
            player.release();

            execute();
        }
        if (i1 == R.id.btn_prev) {
            player.release();

            execute();
        }

        //настройки субтитров
        if(i1==R.id.btn_sub){
            //открываем в уголке окошко с ними
            final PopupMenu popup = new PopupMenu(DynVideoPlayer.this, v);
            MenuInflater inflater=popup.getMenuInflater();
           // popup.inflate(R.menu.popsubs);
            inflater.inflate(R.menu.popsubs,popup.getMenu());
            popup.getMenu().findItem(R.id.menu1).setChecked(popup.getMenu().findItem(R.id.menu1).isChecked());
            popup.getMenu().findItem(R.id.menu2).setChecked(popup.getMenu().findItem(R.id.menu2).isChecked());
            popup.getMenu().findItem(R.id.menu3).setChecked(popup.getMenu().findItem(R.id.menu3).isChecked());
            popup.getMenu().findItem(R.id.menu4).setChecked(popup.getMenu().findItem(R.id.menu4).isChecked());

            //popup.getMenu().setGroupCheckable(1, true, true);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu1:
                           MenuItem subMenuItem = popup.getMenu().getItem(0);
                            //включить/выключить субтитры вообще
                            if(subView.getVisibility()==View.VISIBLE){
                                subView.setVisibility(View.INVISIBLE);
                               subMenuItem.setChecked(false);
                                //popup.getMenu().findItem(R.id.menu1).setChecked(false);
                                //item.setChecked(false);
                            }else {
                                subMenuItem.setChecked(true);
                                //popup.getMenu().findItem(R.id.menu1).setChecked(true);
                                subView.setVisibility(View.VISIBLE);
                                //item.setChecked(true);
                            }
                            return true;

                        case R.id.menu2:

                            //включить/выключить русский перевод субтитров
                            if(!subView.rus_mode){
                                subView.rus_mode=true;
                                item.setChecked(true);
                                popup.getMenu().findItem(R.id.menu2).setChecked(true);
                                //new RusParseTask().execute();
                            }else {
                                subView.rus_mode=false;
                                item.setChecked(false);
                                popup.getMenu().findItem(R.id.menu2).setChecked(false);
                            }
                            return true;
                        case R.id.menu3:

                            //заморозить субтитры
                            if(!isSubsFrozen){
                                isSubsFrozen=true;
                                item.setChecked(true);
                                //запоминаем положение до заморозки
                                subView.last_position=player.getCurrentPosition();
                                subView.isStopped=true;
                                popup.getMenu().findItem(R.id.menu3).setChecked(true);
                            }else{
                                isSubsFrozen=false;
                                //запоминаем положение после разморозки
                                subView.cur_position=player.getCurrentPosition();
                                //вычисляем разницу
                                int dt=(int)(subView.cur_position-subView.last_position);
                                //на эту разницу перематываем субтитры, чтобы они сходились с видео
                                subView.dt+=dt;
                                popup.getMenu().findItem(R.id.menu3).setChecked(false);
                                item.setChecked(false);
                                subView.isStopped=false;
                            }

                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.freeze_subs),
                                    Toast.LENGTH_SHORT).show();
                            return true;

                        case R.id.menu4:
                            if(!subView.isWatchAndSpeakMode) {
                                subView.isWatchAndSpeakMode=true;
                                item.setChecked(true);
                                ActivityCompat.requestPermissions
                                        (DynVideoPlayer.this,
                                                new String[]{Manifest.permission.RECORD_AUDIO},
                                                REQUEST_RECORD_PERMISSION);

                                subView.speech=speech;
                                subView.isReadyToSpeak=true;
                                subView.recognizerIntent=recognizerIntent;
                                popup.getMenu().findItem(R.id.menu4).setChecked(true);
                            }
                            else {
                                popup.getMenu().findItem(R.id.menu4).setChecked(false);
                                subView.isWatchAndSpeakMode=false;
                                item.setChecked(false);
                            }
                            return true;

                        case R.id.menu5:
                            gravity_mode=!gravity_mode;
                            return true;
                        default:
                            return false;
                    }
                }
            });


            popup.show();
        }



        if (i1 == R.id.btn_settings) {
            //настройки качества видео для hls
            PopupMenu popup = new PopupMenu(DynVideoPlayer.this, v);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    player.setSelectedTrack(0, (item.getItemId() - 1));
                    return false;
                }
            });
            Menu menu = popup.getMenu();
            menu.add(Menu.NONE, 0, 0, getString(R.string.video_quality));
            //считаем качество видео
            for (int i = 0; i < player.getTrackCount(0); i++) {
                MediaFormat format = player.getTrackFormat(0, i);
                if (MimeTypes.isVideo(format.mimeType)) {
                    if (format.adaptive) {
                        menu.add(1, (i + 1), (i + 1), "Auto");
                    } else {
                        menu.add(1, (i + 1), (i + 1), format.width + "p");
                    }
                }
            }
            menu.setGroupCheckable(1, true, true);
            if(menu.findItem((player.getSelectedTrack(0) + 1))!=null) menu.findItem((player.getSelectedTrack(0) + 1)).setChecked(true);
            popup.show();
        }
    }
    //фунция инициализации работы плеера
    private void execute() {
        //создаем новый плеер
        player=ExoPlayer.Factory.newInstance(RENDERER_COUNT);
        playerControl = new PlayerControl(player);

        txt_title.setText(video_title);

        new MySubsParseTask().execute();

        //устанавливаем текуший плеер на субтитры
        subView.setPlayer(playerControl);

        if(player!=null) {
            hpLibRendererBuilder = getHpLibRendererBuilder();
            hpLibRendererBuilder.buildRenderers(this);
            loadingPanel.setVisibility(View.VISIBLE);
            mainHandler.postDelayed(updatePlayer, 200);
            mainHandler.postDelayed(hideControls, 3000);
            controlsState = ControlsMode.FULLCONTORLS;

        }
     //if(!playerControl.isPlaying()) playerControl.start();

    }

    //какой тип стриминга видео
    private HpLib_RendererBuilder getHpLibRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "HpLib");
        switch (video_type){
            case "hls":
                return new HpLib_HlsHpLibRendererBuilder(this, userAgent, video_url);
            case "others":
                return new HpLib_ExtractorHpLibRendererBuilder(this,userAgent, Uri.parse(video_url));
            default:
                throw new IllegalStateException(getString(R.string.Not_supported_format) + video_url);
        }
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    public void onRenderersError(Exception e) {
    }

    public void onRenderers(TrackRenderer[] renderers, BandwidthMeter bandwidthMeter) {
        for (int i = 0; i < renderers.length; i++) {
            if (renderers[i] == null) {
                renderers[i] = new DummyTrackRenderer();
            }
        }
        // Complete preparation.
        this.videoRenderer = renderers[TYPE_VIDEO];
        pushSurface(false);
        player.prepare(renderers);
        player.setPlayWhenReady(true);
    }

    private void pushSurface(boolean blockForSurfacePush) {
        if (videoRenderer == null) {return;}
        if (blockForSurfacePush) {
            player.blockingSendMessage(
                    videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface.getHolder().getSurface());
        } else {
            player.sendMessage(
                    videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface.getHolder().getSurface());
        }
    }


    private void killPlayer(){
        if (player != null) {
            player.release();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        killPlayer();
    }

    @Override
    public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {

    }

    @Override
    public void onLoadCanceled(int sourceId, long bytesLoaded) {

    }

    @Override
    public void onLoadError(int sourceId, IOException e) {

    }

    @Override
    public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs) {

    }
    //листенер для изменения размера субтитров, если сделать масштабирующее движение пальцами по экрану
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            //коэффициент изменения размера
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));
            //меняем размер
            subView.setScaleX(mScaleFactor);
            subView.setScaleY(mScaleFactor);
            return true;
        }
    }


    @Override
    protected void onPause(){

        super.onPause();
        //если плеер играет, то остановить
        if (playerControl.isPlaying()) {
            playerControl.pause();
            //заменяем иконку-кнопку на паузу
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);
        }
        if(subView.tts!=null) {
            subView.tts.stop();
            subView.tts.shutdown();
        }
        //onStop();
    }
    @Override
    protected void onResume(){

        super.onResume();


        onSaveInstanceState(bundy);
        onRestoreInstanceState(bundy);
        onlySeekbar.setVisibility(View.VISIBLE);
        top_controls.setVisibility(View.VISIBLE);
        bottom_controls.setVisibility(View.VISIBLE);
        root.setVisibility(View.VISIBLE);
        showControls();



    }

    //для парсинга файла с английскими субтитрами во внешнем потоке
    class MySubsParseTask extends AsyncTask<Void,Void,Void>
    {


        protected void onPreExecute() {


        }
        protected Void doInBackground(Void... params) {
            //источник субтитров - ссылка

            try {
                //если субтитры выбраны с устройства в формате srt
                if (subsUri != null)
                    subView.setSubSource(subsUri, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP, DynVideoPlayer.this);
                    //иначе выбираем по ссылке
                else subView.setSubSource(subs_source, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),getString(R.string.choose_files),Toast.LENGTH_SHORT);
            }
            return null;
        }



        protected void onPostExecute(Void result) {

        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(player!=null){
            outState.putLong("seek_time", player.getCurrentPosition());
            outState.putFloat("scaleFactor", mScaleFactor);
            outState.putFloat("subX", subView.getX());
            outState.putFloat("subY", subView.getY());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        subView.setX(savedInstanceState.getFloat("subX"));
        subView.setY(savedInstanceState.getFloat("subY"));

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(DynVideoPlayer.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
            speech.destroy();

        }
    }


    @Override
    public void onBeginningOfSpeech() {


    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);

        subView.setText(errorMessage);
        subView.appendRussian();
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {

    }

    @Override
    public void onPartialResults(Bundle arg0) {

    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {

    }

    @Override
    public void onResults(Bundle results) {

        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
//        for (String result : matches)
//            text += result + "\n";

        subView.setText(matches.get(0));
        subView.appendRussian();
    }

    @Override
    public void onRmsChanged(float rmsdB) {


    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}
