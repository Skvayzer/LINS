package com.example.mp11;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.views.CategDictionary;
import com.example.mp11.views.DictAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


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

  //  private MessageListAdapter mMessageAdapter;
  //  private FloatingActionButton sendbtn;
    private EditText editText;
    private ListView lv;
   // private MessageListAdapter adapter;

  //  private Button btnStore, btnGetall;
  //  private EditText etword, etdefinition, etsyns,etex;
  //  private MyDbHelper databaseHelper;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
boolean test=true;
    private OnFragmentInteractionListener mListener;

    ListView listView;
    SearchView sv;
    View ftView;
    DictAdapter adapter;
    LoadHandler handler;
    boolean isLoading=false;

    Iterator<DataSnapshot> iterator;
    Iterator<DataSnapshot> inner_iterator;
    final ArrayList<String> names=new ArrayList<>();
    final ArrayList<String> holders=new ArrayList<>();
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
        sv=view.findViewById(R.id.searchdicts);
        listView=view.findViewById(R.id.dict_search_list);
        handler=new LoadHandler();
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


        LayoutInflater li=(LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ftView=li.inflate(R.layout.footer_view,null);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(view.getLastVisiblePosition()==totalItemCount-1&&listView.getCount()>0&&!isLoading){
                isLoading=true;
                Thread thread=new ThreadGetData();
                thread.start();
                }
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref=database.getReference().child("dictionaries");

       // final HashMap<String, List<String>> map=new HashMap<>();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                iterator = snapshotIterator.iterator();

                int count=6;
                while (iterator.hasNext()&&count>0) {
                    DataSnapshot next = (DataSnapshot) iterator.next();
                    Iterable<DataSnapshot> snapit = next.getChildren();
                    inner_iterator = snapit.iterator();
                    while(inner_iterator.hasNext()&&count>0) {
                        DataSnapshot ds = (DataSnapshot) inner_iterator.next();
                        names.add(ds.getKey());
                        holders.add(next.getKey());
                        count--;
                        if(count<0) break;
                    }

                }

//                for(DataSnapshot ds: dataSnapshot.getChildren()) {
//
//
//                    if(count<0) break;
//                   // map.put(ds.getKey(),names);
//                }
                adapter=new DictAdapter(getContext(),names,holders);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //CategDictionary.downloadDict(FirebaseAuth.getInstance().getCurrentUser().getUid(), "jehb", getContext());
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
    public class LoadHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    listView.addFooterView(ftView);
                    break;
                case 1:
                    adapter.addToList((ArrayList<String>)msg.obj);
                    listView.removeFooterView(ftView);
                    isLoading=false;

                    break;
                    default:
                        break;
            }
        }
    }
    public ArrayList<String> getMoreData(){
        ArrayList<String> list=new ArrayList<String>();
        ArrayList<String> hold=new ArrayList<>();
        int count=6;
        if(iterator.hasNext())
        while (iterator.hasNext()&&count>0) {
            DataSnapshot next = (DataSnapshot) iterator.next();
            while(inner_iterator.hasNext()&&count>0) {
                DataSnapshot ds = (DataSnapshot) inner_iterator.next();
                list.add(ds.getKey());
               hold.add(next.getKey());
                count--;
                if(count<0) break;
            }

        }
        else{
            while(inner_iterator.hasNext()&&count>0) {
                DataSnapshot ds = (DataSnapshot) inner_iterator.next();
                list.add(ds.getKey());
                hold.add("kek");
                count--;
                if(count<0) break;
            }
        }

        return list;
    }
    public class ThreadGetData extends Thread{
        @Override
        public void run(){
            handler.sendEmptyMessage(0);
            ArrayList<String> new_list=getMoreData();
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            Message msg=handler.obtainMessage(1,new_list);
            handler.sendMessage(msg);

        }
    }
}
