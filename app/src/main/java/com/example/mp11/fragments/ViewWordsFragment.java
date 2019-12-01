package com.example.mp11.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.mp11.R;
import com.example.mp11.adapters.WordAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewWordsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewWordsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//фрагмент для просмора слов в словаре пользователя
public class ViewWordsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ListView listView;

    private View ftView;
    private WordAdapter adapter;
    private ViewWordsFragment.LoadHandler handler;
    boolean isLoading=false;
    private ProgressBar pb;
    private Iterator<DataSnapshot> iterator;

    private FirebaseDatabase database;

    public String userId, dictName;

    ArrayList<String> words=new ArrayList<>();
    public ViewWordsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewWordsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewWordsFragment newInstance(String param1, String param2) {
        ViewWordsFragment fragment = new ViewWordsFragment();
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
        View view=inflater.inflate(R.layout.fragment_view_words, container, false);

        listView=view.findViewById(R.id.dict_search_list);
        //хэндлер для подгрузки слов, когда долистали до конца список
        handler=new ViewWordsFragment.LoadHandler();

        LayoutInflater li=(LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //вьюшка анимации подгрузки
        ftView=li.inflate(R.layout.footer_view,null);

        //анимация загрузки
        pb=(ProgressBar)view.findViewById(R.id.progress_dicts);


        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //когда долистали список до конца,  подгрузить динамически ещё порцию слов
                if(view.getLastVisiblePosition()==totalItemCount-1&&listView.getCount()>0&&!isLoading){
                    isLoading=true;
                    Thread thread=new ViewWordsFragment.ThreadGetData();
                    thread.start();
                }
            }
        });

        database = FirebaseDatabase.getInstance();
        DatabaseReference ref=database.getReference();

        //чтение с Firebase
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                DataSnapshot dataSnapshot=dataSnapshot1.child("dictionaries").child(userId).child(dictName).child("dictionary");
                //берем итератор
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                iterator = snapshotIterator.iterator();

                //счётчик
                int count=15;
                //если дальше есть слова
                while (iterator.hasNext()&&count>0) {
                    //берем слово, добавляем в массив со словами, счётчик уменьшаем
                    DataSnapshot next = (DataSnapshot) iterator.next();
                        String word=next.getKey();
                        words.add(word);
                        count--;
                        if(count<0) break;
                }

                //прикручиваем массив к адаптеру и списку
                adapter=new WordAdapter(getContext(),words);
                listView.setAdapter(adapter);
                pb.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
    //функция для динамической подгрузки слов, когда долистали до конца
    public ArrayList<String> getMoreData(){
        ArrayList<String> list=new ArrayList<>();
        //подгружаем еще 15 слов
        int count=15;
        //пока дальше её есть слова
            while (iterator.hasNext()&&count>0) {
                //добавить слово в список, счётчик уменьшить
                DataSnapshot next = (DataSnapshot) iterator.next();
                String word=next.getKey();
                    list.add(word);
                    count--;
                    if(count<0) break;
            }
        return list;
    }
    //для динамической подгрузки списка, когда пользователь долистал до его конца
    public class LoadHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    //добавить анимацию подгрузки
                    listView.addFooterView(ftView);
                    break;
                case 1:
                    //добавить к списку выборку массива и  убрать анимацию
                    adapter.addToList((ArrayList<String>)msg.obj);
                    listView.removeFooterView(ftView);
                    isLoading=false;
                    break;
                default:
                    break;
            }
        }
    }
    //подгружаем во внешнем потоке
    public class ThreadGetData extends Thread{
        @Override
        public void run(){
            //передаём хэндлеру сообщения, чтобы он добавлял новую выборку слов к массиву
            handler.sendEmptyMessage(0);
            ArrayList<String> new_list=getMoreData();
            Message msg=handler.obtainMessage(1,new_list);
            handler.sendMessage(msg);

        }
    }
}
