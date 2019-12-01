package com.example.mp11.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.ProgressBar;

import com.example.mp11.R;
import com.example.mp11.activities.MainActivity;
import com.example.mp11.adapters.DictAdapter;
import com.example.mp11.ForDictionaries.UsersDictionary;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SocialFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SocialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//фрагмент с поиском словарей
public class SocialFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    ListView listView;
    SearchView sv;
    View ftView;
    DictAdapter adapter;
    LoadHandler handler;
    boolean isLoading=false;
    boolean isSearch=false;
    ProgressBar pb;
    Iterator<DataSnapshot> iterator;
    //Iterator<DataSnapshot> inner_iterator;
   // Iterator<DataSnapshot> search_iterator;

    final ArrayList<UsersDictionary> usersDictionaries=new ArrayList<>();
    String text;

    //String CURRENT_USER_ID;
    FirebaseDatabase database;
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

        LayoutInflater li=(LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //вьюшки для анимации загрузки
        ftView=li.inflate(R.layout.footer_view,null);
        pb=(ProgressBar)view.findViewById(R.id.progress_dicts);
        pb.setVisibility(View.VISIBLE);
        //узнаем, как пролистан список
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //если долистали до последнего элемента, подгрузить ещё элементы
                //список динамически подгружается
                if(view.getLastVisiblePosition()==totalItemCount-1&&listView.getCount()>0&&!isLoading){
                isLoading=true;
                Thread thread=new ThreadGetData();
                thread.start();
                }
            }
        });

        //Firebase бд и ссылка на корневой каталог
        database = FirebaseDatabase.getInstance();
        //final DatabaseReference ref=database.getReference();
        //поиск и отображение первых видимых элментов
        if(iterator==null)
        search();

        //по клику на словарь перейти на фрагмент с его описанием
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment dict_description= DictDescriptionFragment.newInstance("kek","lol");
              //  Toast.makeText(getContext(),"ItemClick",Toast.LENGTH_SHORT).show();
                UsersDictionary ud=usersDictionaries.get(position);
                ((DictDescriptionFragment) dict_description).ud=ud;
                ((MainActivity)getActivity()).selectedFragment=dict_description;
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, dict_description).addToBackStack(null).commitAllowingStateLoss();


            }
        });

        //searchview для поиска по словарям в Firebase
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return  false; }

            @Override
            public boolean onQueryTextChange(String newText) {


                newText = newText.toLowerCase();

                if(newText.length()!=0){
                    //если что-то ввели, очистить старый массив
                        //usersDictionaries.clear();
                        isSearch=true;
                        text=newText;
                }else{
                    isSearch=false;
                    //usersDictionaries.clear();
                }
                //поиск по словарям
                search();
                return true;
            }
        });
        return view;
    }
    //функция для поиска словарей или отображения их всех
    private void search(){
        //ссылка на бд Firebase и чтение оттуда
            pb.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            usersDictionaries.clear();
            DatabaseReference ref1=database.getReference();
            ref1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    //берем итератор с подкаталогов каталога со словарями из Firebase
                    //там будут храниться id пользователей и их словари

                    //счётчик, чтоб 15 словарей подгрузить
                    final int[] count = {15};
                    //объект словаря

                    //пока дальше есть пользователь

                        //берем снапшот с этим пользователем

                        //выполняем поиск по бд Firebase в словарях у текущего пользователя
                        //названия которых начинаются с того, что было введено в searchview


                        if(isSearch) {

                            Query query = database.getReference("search_list").orderByKey().startAt(text).endAt(text+"\uf8ff");

                            //считываем выборку по условию
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot1) {
                                    //берём внутренний итератор, в котором будут удовлетворяющие условию словари

                                    Iterable<DataSnapshot> snapit = dataSnapshot1.getChildren();
                                    iterator = snapit.iterator();
                                    //пока дальше есть словари и подгружено меньше 15
                                    while (iterator.hasNext() && count[0] > 0) {
                                        //берем словарь и устанавливаем его характеристики в объект словаря
                                        DataSnapshot ds = (DataSnapshot) iterator.next();
                                        UsersDictionary ud = new UsersDictionary();
                                        ud.name = ds.getKey().split("4el2psy97congree")[0];
                                        ud.holderId = ds.getKey().split("4el2psy97congree")[1];
                                        DataSnapshot userRef = dataSnapshot.child("users").child(ud.holderId);
                                        ud.imageUrl = userRef.child("imageURL").getValue().toString();
                                        ud.holderName = userRef.child("username").getValue().toString();
                                        //добавляем в массив словарь
                                        usersDictionaries.add(ud);
                                        //уменьшаем счётчик
                                        count[0]--;
                                        //если 15 словарей подгрузилось, прервать цикл
                                        if (count[0] < 0) break;
                                    }
                                    adapter = new DictAdapter(getContext(), usersDictionaries);
                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    listView.setVisibility(View.VISIBLE);
                                    pb.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }

                            });
                        }else{
                            DatabaseReference ref=database.getReference("search_list");
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot1) {
                                    //берём внутренний итератор, в котором будут удовлетворяющие условию словари
                                    Iterable<DataSnapshot> snapit = dataSnapshot1.getChildren();
                                    iterator = snapit.iterator();
                                    //пока дальше есть словари и подгружено меньше 15
                                    while (iterator.hasNext() && count[0] > 0) {
                                        //берем словарь и устанавливаем его характеристики в объект словаря
                                        DataSnapshot ds = (DataSnapshot) iterator.next();
                                        UsersDictionary ud = new UsersDictionary();
                                        ud.name = ds.getKey().split("4el2psy97congree")[0];
                                        ud.holderId = ds.getKey().split("4el2psy97congree")[1];
                                        DataSnapshot userRef = dataSnapshot.child("users").child(ud.holderId);
                                        if(userRef.child("imageURL").getValue()!=null)ud.imageUrl = userRef.child("imageURL").getValue().toString();
                                        if(userRef.child("username").getValue()!=null)ud.holderName = userRef.child("username").getValue().toString();
                                        //добавляем в массив словарь
                                        usersDictionaries.add(ud);
                                        //уменьшаем счётчик
                                        count[0]--;
                                        //если 15 словарей подгрузилось, прервать цикл
                                        if (count[0] < 0) break;
                                    }
                                    adapter = new DictAdapter(getContext(), usersDictionaries);
                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    listView.setVisibility(View.VISIBLE);
                                    pb.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }





                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

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
    //для динамической подгрузки списка, когда пользователь долистал до его конца
    public class LoadHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    //добавить анимацию подгрузки
                    listView.addFooterView(ftView);
                    break;
                case 1:
                    //добавить к списку выборку массива
                    adapter.addToList((ArrayList<UsersDictionary>)msg.obj);
                    adapter.notifyDataSetChanged();
                    //убрать анимацию
                    listView.removeFooterView(ftView);
                    isLoading=false;
                    break;
                    default:
                        break;
            }
        }
    }
    //функция подгрузки данных для динамического списка, когда пользователь долистал до конца
    public ArrayList<UsersDictionary> getMoreData(){
        //если подгрузка из общего списка словарей, не по условию из searchview

            //создаем новый массив со словарями
            ArrayList<UsersDictionary> list = new ArrayList<>();
            //счётчик, что ещё 15 словарей подгрузятся
            int count = 15;

           // if (iterator.hasNext())
                //пока у итератора ещё дальше есть пользователи
                while (iterator.hasNext() && count > 0) {
                    //пока во внутреннем итераторе есть словари

                        DataSnapshot ds = (DataSnapshot) iterator.next();
                        final UsersDictionary ud = new UsersDictionary();
                        //кладём собранные данные текущего словаря в объект
                        ud.name = ds.getKey().split(" ")[0];
                        ud.holderId =ds.getKey().split(" ")[1];
                        DatabaseReference userRef = database.getReference("users").child(ud.holderId);
                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child("imageURL")!=null) ud.imageUrl = dataSnapshot.child("imageURL").getValue(String.class);
                                if(dataSnapshot.child("username")!=null) ud.holderName = dataSnapshot.child("username").getValue(String.class);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        //добавляем объекст к списку
                        list.add(ud);
                        count--;
                        if (count < 0) break;

                    //берем следующего пользователя, устанавливаем его как текущего и берём
                    //из него внутренний итератор для его словарей


                }
            return list;



    }
    //подгружаем во внешнем потоке
    public class ThreadGetData extends Thread{
        @Override
        public void run(){
            //передаём хэндлеру сообщения, чтобы он добавлял новую выборку словарей к массиву
            handler.sendEmptyMessage(0);
            ArrayList<UsersDictionary> new_list=getMoreData();
            Message msg=handler.obtainMessage(1,new_list);
            handler.sendMessage(msg);

        }
    }
}
