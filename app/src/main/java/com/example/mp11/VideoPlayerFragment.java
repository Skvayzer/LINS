package com.example.mp11;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VideoPlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoPlayerFragment extends Fragment //implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static ProgressDialog progressDialog;


    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurface;
    TextView tv;
    Button btn;
    String cururl;
    WebView mWebView;
    String videourl;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public VideoPlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoPlayerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoPlayerFragment newInstance(String param1, String param2) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


//        vidSurface = (SurfaceView) view.findViewById(R.id.surfView);
//        vidHolder = vidSurface.getHolder();
//        vidHolder.addCallback(this);
        //videoView = (VideoView)view.findViewById(R.id.video);

//        progressDialog = ProgressDialog.show(getContext(), "", "Buffering video...",true);
//        progressDialog.setCancelable(true);
//
//
//        PlayVideo();

        View view= inflater.inflate(R.layout.fragment_video_player, container, false);
        mWebView = (WebView) view.findViewById(R.id.webview);
        btn=(Button)view.findViewById(R.id.watch);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                final String url = "https://english-films.com/westerns/171-nazad-v-buduschee-3-back-to-the-future-part-iii-1990-hd-720-ru-eng.html";
                mWebView.loadUrl(url);
                mWebView.setWebViewClient(new WebViewClient());
                cururl = mWebView.getUrl();
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String cururl = mWebView.getUrl();
                        new MyTask().execute();
                       // Toast.makeText(getContext(),"g",Toast.LENGTH_SHORT).show();



                    }


                });


            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });



        return view;
    }
//    private void foo() {
//        SpannableString link = makeLinkSpan("click here", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getContext(), "YOU CLICKED", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        tv.setText("To perform action, ");
//
//        tv.append(link);
//        tv.append(".");
//        makeLinksFocusable(tv);
//    }
//    private SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
//        SpannableString link = new SpannableString(text);
//        link.setSpan(new ClickableString(listener), 0, text.length(),
//                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
//        return link;
//    }
//
//    private void makeLinksFocusable(TextView tv) {
//        MovementMethod m = tv.getMovementMethod();
//        if ((m == null) || !(m instanceof LinkMovementMethod)) {
//            if (tv.getLinksClickable()) {
//                tv.setMovementMethod(LinkMovementMethod.getInstance());
//            }
//        }
//    }
//
//
//
//    private static class ClickableString extends ClickableSpan {
//        private View.OnClickListener mListener;
//        public ClickableString(View.OnClickListener listener) {
//            mListener = listener;
//        }
//        @Override
//        public void onClick(View v) {
//            mListener.onClick(v);
//        }
//    }


//    private void PlayVideo()
//    {
//        try
//        {
//            getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);
//            MediaController mediaController = new MediaController(getContext());
//            mediaController.setAnchorView(videoView);
//
//            Uri video = Uri.parse(videourl);
//            videoView.setMediaController(mediaController);
//            videoView.setVideoURI(video);
//            videoView.requestFocus();
//            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
//            {
//
//                public void onPrepared(MediaPlayer mp)
//                {
//                    progressDialog.dismiss();
//                    videoView.start();
//                }
//            });
//
//        }
//        catch(Exception e)
//        {
//            progressDialog.dismiss();
//            System.out.println("Video Play Error :"+e.toString());
//
//        }
//
//    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

//    @Override
//    public void onPrepared(MediaPlayer mp) {
//        mediaPlayer.start();
//    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        try {
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setDisplay(vidHolder);
//            mediaPlayer.setDataSource(videourl);
//            mediaPlayer.prepare();
//            mediaPlayer.setOnPreparedListener(this);
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    class MyTask extends AsyncTask<Void, String, String> {
        String current_url="";
        @Override
        protected void onPreExecute() {
            current_url=mWebView.getUrl();
        }
        @Override
        protected String doInBackground(Void... params) {

            String res="";
            Document doc=null;
            try {
                //doc = Jsoup.connect(cururl).get();

                doc = Jsoup.connect(current_url).header("Accept-Encoding", "gzip, deflate")
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                        .maxBodySize(0)
                        .get();
                Elements vids = doc.getElementsByTag("iframe");
                for (Element vid : vids) {
                    if(vid.attr("src").contains("openload")){
                    //if (vid.attr("src").contains("streamango")) {
                        res = vid.attr("src");
                        int from=res.indexOf("embed")+6;
                        int to=res.indexOf("/",from);
                        res=res.substring(from,to);
//                        Document doc1 = null;
//
//                        //doc = Jsoup.connect(cururl).get();
//
//                        doc1 = Jsoup.connect(res).header("Accept-Encoding", "gzip, deflate")
//                                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
//                                .maxBodySize(0)
//                                .get();
//
//
//                        Elements vidu = doc1.getElementsByTag("video");
//                        res = vidu.first().attr("src");
//                        Document doc2 = null;
//
//                        //doc = Jsoup.connect(cururl).get();
//
//                        doc2 = Jsoup.connect(res).header("Accept-Encoding", "gzip, deflate")
//                                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
//                                .maxBodySize(0)
//                                .get();
//
//
//                        Elements vidfi = doc2.getElementsByTag("meta");
//                        for (Element vidnow : vidfi) {
//                            if (vidnow.attr("name").equals("src")) {
//                                res = vidnow.attr("content");
//                                break;
//
//                            }
//                        }


                    }
                }






//                Elements links = doc.select("video");
//                Log.d("URL: ", links.first().text());

            } catch (IOException e) {
                e.printStackTrace();
            }

            HttpClient httpclient = new DefaultHttpClient();
            String URL="https://api.openload.co/1/file/dlticket?file="+res+"&login=8fe311552cf7409d&key=XYvH9jgv";
            HttpGet httpget= new HttpGet(URL);
            String ticket="none";
            HttpResponse response = null;
            try {
                response = httpclient.execute(httpget);
                if(response.getStatusLine().getStatusCode()==200){
                    String server_response = "";
                    try {
                        server_response = EntityUtils.toString(response.getEntity());
                        JSONObject obj=new JSONObject(server_response);
                        ticket=obj.getJSONObject("result").getString("ticket");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                    Log.i("Server response", server_response );
                } else {
                    Log.i("Server response", "Failed to get server response" );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            HttpGet httpget1= new HttpGet("https://api.openload.co/1/file/dl?file="+res+"&ticket="+ticket);
            String video_url;
            try {
                HttpResponse response1=httpclient.execute(httpget1);
                if(response1.getStatusLine().getStatusCode()==200){
                    String server_response = "";
                    try {
                        server_response = EntityUtils.toString(response1.getEntity());
                        JSONObject obj=new JSONObject(server_response);
                        video_url=obj.getJSONObject("result").getString("url");
                        res=video_url;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                    Log.i("Server response", server_response );
                } else {
                    Log.i("Server response", "Failed to get server response" );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            Elements subs = doc.getElementsByTag("a");
            for (Element sub : subs) {
                if(sub.attr("href").contains("srt")){
                    res+=" "+sub.attr("href");
                    break;
                }

            }

            return res + " " +doc.title();
        }


        @Override
        protected void onPostExecute(String result) {
            videourl=result;
            Intent i=new Intent(getContext(), DynVideoPlayer.class);
            String[] s=videourl.split(" ");
            String title=videourl.substring(s[0].length()+s[1].length()+1,videourl.length());
            i.putExtra("videourl",s[0]);
            i.putExtra("subsurl",s[1]);
            i.putExtra("title",title);

            startActivity(i);
        }
    }
    public boolean downloadFile(final String path)
    {
        try
        {
            URL url = new URL(path);

            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(10000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

            File file = new File(getContext().getDir("filesdir", Context.MODE_PRIVATE) + "/subs.sqr");

            if (file.exists())
            {
                file.delete();
                downloadFile(path);
            }
            file.createNewFile();

            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            int len;
            while ((len = inStream.read(buff)) != -1)
            {
                outStream.write(buff, 0, len);
            }

            outStream.flush();
            outStream.close();
            inStream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
