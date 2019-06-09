package com.example.mp11.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mp11.activities.DynVideoPlayer;
import com.example.mp11.R;


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

    EditText video,subs;
    Button btn,btn_get_subs;

    boolean flagUrl=true;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    String videoS,subsS;
    public  Uri videoUri,subsUri;
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
        View view= inflater.inflate(R.layout.fragment_video_player, container, false);
        btn=(Button)view.findViewById(R.id.start_video);
        btn_get_subs=(Button)view.findViewById(R.id.subs_path);
        video=(EditText)view.findViewById(R.id.video_url);
        subs=(EditText)view.findViewById(R.id.subs);
                //клик на кнопку просмотра
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //если пользователь не выбирал субтитры с устройства, а вводил ссылку
                        if(flagUrl) {
                            //собираем значения
                            videoS = video.getText().toString();
                            subsS = subs.getText().toString();
                            Intent i = new Intent(getContext(), DynVideoPlayer.class);
                            //передаём видеоплееру данные
                            i.putExtra("videourl", videoS);
                            i.putExtra("subsurl", subsS);
                            i.putExtra("title", "Видео");
                            startActivity(i);
                        }
                        //иначе пользователь выбирал субтитры с устройства
                        else{
                            videoS = video.getText().toString();
                            Intent i = new Intent(getContext(), DynVideoPlayer.class);
                            //кладём данные видеплееру
                            i.putExtra("videourl", videoS);
                            i.putExtra("subsuri", subsUri.toString());
                            i.putExtra("title", "Видео");
                            startActivity(i);
                        }

                    }


                });
                //кнопка для выборки файла с субтитрами с устройства
                btn_get_subs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //интент на просмотр файлов
                        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                        chooseFile.setType("*/*");
                        //стартуем активность интентом, чтобы потом результат получить
                        startActivityForResult(
                                Intent.createChooser(chooseFile, "Choose a file"),
                                112
                        );
                    }
                });


        return view;
    }
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
    //берем результат из интента по коду 112
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //если код 112 и всё успешно
        if (requestCode == 112 && resultCode == Activity.RESULT_OK&&data!=null&&data.getData()!=null){
            //собираем uri на файл
            Uri content_describer = data.getData();
            subsUri=content_describer;
            flagUrl=false;
        }
    }
}

