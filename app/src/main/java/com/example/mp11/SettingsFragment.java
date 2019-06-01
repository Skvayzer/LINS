package com.example.mp11;


import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.mp11.views.SubtitleView;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;





    private static Handler handler = new Handler();

    private OnFragmentInteractionListener mListener;

    Button btn;
    CheckBox box,trans;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
                             Bundle savedInstanceState)   {
        View view= inflater.inflate(R.layout.fragment_settings, container, false);
        btn=(Button)view.findViewById(R.id.govideo);
        box=(CheckBox)view.findViewById(R.id.chckservice);
        trans=(CheckBox)view.findViewById(R.id.chcktranslation);
        Button logout=(Button)view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(getActivity(),LoginActivity.class);
                startActivity(i);
                getActivity().finish();

            }
        });
        box.setSelected(true);
        trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(box.isSelected()){
                    box.setSelected(false);
                    EasyWordsBtn.eng=true;
                }else{
                    box.setSelected(true);
                    EasyWordsBtn.eng=false;
                }
            }
        });
        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(box.isSelected()){
                    box.setSelected(false);
                    Intent serv=new Intent(getActivity().getApplicationContext(),EasyWordsBtn.class);
                    getActivity().stopService(serv);
                }else{
                    box.setSelected(true);
                    Intent serv=new Intent(getActivity().getApplicationContext(),EasyWordsBtn.class);
                    getActivity().startService(serv);
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getContext(), VideoPlayer.class);

                startActivity(i);
            }
        });


        return view;
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

}
