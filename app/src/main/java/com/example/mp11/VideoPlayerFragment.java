package com.example.mp11;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


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
    String videourl="https://youtu.be/KV4v-yNR7NU";
    VideoView videoView ;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurface;
    TextView tv;
    Button btn;
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
        View view= inflater.inflate(R.layout.fragment_settings, container, false);
        btn=(Button)view.findViewById(R.id.govideo);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getContext(), VideoPlayer.class);

                startActivity(i);
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
}
