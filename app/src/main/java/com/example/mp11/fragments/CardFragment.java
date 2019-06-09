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

import com.example.mp11.adapters.MyCustomAdapter;
import com.example.mp11.ForDatabases.MyDbHelper;
import com.example.mp11.R;
import com.example.mp11.ForDictionaries.StringTranslation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import link.fls.swipestack.SwipeStack;


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

    ImageButton btn_settings;
    ImageButton showbtn;


    private ArrayList<String> mData;
    private SwipeStack mSwipeStack;
    private SwipeStackAdapter mAdapter;
    private TextView word, anword;
    boolean visibility=false;
    ListView list;
    MyDbHelper databaseHelper;
    private ArrayList<StringTranslation> wordModelArrayList;
    private MyCustomAdapter customAdapter;
    String current_word;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Gson gson;

    int KNOWN_SESSION=0;
    int count=0;
    public static String CURRENT_DICT_NAME;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private OnFragmentInteractionListener mListener;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_card, container, false);

        mSwipeStack = (SwipeStack) view.findViewById(R.id.swipeStack);
        mButtonLeft = (Button) view.findViewById(R.id.buttonSwipeLeft);
        mButtonRight = (Button) view.findViewById(R.id.buttonSwipeRight);

        btn_settings=(ImageButton)view.findViewById(R.id.card_settings);





        btn_settings.setOnClickListener(this);
        mButtonLeft.setOnClickListener(this);
        mButtonRight.setOnClickListener(this);



        //массив с карточками(пользователь работу его не видит), но тут в следующих версиях будет собираться
        //кол-во повторённых слов за сеанс
        mData = new ArrayList<>();
        mAdapter = new SwipeStackAdapter(mData);
        mSwipeStack.setAdapter(mAdapter);
        mSwipeStack.setListener(this);

        //добавляем одну карточку на создании фрагмента
        mData.add("a");
        mAdapter.notifyDataSetChanged();

        //берем shared preferences, достаём оттуда очередь с изученными словами по названию выбранного словаря
        gson = new Gson();
        preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = preferences.edit();
        Type type = new TypeToken<ArrayDeque<String>>() {
        }.getType();
        ArrayDeque<String> deque=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-known",null),type);
        //если изученных слов ещё нет, создать очередь
        if(deque==null) deque=new ArrayDeque<>();
        //если размер очередь больше, чем кол-во слов, которые пользователь хочет повторять за сеанс, то установить
        //это кол-во таким, какое пользователь указал в настройках
        if(deque.size()>KNOWN_SESSION) KNOWN_SESSION= Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("wordsCount","5"));
        //иначе кол-во повторяемых слов равно размеру очереди
        else KNOWN_SESSION=deque.size();
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

    @Override
    public void onClick(View v) {
        //если нажата кнопка "Помню", перелистнуть карточку налево
        if (v.equals(mButtonLeft)) {
            mSwipeStack.swipeTopViewToLeft();



        } else
            //если нажата кнопка "Не помню", перелистнуть карточку вправо
            if (v.equals(mButtonRight)) {
            mSwipeStack.swipeTopViewToRight();



        } else if(v.equals(btn_settings)) {

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
        ArrayList<String> ar=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-rest",null),type);
        //если не пустой, удаляем оттуда слово, которое переместим в очередь с изученными словами
        if(ar!=null&&ar.size()!=0)ar.remove(tv.getText().toString());
        //записываем изменения
        editor.putString(CURRENT_DICT_NAME+"-rest",gson.toJson(ar));
        //берем очередь с изученными словами, а если её нет, создаём новую
        type = new TypeToken<ArrayDeque<String>>() {
        }.getType();
        ArrayDeque<String> deque=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-known",null),type);
        if(deque==null) deque=new ArrayDeque<>();
        //если пользователь всё ещё повторяет изученные слова в начале сеанса и очередь не пустая, то
        //перемещаем текущее слово из начала очереди в конец
        if(KNOWN_SESSION>0&&deque.size()!=0){
            deque.offer(deque.poll());
        }else {
            //иначе просто помещаем слово в конец очереди
            deque.offer(tv.getText().toString());
        }
        //записываем и сохраняем изменения
        editor.putString(CURRENT_DICT_NAME+"-known",gson.toJson(deque));
        editor.apply();
    }
    @Override
    public void onViewSwipedToRight(int position) {
        String swipedElement = mAdapter.getItem(position);
        Toast.makeText(getActivity(), getString(R.string.view_swiped_right), Toast.LENGTH_SHORT).show();
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
            //достаем из shared preferences очередь с изученными словами
            Type type = new TypeToken<ArrayDeque<String>>() {
            }.getType();
            ArrayDeque<String> deque=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-known",null),type);
            //если её нет, создаем
            if(deque==null) deque=new ArrayDeque<>();
            //если нет названия текущего словаря, достать его из shared preferences
            if(CURRENT_DICT_NAME==null||CURRENT_DICT_NAME.equals("")){
                CURRENT_DICT_NAME=preferences.getString("CURRENT_DICT_NAME",null);
            }
            //если и в shared preferences названия нет, то просто берём слова из всех словарей пользователя
            if(CURRENT_DICT_NAME==null||CURRENT_DICT_NAME.equals("")) {
                //получаем массив со словарями на устройстве
                String json = preferences.getString("dictionaries", null);
                String names[] = gson.fromJson(json, String[].class);
               //если массив пустой или него нет, то у пользователя ещё нет слов
                if (names != null && names.length != 0) {
                    String name = "";
                    String r = "У вас ещё нет слов";

                    try {
                        //циклом собираем слова
                        while (true) {
                            if (names != null) {
                                //рандомный словарь из массива со словарями
                                int random = (int) (Math.random() * (names.length));
                                name = names[random];
                                //открываем словарь по названию
                                databaseHelper = new MyDbHelper(getContext(), name);
                                //если словарь в бд пустой, то у пользователя ещё нет слов
                                if (databaseHelper.getAllWords().size() == 0) {
                                    textViewCard.setText("У вас ещё нет слов");
                                    wordModelArrayList = new ArrayList<StringTranslation>();
                                    wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                                    if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);
                                    break;
                                }
                                //иначе берём из словаря рандомное слово
                                else {

                                    int randomNumber = (int) (Math.random() * (databaseHelper.getAllWords().size()));
                                    if (databaseHelper.isOpened() && databaseHelper.getAllWords().size() != 0) {
                                        r = databaseHelper.getAllWords().get(randomNumber).getWord();
                                        wordModelArrayList = databaseHelper.getWord(r);
                                        break;

                                    }
                                }
                            }
                            //иначе у пользователя ещё нет слов
                            else {
                                textViewCard.setText("У вас ещё нет слов");
                                wordModelArrayList = new ArrayList<StringTranslation>();
                                wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                                if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                    //если произошла ошибка с открытием словаря, будем считать, что слов тоже нет
                    catch (Exception e) {
                        e.printStackTrace();
                        textViewCard.setText("У вас ещё нет слов");
                        wordModelArrayList = new ArrayList<StringTranslation>();
                        wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                        if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);
                    }
                    //адаптер для определений слова для списка
                    customAdapter = new MyCustomAdapter(getContext(), wordModelArrayList, r);
                    list.setAdapter(customAdapter);

                    textViewCard.setText(r);
                    //кнопка "показать слово"
                    showbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //берём верхнюю карточку в стеке, вернее её кнопку "показать слово"
                            ImageButton showbtn = (ImageButton) mSwipeStack.getTopView().findViewById(R.id.showwordbtn);
                            //берем список с верхней карточки стека
                            list = (ListView) mSwipeStack.getTopView().findViewById(R.id.word_list_card);
                            //если список невидим, показать его, а кнопку спрятать
                            if (list.getVisibility() != View.VISIBLE) {
                                list.setVisibility(View.VISIBLE);
                                showbtn.setVisibility(View.INVISIBLE);
                                //изменяем содержание массива, чтобы карточка динамически обновилась
                                mData.set(position, "lol");
                                notifyDataSetChanged();

                            }

                        }
                    });
                }
                //иначе у пользователя ещё нет слов
                else {
                    textViewCard.setText("У вас ещё нет слов");
                    if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);
                }
            }
            //если пользователь выбрал, из какого словаря изучать слова
            else{
                String r = "У вас ещё нет слов";
                //достаём из shared preferences массив с неизученными словами
                final Gson gson = new Gson();
                SharedPreferences preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = preferences.edit();
                type = new TypeToken<List<String>>() {
                }.getType();
                ArrayList<String> ar=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-rest",null),type);
                //открываем по имени словаря бд
                databaseHelper = new MyDbHelper(getContext(), CURRENT_DICT_NAME);
                //если пользователь ещё повторяет изученные слова и очередь с ними не пуста
                if(KNOWN_SESSION>0&&deque.size()!=0){
                    //достаём первое слово из очереди, но пока не удаляем его
                    r=deque.peek();
                    //ищем его в словаре и устанавливаем массиву, который передадим в адаптер
                    wordModelArrayList = databaseHelper.getWord(r);
                    //и уменьшаем счётчик
                    KNOWN_SESSION--;

                }
                //иначе пользователь закончил сеанс повторения уже изученных слов
                else
                    //если массив с неизученными словами пустой, то слов у пользователя новых нет
                    if(ar==null||ar.size()==0){
                    textViewCard.setText("У вас ещё нет слов");
                    wordModelArrayList = new ArrayList<StringTranslation>();
                    wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                    if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);

                    }
                    //иначе возьмём рандомно слово оттуда
                    else {
                    int randomNumber = (int) (Math.random() * (ar.size()));
                    if (ar.size() != 0) {
                        r = ar.get(randomNumber);
                        wordModelArrayList = databaseHelper.getWord(r);
                    }
                    //иначе у пользователя ещё нет слов
                    else {
                        textViewCard.setText("У вас ещё нет слов");
                        wordModelArrayList = new ArrayList<StringTranslation>();
                        wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                        if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);

                    }

                }

                    //прикручиваем полученный массив с определениями слова к адаптеру и списку
                customAdapter = new MyCustomAdapter(getContext(), wordModelArrayList, r);
                list.setAdapter(customAdapter);

                //показываем само слово на карточке
                textViewCard.setText(r);
                showbtn = (ImageButton) convertView.findViewById(R.id.showwordbtn);
                showbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //берём кнопку и список с верхней карточки стека
                        ImageButton showbtn = (ImageButton) mSwipeStack.getTopView().findViewById(R.id.showwordbtn);
                        list = (ListView) mSwipeStack.getTopView().findViewById(R.id.word_list_card);
                        //прячем кнопку и показываем список
                        if (list.getVisibility() != View.VISIBLE) {
                            list.setVisibility(View.VISIBLE);
                            showbtn.setVisibility(View.INVISIBLE);
                            mData.set(position, "lol");
                            notifyDataSetChanged();
                        }
                    }
                });


            }

            return convertView;
        }
    }

}
