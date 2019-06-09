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
    Iterator<DataSnapshot> inner_iterator;
    Iterator<DataSnapshot> search_iterator;
    Iterator<DataSnapshot> search_inner_iterator;
    final ArrayList<UsersDictionary> usersDictionaries=new ArrayList<>();
    String text;

    String CURRENT_USER_ID;
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
        final DatabaseReference ref=database.getReference();
        //поиск и отображение первых видимых элментов
        search(ref);

        //по клику на словарь перейти на фрагмент с его описанием
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment dict_description= DictDescriptionFragment.newInstance("kek","lol");
              //  Toast.makeText(getContext(),"ItemClick",Toast.LENGTH_SHORT).show();
                UsersDictionary ud=usersDictionaries.get(position);
                ((DictDescriptionFragment) dict_description).ud=ud;
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, dict_description)
                        .commit();
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
                        usersDictionaries.clear();
                        isSearch=true;
                        text=newText;
                }else{
                    isSearch=false;
                    usersDictionaries.clear();
                }
                //поиск по словарям
                search(ref);
                return true;
            }
        });
        return view;
    }
    //функция для поиска словарей или отображения их всех
    private void search(DatabaseReference ref){
        //если пользователь ничего не ищет и searchview пустой
        if(!isSearch) {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                    //берем итератор с подкаталогов каталога со словарями из Firebase
                    //там будут храниться id пользователей и их словари
                    DataSnapshot dataSnapshot = dataSnapshot1.child("dictionaries");
                    Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                    iterator = snapshotIterator.iterator();
                    //подгружаем 15 словарей
                    int count = 15;
                    //пока дальше ещё есть пользователи и подгружено меньше 15 словарей
                    while (iterator.hasNext() && count > 0) {
                        //берем подкаталог следующего пользователя
                        DataSnapshot next = (DataSnapshot) iterator.next();
                        //полечаем "внутренний" итератор с его подкаталогов, где хранятся уже словари
                        Iterable<DataSnapshot> snapit = next.getChildren();
                        inner_iterator = snapit.iterator();
                        //пока у этого пользователя ещё есть словари и подгружено меньше 15 словарей
                        while (inner_iterator.hasNext() && count > 0) {
                            //берем подкаталог со словарём
                            DataSnapshot ds = (DataSnapshot) inner_iterator.next();
                            //объект словаря пользователя
                            final UsersDictionary ud = new UsersDictionary();
                            //кладём в объект название словаря, id пользователя
                            ud.name = ds.getKey();
                            ud.holderId = next.getKey();
                            //также сохраняем его как текущего
                            CURRENT_USER_ID = next.getKey();
                            //также ищем ссылку на фото аватара и имя пользователя
                            DataSnapshot dsProfileInfo = dataSnapshot1.child("users").child(ud.holderId);
                            //если есть, кладём в объект ссылку на фото аватара
                            if(dsProfileInfo.child("imageURL").getValue()!=null)
                                ud.imageUrl = dsProfileInfo.child("imageURL").getValue().toString();
                            //если есть, кладём имя пользователя в объект
                            if(dsProfileInfo.child("username").getValue()!=null) ud.holderName = dsProfileInfo.child("username").getValue().toString();
                            //добавляем к массиву словарей
                            usersDictionaries.add(ud);
                            //уменьшаем счётчик
                            count--;
                            //если 15 словарей подгружены, завершить цикл
                            if (count < 0) break;
                        }

                    }
                    //прикручиваем адаптер с массивом выбранных словарей к списку
                    adapter = new DictAdapter(getContext(), usersDictionaries);
                    listView.setAdapter(adapter);
                    //загрузка завершена
                    pb.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        //иначе пользователь ищет словари, он что-то ввёл в searchview
        else{
            //ссылка на бд Firebase и чтение оттуда
            DatabaseReference ref1=database.getReference();
            ref1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    //берем итератор с подкаталогов каталога со словарями из Firebase
                    //там будут храниться id пользователей и их словари
                    Iterable<DataSnapshot> snapshotIterator = dataSnapshot.child("dictionaries").getChildren();
                    search_iterator = snapshotIterator.iterator();
                    //счётчик, чтоб 15 словарей подгрузить
                    final int[] count = {15};
                    //объект словаря
                    final UsersDictionary[] ud = new UsersDictionary[1];
                    //пока дальше есть пользователь
                    while (search_iterator.hasNext() && count[0] > 0) {
                        //берем снапшот с этим пользователем
                        final DataSnapshot next = (DataSnapshot) search_iterator.next();
                        //выполняем поиск по бд Firebase в словарях у текущего пользователя
                        //названия которых начинаются с того, что было введено в searchview
                        Query query=database.getReference("dictionaries").child(next.getKey()).orderByChild("name").startAt(text);
                        final int finalCount = count[0];
                        //считываем выборку по условию
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot1) {
                                //берём внутренний итератор, в котором будут удовлетворяющие условию словари
                                Iterable<DataSnapshot> snapit = dataSnapshot1.getChildren();
                                search_inner_iterator = snapit.iterator();
                                //пока дальше есть словари и подгружено меньше 15
                                while (search_inner_iterator.hasNext() && finalCount > 0) {
                                    //берем словарь и устанавливаем его характеристики в объект словаря
                                    DataSnapshot ds = (DataSnapshot) search_inner_iterator.next();
                                    ud[0] = new UsersDictionary();
                                    ud[0].name = ds.getKey();
                                    ud[0].holderId = next.getKey();
                                    DataSnapshot userRef = dataSnapshot.child("users").child(ud[0].holderId);
                                    ud[0].imageUrl = userRef.child("imageURL").getValue().toString();
                                    ud[0].holderName = userRef.child("username").getValue().toString();
                                    //добавляем в массив словарь
                                    usersDictionaries.add(ud[0]);
                                    //уменьшаем счётчик
                                    count[0]--;
                                    //если 15 словарей подгрузилось, прервать цикл
                                    if (count[0] < 0) break;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });

                    }
                    //если массив не пустой, прикрутить его к адаптеру и списку
                   if(usersDictionaries!=null&&usersDictionaries.size()!=0) {
                       adapter = new DictAdapter(getContext(), usersDictionaries);
                       listView.setAdapter(adapter);
                       adapter.notifyDataSetChanged();
                       pb.setVisibility(View.GONE);
                   }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
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
    public ArrayList<UsersDictionary> getMoreData(boolean flag, final String text){
        //если подгрузка из общего списка словарей, не по условию из searchview
        if(!flag) {
            //создаем новый массив со словарями
            ArrayList<UsersDictionary> list = new ArrayList<>();
            //счётчик, что ещё 15 словарей подгрузятся
            int count = 15;

           // if (iterator.hasNext())
                //пока у итератора ещё дальше есть пользователи
                while (iterator.hasNext() && count > 0) {
                    //пока во внутреннем итераторе есть словари
                    while (inner_iterator.hasNext() && count > 0) {
                        DataSnapshot ds = (DataSnapshot) inner_iterator.next();
                        final UsersDictionary ud = new UsersDictionary();
                        //кладём собранные данные текущего словаря в объект
                        ud.name = ds.getKey();
                        ud.holderId =CURRENT_USER_ID;
                        DatabaseReference userRef = database.getReference("users").child(ud.holderId);
                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                ud.imageUrl = dataSnapshot.child("imageURL").getValue(String.class);
                                ud.holderName = dataSnapshot.child("username").getValue(String.class);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        //добавляем объекст к списку
                        list.add(ud);
                        count--;
                        if (count < 0) break;
                    }
                    //берем следующего пользователя, устанавливаем его как текущего и берём
                    //из него внутренний итератор для его словарей
                    DataSnapshot next = (DataSnapshot) iterator.next();
                    if(!next.getKey().equals(CURRENT_USER_ID)) CURRENT_USER_ID=next.getKey();
                    Iterable<DataSnapshot> snapit = next.getChildren();
                    inner_iterator = snapit.iterator();
                }
            return list;
        }
        //иначе пользователь ищет по выборке из searchview
        //тут почти то же самое, только проходимся не по всем словарям пользователя,
        //а только по тем, которые удовлетворяют условию
        else{
            final ArrayList<UsersDictionary> list = new ArrayList<>();

            DatabaseReference ref=database.getReference();
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> snapshotIterator = dataSnapshot.child("dictionaries").getChildren();
                    search_iterator = snapshotIterator.iterator();
                    final int[] count = {15};
                    final UsersDictionary[] ud = new UsersDictionary[1];
                    while (search_iterator.hasNext() && count[0] > 0) {
                        final DataSnapshot next = (DataSnapshot) search_iterator.next();
                        Query query=database.getReference("dictionaries").child(next.getKey()).orderByChild("name").startAt(text);
                        final int finalCount = count[0];
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot1) {
                                Iterable<DataSnapshot> snapit = dataSnapshot1.getChildren();
                                search_inner_iterator = snapit.iterator();
                                while (search_inner_iterator.hasNext() && finalCount > 0) {
                                    DataSnapshot ds = (DataSnapshot) search_inner_iterator.next();
                                    ud[0] = new UsersDictionary();
                                    ud[0].name = ds.getKey();
                                    ud[0].holderId = next.getKey();
                                    DataSnapshot userRef = dataSnapshot.child("users").child(ud[0].holderId);

                                    ud[0].imageUrl = userRef.child("imageURL").getValue().toString();
                                    ud[0].holderName = userRef.child("username").getValue().toString();

                                    if(ud[0]!=null&&ud[0].name!=null&&ud[0].holderId!=null&&
                                            ud[0].holderId!=null&&ud[0].imageUrl!=null) {
                                        list.add(ud[0]);
                                        count[0]--;
                                        if (count[0] < 0) break;
                                    }
                                }
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
            return list;
        }
    }
    //подгружаем во внешнем потоке
    public class ThreadGetData extends Thread{
        @Override
        public void run(){
            //передаём хэндлеру сообщения, чтобы он добавлял новую выборку словарей к массиву
            handler.sendEmptyMessage(0);
            ArrayList<UsersDictionary> new_list=getMoreData(isSearch,text);
            Message msg=handler.obtainMessage(1,new_list);
            handler.sendMessage(msg);

        }
    }
}
