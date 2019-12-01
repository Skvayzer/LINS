package com.example.mp11.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.activities.MainActivity;
import com.example.mp11.adapters.MyCustomAdapter;
import com.example.mp11.ForDatabases.MyDbHelper;
import com.example.mp11.R;
import com.example.mp11.ForDictionaries.StringTranslation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import link.fls.swipestack.SwipeStack;

import static com.example.mp11.activities.MainActivity.userId;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment extends Fragment implements SwipeStack.SwipeStackListener, View.OnClickListener,TextToSpeech.OnInitListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button mButtonLeft, mButtonRight;


    private ImageButton showbtn;


    private ArrayList<String> mData;
    private SwipeStack mSwipeStack;
    private SwipeStackAdapter mAdapter;
    private TextView word, anword;
    boolean visibility=false;
    private  ListView list;
    private MyDbHelper databaseHelper;
    private String lastWord="";
    private ArrayList<StringTranslation> wordModelArrayList;
    private MyCustomAdapter customAdapter;

    private SharedPreferences preferences;
    private  SharedPreferences.Editor editor;
    private  Gson gson;


    //выбранный словарь
    public static String CURRENT_DICT_NAME;
    String current_dict;

    public static TextToSpeech tts;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private OnFragmentInteractionListener mListener;



    public static HashMap<String, ArrayDeque<String>> map=new HashMap<>();
    //должен по всем словарям продвигатсься !!!
    public ArrayList<String> dictNames=null;
    public static ArrayDeque<String> levelTimeDeque=null;
    public static int dictNum=0;




    public CardFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CardFragment newInstance(String param1, String param2) {
        CardFragment fragment = new CardFragment();
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
        gson = new Gson();
        preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = preferences.edit();

        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                        Toast.makeText(getContext(), "Язык не поддерживается", Toast.LENGTH_SHORT).show();


                }
            }
        });
        tts.setLanguage(Locale.US);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card, container, false);

        mSwipeStack = (SwipeStack) view.findViewById(R.id.swipeStack);
        mButtonLeft = (Button) view.findViewById(R.id.buttonSwipeLeft);
        mButtonRight = (Button) view.findViewById(R.id.buttonSwipeRight);





        mButtonLeft.setOnClickListener(this);
        mButtonRight.setOnClickListener(this);


        mData = new ArrayList<>();
        mAdapter = new SwipeStackAdapter(mData);
        mSwipeStack.setAdapter(mAdapter);
        mSwipeStack.setListener(this);

        //добавляем одну карточку на создании фрагмента
        mData.add("a");
        mAdapter.notifyDataSetChanged();


        levelTimeDeque=gson.fromJson(preferences.getString("levelTimeDeque",null),new TypeToken<ArrayDeque<String>>() {
        }.getType());

        return view;
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        if (visible) {
            //Do your stuff here
        }else{

        }

        super.setMenuVisibility(visible);
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

    @Override
    public void onClick(View v) {
        //если нажата кнопка "Помню", перелистнуть карточку налево
        if (v.equals(mButtonLeft)) {
            mSwipeStack.swipeTopViewToLeft();
        } else
            //если нажата кнопка "Не помню", перелистнуть карточку вправо
            if (v.equals(mButtonRight)) {
            mSwipeStack.swipeTopViewToRight();
        }
    }

    @Override
    public void onViewSwipedToLeft(int position) {

        Toast.makeText(getActivity(), getString(R.string.view_swiped_left) , Toast.LENGTH_SHORT).show();
        //добавить новую карточку
        mData.add("a");
        mAdapter.notifyDataSetChanged();

        TextView tv=mSwipeStack.getTopView().findViewById(R.id.current_word_card);

        //берем из shared preferences массивы с неизученными и изученными словами
        Type type = new TypeToken<List<String>>() {
        }.getType();
        ArrayList<String> ar;
        if(dictNames!=null && dictNames.size()!=0&&CURRENT_DICT_NAME==null)
        ar=gson.fromJson(preferences.getString(dictNames.get(dictNum)+"-rest",null),type);
        else ar=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-rest",null),type);
        //если не пустой, удаляем оттуда слово, которое переместим в очередь с изученными словами
        if(ar!=null&&ar.size()!=0)ar.remove(tv.getText().toString());
        //записываем изменения
        String name=CURRENT_DICT_NAME;
        if(dictNames!=null&&dictNames.size()!=0) {
            editor.putString(dictNames.get(dictNum) + "-rest", gson.toJson(ar));
            name=dictNames.get(dictNum);
        }
        else
        editor.putString(CURRENT_DICT_NAME+"-rest",gson.toJson(ar));


        //в дальнейшую контейнер кладутся слова для перемещения их на уровень вперед
        if(name!=null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();

            DatabaseReference ref = db.getReference().child("dictionaries").child(userId).child(name).child("rest");
            ref.setValue(ar);
        }
        if(MainActivity.current_session!=null){
            if (MainActivity.further_container.containsKey(dictNames.get(dictNum))) {
                MainActivity.further_container.get(dictNames.get(dictNum)).offer(MainActivity.current_session.poll());
            }else{
                ArrayDeque<String> ad=new ArrayDeque<>();
                ad.offer(MainActivity.current_session.poll());
                MainActivity.further_container.put(dictNames.get(dictNum), ad);
            }

        }else{
            if(CURRENT_DICT_NAME!=null) {
                if (MainActivity.lost_container.get(CURRENT_DICT_NAME) != null) {
                    MainActivity.lost_container.get(CURRENT_DICT_NAME).offer(tv.getText().toString());
                } else {
                    ArrayDeque<String> ad = new ArrayDeque<>();
                    ad.offer(tv.getText().toString());
                    MainActivity.lost_container.put(CURRENT_DICT_NAME, ad);
                }
            }else{
                if (MainActivity.lost_container.get(current_dict) != null) {
                    MainActivity.lost_container.get(current_dict).offer(tv.getText().toString());
                } else {
                    ArrayDeque<String> ad = new ArrayDeque<>();
                    ad.offer(tv.getText().toString());
                    MainActivity.lost_container.put(current_dict, ad);
                }
            }
        }


        //записываем и сохраняем изменения
        editor.apply();
    }
    @Override
    public void onViewSwipedToRight(int position) {
        Toast.makeText(getActivity(), getString(R.string.view_swiped_right), Toast.LENGTH_SHORT).show();
        if(MainActivity.current_session!=null){
            if(MainActivity.lost_container.get(dictNames.get(dictNum))!=null)
            MainActivity.lost_container.get(dictNames.get(dictNum)).offer(MainActivity.current_session.poll());
            else{
                ArrayDeque<String> ad=new ArrayDeque<String>();
                ad.offer(MainActivity.current_session.poll());
                MainActivity.lost_container.put(dictNames.get(dictNum),ad);
            }
        }

        //если пользователь свайпнул вправо, создать новую карточку
        mData.add("a");
        mAdapter.notifyDataSetChanged();



    }

    @Override
    public void onStackEmpty() {

    }

    @Override
    public void onInit(int status) {

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
    //адаптер для стека с карточками
    public class SwipeStackAdapter extends BaseAdapter {

        private List<String> mData;

        public SwipeStackAdapter(List<String> data) {
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public String getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        public void update(){

        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.card, parent, false);
            TextView textViewCard = (TextView) convertView.findViewById(R.id.current_word_card);
            anword=(TextView) convertView.findViewById(R.id.textViewCardanother);
            list=(ListView)convertView.findViewById(R.id.word_list_card);
            showbtn = (ImageButton) convertView.findViewById(R.id.showwordbtn);
            ImageButton sound_btn=(ImageButton)convertView.findViewById(R.id.sound_btn_card);
            if(levelTimeDeque!=null&&levelTimeDeque.size()!=0) {
                String[] s=levelTimeDeque.peek().split(" ");
                int level=Integer.parseInt(s[0]);
                long time=Long.parseLong(s[1]);
                //если в текущая сессия со словами пустая, попытаться достать слова из следующего словаря
                if (MainActivity.current_session == null) {

                    map = ((MainActivity) getActivity()).ebbyCurve.get(level).get(time);

                    if(map!=null) {
                        if (dictNames == null) {
                            dictNames = new ArrayList<String>(Arrays.asList(map.keySet().toArray(new String[map.keySet().size()])));
                            dictNum = 0;
                        }

                        if (dictNum < dictNames.size()) {
                            MainActivity.current_session = map.get(dictNames.get(dictNum));
                            FirebaseDatabase db = FirebaseDatabase.getInstance();

                            DatabaseReference ref = db.getReference().child("dictionaries").child(userId).child(dictNames.get(dictNum)).child("rest");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dictNames!=null&&dictNum<dictNames.size()) {
                                        GenericTypeIndicator<ArrayList<String>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {
                                        };
                                        editor.putString(dictNames.get(dictNum) + "-rest", gson.toJson(dataSnapshot.getValue(genericTypeIndicator)));
                                        editor.apply();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                    }
                }
            }
            if(MainActivity.current_session!=null){
                //если текущая сессия закончилась, сохранить все результаты
            if(MainActivity.current_session.size()==0){



                if(dictNum+1<dictNames.size()) {
                    map.remove(dictNames.get(dictNum));
                    MainActivity.current_session = map.get(dictNames.get(++dictNum));
                    editor.putString("ebbyCurve",gson.toJson(((MainActivity)getActivity()).ebbyCurve));
                    editor.apply();

                }else{
                        String[] s=levelTimeDeque.peek().split(" ");
                        int level=Integer.parseInt(s[0]);
                        long time=Long.parseLong(s[1]);
                        MainActivity.current_session = null;
                        dictNames = null;



                        ((MainActivity) getActivity()).saveFurtherWords(level);
                        ((MainActivity) getActivity()).ebbyCurve.get(level).remove(time);
                        levelTimeDeque.poll();
                        editor.putString("ebbyCurve", gson.toJson(((MainActivity) getActivity()).ebbyCurve));

                        editor.putString("levelTimeDeque", gson.toJson(levelTimeDeque));

                        editor.apply();
                        MainActivity.further_container = new HashMap<>();
                        if(levelTimeDeque.size()!=0) {
                            s = levelTimeDeque.peek().split(" ");
                            level = Integer.parseInt(s[0]);
                            time = Long.parseLong(s[1]);
                            if (((MainActivity) getActivity()).ebbyCurve.get(level).get(time) != null) {
                                if (MainActivity.current_session == null) {
                                    map = ((MainActivity) getActivity()).ebbyCurve.get(level).get(time);

                                    if (dictNames == null) {
                                        dictNames = new ArrayList<String>(Arrays.asList(map.keySet().toArray(new String[map.keySet().size()])));
                                        dictNum = 0;
                                    }

                                    if (dictNum < dictNames.size()) {
                                        MainActivity.current_session = map.get(dictNames.get(dictNum));

                                    } else {

                                    }
                                }
                                getEbbyWord(convertView, textViewCard, sound_btn, position);
                            } else {
                                searchNewWord(convertView, textViewCard, sound_btn, position);
                            }
                        }else {
                            searchNewWord(convertView, textViewCard, sound_btn, position);
                        }
                }

            }else{
                getEbbyWord(convertView,textViewCard,sound_btn,position);
            }
            }else {
              searchNewWord(convertView,textViewCard,sound_btn,position);
            }

            return convertView;
        }
        //добыть слово из кривой забывания
        public void getEbbyWord(View convertView, TextView textViewCard, ImageButton sound_btn, final int position){
            String r=MainActivity.current_session.peek();
            String dictName=dictNames.get(dictNum);
            databaseHelper = new MyDbHelper(getContext(), dictName);
            wordModelArrayList = databaseHelper.getWord(r);
            customAdapter = new MyCustomAdapter(getContext(), wordModelArrayList, r);
            list.setAdapter(customAdapter);
            textViewCard.setText(r);
            final String finalR = r;
            sound_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tts.speak(finalR, TextToSpeech.QUEUE_FLUSH, null);
                }

            });
            //кнопка "показать слово"
            showbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //берём верхнюю карточку в стеке, вернее её кнопку "показать слово"
                    ImageButton showbtn = (ImageButton) mSwipeStack.getTopView().findViewById(R.id.showwordbtn);
                    //берем список с верхней карточки стека
                    list = (ListView) mSwipeStack.getTopView().findViewById(R.id.word_list_card);
                    //если список невидим, показать его, а кнопку спрятать

                    list.setVisibility(View.VISIBLE);
                    showbtn.setVisibility(View.INVISIBLE);
                    //изменяем содержание массива, чтобы карточка динамически обновилась
                    mData.set(position, "lol");
                    notifyDataSetChanged();


                }
            });
        }
        //взять новое слово из словаря
        public void searchNewWord(View convertView, TextView textViewCard, ImageButton sound_btn, final int position){
            //если нет названия текущего словаря, достать его из shared preferences

            CURRENT_DICT_NAME = preferences.getString("CURRENT_DICT_NAME", null);

            //если и в shared preferences названия нет, то просто берём слова из всех словарей пользователя
            if (CURRENT_DICT_NAME == null || CURRENT_DICT_NAME.equals("")) {
                //получаем массив со словарями на устройстве
                String json = preferences.getString("dictionaries", null);
                Type type = new TypeToken<List<String>>() {
                }.getType();
                ArrayList<String> names = gson.fromJson(json, type);

                //если массив пустой или него нет, то у пользователя ещё нет слов
                if (names != null && names.size() != 0) {
                    String name = null;
                    String r = getString(R.string.you_have_no_words);



                    try {
                        //циклом собираем слова
                        while (true) {
                            if(names.size()==0){
                                textViewCard.setText(getString(R.string.you_have_no_words));
                                current_dict=null;

                                if (showbtn != null) showbtn.setVisibility(View.INVISIBLE);
                                if (sound_btn != null) sound_btn.setVisibility(View.INVISIBLE);
                            }
                            //рандомный словарь из массива со словарями
                            int random = (int) (Math.random() * (names.size()));
                            name = names.get(random);
                            //открываем словарь по названию
                            databaseHelper = new MyDbHelper(getContext(), name);
                            //если словарь в бд пустой, то у пользователя ещё нет слов
                            if (databaseHelper.getAllWords().size() == 0) {

                                names.remove(name);


                            }
                            //иначе берём из словаря рандомное слово
                            else {
                                current_dict=name;
                                int randomNumber = (int) (Math.random() * (databaseHelper.getAllWords().size()));
                                if (databaseHelper.isOpened() && databaseHelper.getAllWords().size() != 0) {
                                    r = databaseHelper.getAllWords().get(randomNumber).getWord();
                                    wordModelArrayList = databaseHelper.getWord(r);
                                    break;

                                }
                            }


                        }
                    }
                    //если произошла ошибка с открытием словаря, будем считать, что слов тоже нет
                    catch (Exception e) {
                        e.printStackTrace();
                        textViewCard.setText(getString(R.string.you_have_no_words));
                        if (showbtn != null) showbtn.setVisibility(View.INVISIBLE);
                        if (sound_btn != null) sound_btn.setVisibility(View.INVISIBLE);
                    }

                    FirebaseDatabase db=FirebaseDatabase.getInstance();

                    DatabaseReference ref=db.getReference().child("dictionaries").child(userId).child(name).child("rest");
                    final String finalName = name;
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GenericTypeIndicator<ArrayList<String>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                            editor.putString(finalName + "-rest", gson.toJson(dataSnapshot.getValue(genericTypeIndicator)));
                            editor.apply();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    //адаптер для определений слова для списка
                    customAdapter = new MyCustomAdapter(getContext(), wordModelArrayList, r);
                    list.setAdapter(customAdapter);

                    textViewCard.setText(r);
                    final String finalR = r;
                    sound_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tts.speak(finalR, TextToSpeech.QUEUE_FLUSH, null);
                        }

                    });
                    //кнопка "показать слово"
                    showbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //берём верхнюю карточку в стеке, вернее её кнопку "показать слово"
                            ImageButton showbtn = (ImageButton) mSwipeStack.getTopView().findViewById(R.id.showwordbtn);
                            //берем список с верхней карточки стека
                            list = (ListView) mSwipeStack.getTopView().findViewById(R.id.word_list_card);
                            //если список невидим, показать его, а кнопку спрятать

                            list.setVisibility(View.VISIBLE);
                            showbtn.setVisibility(View.INVISIBLE);
                            //изменяем содержание массива, чтобы карточка динамически обновилась
                            mData.set(position, "lol");
                            notifyDataSetChanged();


                        }
                    });
                }
                //иначе у пользователя ещё нет слов
                else {
                    current_dict=null;
                    textViewCard.setText(getString(R.string.you_have_no_words));
                    if (showbtn != null) showbtn.setVisibility(View.INVISIBLE);
                    if (sound_btn != null) sound_btn.setVisibility(View.INVISIBLE);
                }
            }
            //если пользователь выбрал, из какого словаря изучать слова
            else {

                String r = getString(R.string.you_have_no_words);
                //достаём из shared preferences массив с неизученными словами
                final Gson gson = new Gson();
                SharedPreferences preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = preferences.edit();
                Type type = new TypeToken<List<String>>() {
                }.getType();
                ArrayList<String> ar = gson.fromJson(preferences.getString(CURRENT_DICT_NAME + "-rest", null), type);
                //открываем по имени словаря бд
                databaseHelper = new MyDbHelper(getContext(), CURRENT_DICT_NAME);


                //если массив с неизученными словами пустой, то слов у пользователя новых нет
                if (ar == null || ar.size() == 0) {
                    textViewCard.setText(r);
                    current_dict=null;
                    if (showbtn != null) showbtn.setVisibility(View.INVISIBLE);
                    if (sound_btn != null) sound_btn.setVisibility(View.INVISIBLE);
                }
                //иначе возьмём рандомно слово оттуда
                else {
                    while (true) {
                        int randomNumber = (int) (Math.random() * (ar.size()));
                        if (ar.size() != 0) {
                            r = ar.get(randomNumber);
                            wordModelArrayList = databaseHelper.getWord(r);
                            if (r!=null&&(!r.equals(lastWord) || ar.size() == 1)) {
                                lastWord = r;
                                break;
                            }

                        }
                    }
                }

                FirebaseDatabase db=FirebaseDatabase.getInstance();

                DatabaseReference ref=db.getReference().child("dictionaries").child(userId).child(CURRENT_DICT_NAME).child("rest");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<ArrayList<String>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                        editor.putString(CURRENT_DICT_NAME + "-rest", gson.toJson(dataSnapshot.getValue(genericTypeIndicator)));
                        editor.apply();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //прикручиваем полученный массив с определениями слова к адаптеру и списку
                customAdapter = new MyCustomAdapter(getContext(), wordModelArrayList, r);
                list.setAdapter(customAdapter);

                //показываем само слово на карточке
                textViewCard.setText(r);
                final String finalR = r;
                sound_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        tts.speak(finalR, TextToSpeech.QUEUE_FLUSH, null);
                    }

                });
                showbtn = (ImageButton) convertView.findViewById(R.id.showwordbtn);
                showbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //берём кнопку и список с верхней карточки стека
                        ImageButton showbtn = (ImageButton) mSwipeStack.getTopView().findViewById(R.id.showwordbtn);
                        list = (ListView) mSwipeStack.getTopView().findViewById(R.id.word_list_card);
                        //прячем кнопку и показываем список
                        list.setVisibility(View.VISIBLE);
                        showbtn.setVisibility(View.INVISIBLE);
                        mData.set(position, "lol");
                        notifyDataSetChanged();

                    }
                });


            }
        }
    }

    //revealing confine
}
