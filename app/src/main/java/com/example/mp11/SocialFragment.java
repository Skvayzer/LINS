package com.example.mp11;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mp11.MyDatabase.MyDbHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SocialFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SocialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SocialFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private MessageListAdapter mMessageAdapter;
    private FloatingActionButton sendbtn;
    private EditText editText;
    private ListView lv;
    private MessageListAdapter adapter;

    private Button btnStore, btnGetall;
    private EditText etword, etdefinition, etsyns,etex;
    private MyDbHelper databaseHelper;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
boolean test=true;
    private OnFragmentInteractionListener mListener;

    public SocialFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SocialFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SocialFragment newInstance(String param1, String param2) {
        SocialFragment fragment = new SocialFragment();
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
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_social, container, false);
//        sendbtn=(FloatingActionButton) view.findViewById(R.id.sendbtn);
//        editText=(EditText)view.findViewById(R.id.usermessage);
//        lv=(ListView)view.findViewById(R.id.message_list);
//        adapter=new MessageListAdapter(getContext());
//        lv.setAdapter(adapter);
//        sendbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendMessage();
//            }
//        });

//        databaseHelper = new MyDbHelper(getContext(),"TED");
//
//        btnStore = (Button) view.findViewById(R.id.btnstore);
//        btnGetall = (Button) view.findViewById(R.id.btnget);
//        etword = (EditText) view.findViewById(R.id.etword);
//        etdefinition = (EditText) view.findViewById(R.id.etdefinition);
//        etsyns = (EditText) view.findViewById(R.id.etsyns);
//        etex=(EditText) view.findViewById(R.id.etex);
//        btnStore.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                databaseHelper.addWord(etword.getText().toString(), etdefinition.getText().toString(), etsyns.getText().toString(),etex.getText().toString());
//                etword.setText("");
//                etdefinition.setText("");
//                etsyns.setText("");
//                etex.setText("");
//                Toast.makeText(getContext(), "Stored Successfully!", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        btnGetall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext() , GetAllWordsActivity.class);
//                startActivity(intent);
//            }
//        });

        return view;
    }
//    public void sendMessage() {
//        String message = editText.getText().toString();
//        if (message.length() > 0) {
//
//            String date=getCurrentTimeUsingDate();
//            adapter.add(new Message(message, new MemberData("Соня ТкаченКО-КО-КО","#66FF88"), test, date));
//            test=!test;
//            editText.getText().clear();
//        }
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
    public static String getCurrentTimeUsingDate() {
        Date date = new Date();
        String strDateFormat = "hh:mm:ss a";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate= dateFormat.format(date);
        return formattedDate;
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
