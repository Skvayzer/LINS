package com.example.mp11.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.activities.DynVideoPlayer;
import com.example.mp11.R;
import com.example.mp11.videoplayer.SubtitleView;

import static android.content.Context.MODE_PRIVATE;


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

    TextView tv;

    boolean flagUrl=true;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    String videoS,subsS;
    public  Uri videoUri,subsUri;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    String description;

    int dt=0;
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
        preferences=getActivity().getSharedPreferences("pref",MODE_PRIVATE);
        editor=preferences.edit();
        subsUri=Uri.parse(preferences.getString("subspath","android.resource://com.example.mp11/" + R.raw.thetrial));
        description=getContext().getString(R.string.where_take_subs);
        dt=preferences.getInt("dt",22000);
        //if(preferences.getString("subspath",null)==null) dt=22000;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_video_player, container, false);
        btn=(Button)view.findViewById(R.id.start_video);
        btn_get_subs=(Button)view.findViewById(R.id.subs_path);
        video=(EditText)view.findViewById(R.id.video_url);
        subs=(EditText)view.findViewById(R.id.subs);
        tv=(TextView)view.findViewById(R.id.descript);
        subs.setText(subsUri.toString());
        video.setText(preferences.getString("videopath","https://emerald.rev.lavenderhosted.com/s5e2.mp4"));
                //клик на кнопку просмотра
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //если пользователь не выбирал субтитры с устройства, а вводил ссылку

                            //собираем значения
                            videoS = video.getText().toString();
                            subsS = subs.getText().toString();
                            editor.putString("subspath",subsS);
                            editor.putString("videopath",videoS);
                            editor.apply();
                            Intent i = new Intent(getContext(), DynVideoPlayer.class);
                            //передаём видеоплееру данные
                            i.putExtra("videourl", videoS);
                            //если передали ссылку
                            if(Uri.parse(subsS)==null)
                            i.putExtra("subsurl", subsS);
                            else {
                                i.putExtra("subsuri", subsUri.toString());


                            }
                            i.putExtra("title", R.string.video_name_in_player);
                            i.putExtra("dt",dt);
                            startActivity(i);

                        //иначе пользователь выбирал субтитры с устройства


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

                tv.setText(description);
                //разделительная строка с ссылкой
        SpannableString link = makeLinkSpan("subscene.com", new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri webpage = Uri.parse("https://subscene.com/");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    startActivity(intent);
            }
        });
        tv.append(link);
        tv.append(getContext().getString(R.string.and_whitespaces));
        link = makeLinkSpan("opensubtitles.org", new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri webpage = Uri.parse("https://www.opensubtitles.org/en/search");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            }
        });
        tv.append(link);
        return view;
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
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

    private SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
        SpannableString link = new SpannableString(text);
        link.setSpan(new ClickableString(listener), 0, text.length(),
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        return link;
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

