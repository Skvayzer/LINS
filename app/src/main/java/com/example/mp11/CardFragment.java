package com.example.mp11;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.MyDatabase.MyCustomAdapter;
import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.MyDatabase.WordModel;
import com.example.mp11.views.CategDictionary;
import com.example.mp11.views.StringTranslation;
import com.example.mp11.views.TranslationAdapter;
import com.example.mp11.views.TranslationItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
    private FloatingActionButton mFab;

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
        // Required empty public constructor
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
       // editor=((MainActivity)getActivity()).editor;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_card, container, false);

        mSwipeStack = (SwipeStack) view.findViewById(R.id.swipeStack);
        mButtonLeft = (Button) view.findViewById(R.id.buttonSwipeLeft);
        mButtonRight = (Button) view.findViewById(R.id.buttonSwipeRight);
        mFab = (FloatingActionButton) view.findViewById(R.id.fabAdd);
        btn_settings=(ImageButton)view.findViewById(R.id.card_settings);





        btn_settings.setOnClickListener(this);
        mButtonLeft.setOnClickListener(this);
        mButtonRight.setOnClickListener(this);

        mFab.setOnClickListener(this);

        mData = new ArrayList<>();
        mAdapter = new SwipeStackAdapter(mData);
        mSwipeStack.setAdapter(mAdapter);
        mSwipeStack.setListener(this);


        mData.add("a");
//        mData.add("a");
//        mData.add("a");
        mAdapter.notifyDataSetChanged();


       gson = new Gson();
        preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);

        editor = preferences.edit();
        Type type = new TypeToken<ArrayDeque<String>>() {
        }.getType();
        ArrayDeque<String> deque=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-known",null),type);
        if(deque==null) deque=new ArrayDeque<>();
        if(deque.size()>KNOWN_SESSION) KNOWN_SESSION= Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("wordsCount","5"));
        else KNOWN_SESSION=deque.size();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuReset:
                mSwipeStack.resetStack();
                Snackbar.make(mFab, R.string.stack_reset, Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.menuGitHub:
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://github.com/skvayzer"));
                startActivity(browserIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (v.equals(mButtonLeft)) {
            mSwipeStack.swipeTopViewToLeft();

            mData.add("a");

            mAdapter.notifyDataSetChanged();
           // mData.remove(0);
        } else if (v.equals(mButtonRight)) {
            mSwipeStack.swipeTopViewToRight();

            mData.add("a");

            mAdapter.notifyDataSetChanged();
           // mData.remove(0);
        } else if (v.equals(mFab)) {
            mData.add("a");

            mAdapter.notifyDataSetChanged();
           // mData.remove(0);
        }else if(v.equals(btn_settings)) {

        }
    }

    @Override
    public void onViewSwipedToLeft(int position) {
        String swipedElement = mAdapter.getItem(position);
        Toast.makeText(getActivity(), getString(R.string.view_swiped_left) , Toast.LENGTH_SHORT).show();
        mData.add("a");

        mAdapter.notifyDataSetChanged();
       // mData.remove(0);


       // CategDictionary cur=new CategDictionary(getContext(), CURRENT_DICT_NAME +"-known-words");
        TextView tv=mSwipeStack.getTopView().findViewById(R.id.current_word_card);
//        cur.addWord(tv.getText().toString(),wordModelArrayList,true);
//        databaseHelper.deleteWord(tv.getText().toString());


        Type type = new TypeToken<List<String>>() {
        }.getType();
        ArrayList<String> ar=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-rest",null),type);
       // if(ar==null) ar=new ArrayList<>();
        ar.remove(tv.getText().toString());

        editor.putString(CURRENT_DICT_NAME+"-rest",gson.toJson(ar));
        type = new TypeToken<ArrayDeque<String>>() {
        }.getType();
        ArrayDeque<String> deque=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-known",null),type);
        if(deque==null) deque=new ArrayDeque<>();
//        if(deque.size()>KNOWN_SESSION) KNOWN_SESSION=5;
//        else KNOWN_SESSION=deque.size();
        if(KNOWN_SESSION>0&&deque.size()!=0){
            deque.offer(deque.poll());
        }else {
            deque.offer(tv.getText().toString());
        }
        editor.putString(CURRENT_DICT_NAME+"-known",gson.toJson(deque));
        editor.apply();
    }
    @Override
    public void onViewSwipedToRight(int position) {
        String swipedElement = mAdapter.getItem(position);
        Toast.makeText(getActivity(), getString(R.string.view_swiped_right), Toast.LENGTH_SHORT).show();
        mData.add("a");

        mAdapter.notifyDataSetChanged();
       // mData.remove(0);


    }

    @Override
    public void onStackEmpty() {
       // Toast.makeText(getActivity(), R.string.stack_empty, Toast.LENGTH_SHORT).show();
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

        public View getView(View view){

            return view;
        }



//        public ArrayList<StringTranslation> getWordsFromDict(MyDbHelper db){
//            databaseHelper = new MyDbHelper(getContext(), CURRENT_DICT_NAME);
//
//
//            if (databaseHelper.getAllWords().size() == 0) {
//
//                wordModelArrayList = new ArrayList<StringTranslation>();
//                wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
//                return wordModelArrayList;
//            } else {
//
//                //if(wordModelArrayList.size()!=0) {}
//                int randomNumber = (int) (Math.random() * (databaseHelper.getAllWords().size()));
//                if (databaseHelper.isOpened() && databaseHelper.getAllWords().size() != 0) {
//                    r = databaseHelper.getAllWords().get(randomNumber).getWord();
//
//                    //  if(known_words==null){
//                    wordModelArrayList = databaseHelper.getWord(r);
//                    break;
//                    //  }
//        }
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

                convertView = getLayoutInflater().inflate(R.layout.card, parent, false);


            TextView textViewCard = (TextView) convertView.findViewById(R.id.current_word_card);
            textViewCard.setText(mData.get(position));

            anword=(TextView) convertView.findViewById(R.id.textViewCardanother);
            list=(ListView)convertView.findViewById(R.id.word_list_card);
            Type type = new TypeToken<ArrayDeque<String>>() {
            }.getType();
            ArrayDeque<String> deque=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-known",null),type);
            if(deque==null) deque=new ArrayDeque<>();

            if(CURRENT_DICT_NAME==null||CURRENT_DICT_NAME.equals("")){
                CURRENT_DICT_NAME=preferences.getString("CURRENT_DICT_NAME",null);
            }
            if(CURRENT_DICT_NAME==null||CURRENT_DICT_NAME.equals("")) {
//                final Gson gson = new Gson();
//                SharedPreferences preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
//
//                final SharedPreferences.Editor editor = preferences.edit();
                //    final String known_json=preferences.getString("known_words",null);
                String json = preferences.getString("dictionaries", null);
                String names[] = gson.fromJson(json, String[].class);
                //   final ArrayList<String> known_words=gson.fromJson(known_json,ArrayList.class);
                if (names != null && names.length != 0) {
                    String name = "";
                    String r = "У вас ещё нет слов";
//                MyDbHelper databaseHelper;
                    try {

                        while (true) {
                            if (names != null) {
                                int random = (int) (Math.random() * (names.length));
                              //  if(random==0)continue;
                                name = names[random];
                                 databaseHelper = new MyDbHelper(getContext(), name);
                                //     MyDbHelper databaseHelper = new MyDbHelper(getContext(),name);
                                //databaseHelper = new MyDbHelper(getContext(), name + "-rest-unknown");



                                if (databaseHelper.getAllWords().size() == 0) {

                                    textViewCard.setText("У вас ещё нет слов");
                                    wordModelArrayList = new ArrayList<StringTranslation>();
                                    wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                                    if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);
                                    break;
                                } else {

                                    //if(wordModelArrayList.size()!=0) {}
                                    int randomNumber = (int) (Math.random() * (databaseHelper.getAllWords().size()));
                                    if (databaseHelper.isOpened() && databaseHelper.getAllWords().size() != 0) {
                                        r = databaseHelper.getAllWords().get(randomNumber).getWord();

                                        //  if(known_words==null){
                                        wordModelArrayList = databaseHelper.getWord(r);
                                        break;
                                        //  }
//                        else if(!known_words.contains(r)) {
//                            wordModelArrayList = databaseHelper.getWord(r);
//                            break;
//                        }
                                    }
                                }
                            } else {
                                textViewCard.setText("У вас ещё нет слов");
                                wordModelArrayList = new ArrayList<StringTranslation>();
                                wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                                if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);
                            }
//                        } else {
//                            textViewCard.setText("У вас ещё нет слов");
//                            wordModelArrayList = new ArrayList<StringTranslation>();
//                            wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
//                            break;
//                        }
                            //  databaseHelper.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        textViewCard.setText("У вас ещё нет слов");
                        wordModelArrayList = new ArrayList<StringTranslation>();
                        wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                        if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);
                    }

                    // Random random=new Random();
                    //int randomNumber = random.ints(0,(wordModelArrayList.size()+1)).findFirst().getAsInt();
                    // String curword=wordModelArrayList.get(randomNumber).getWord();
                    // ArrayList<StringTranslation> nowlist = new ArrayList<StringTranslation>();
//            for(WordModel p: wordModelArrayList){
//                if(p.getWord().equals(curword)){
//                    nowlist.add(p);
//                }
//            }

                    customAdapter = new MyCustomAdapter(getContext(), wordModelArrayList, r);
                    list.setAdapter(customAdapter);

                    textViewCard.setText(r);

                    showbtn = (ImageButton) convertView.findViewById(R.id.showwordbtn);
                    final View view = convertView;
                    final ViewGroup parent1 = parent;
//
//                    final String finalR = r;
//                    final String finalName = name;
//                    final MyDbHelper finalDatabaseHelper = databaseHelper;

//                ImageButton sound=(ImageButton)view.findViewById(R.id.sound_btn);
//                sound.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        TextToSpeech tts = new TextToSpeech(getContext(), CardFragment.this);
//                        tts.setLanguage(Locale.US);
//                        tts.speak(finalR,TextToSpeech.QUEUE_ADD, null);
//
//                    }
//                });
                    showbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            ImageButton showbtn = (ImageButton) mSwipeStack.getTopView().findViewById(R.id.showwordbtn);

                            list = (ListView) mSwipeStack.getTopView().findViewById(R.id.word_list_card);
                            if (list.getVisibility() != View.VISIBLE) {
                                list.setVisibility(View.VISIBLE);
                                showbtn.setVisibility(View.INVISIBLE);

                                // Toast.makeText(getContext(), "lol", Toast.LENGTH_SHORT).show();
                                mData.set(position, "lol");
                                notifyDataSetChanged();

                                // CategDictionary cur=new CategDictionary(getContext(), finalName +"-known-words");
                                // cur.addWord(finalR,wordModelArrayList,true);
                                // finalDatabaseHelper.deleteWord(finalR);
//                            String json=null;
//                            if(known_words!=null){
//                                known_words.add(finalR);
//                                json=gson.toJson(known_words);
//                            }
//
//                            else{
//                                ArrayList<String> words=new ArrayList<>();
//                                words.add(finalR);
//                                json=gson.toJson(words);
//                            }
//
//                            editor.putString("known_words",json);
//                            editor.apply();
                                // mSwipeStack.removeView(view);


                                //getView(mSwipeStack.getCurrentPosition(),mSwipeStack.getTopView(),null);


                            }

                        }
                    });
                } else {
                    textViewCard.setText("У вас ещё нет слов");
                    if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);
                }
            }
//            else if(deque.size()!=0&&count<5){
////
////            }
            else{
                String r = "У вас ещё нет слов";
                final Gson gson = new Gson();
                SharedPreferences preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);

                final SharedPreferences.Editor editor = preferences.edit();
                type = new TypeToken<List<String>>() {
                }.getType();
                ArrayList<String> ar=gson.fromJson(preferences.getString(CURRENT_DICT_NAME+"-rest",null),type);

                //databaseHelper = new MyDbHelper(getContext(), CURRENT_DICT_NAME + "-rest-unknown");
                databaseHelper = new MyDbHelper(getContext(), CURRENT_DICT_NAME);
                //if (databaseHelper.getAllWords().size() == 0) {

                if(KNOWN_SESSION>0&&deque.size()!=0){
                    r=deque.peek();
                    wordModelArrayList = databaseHelper.getWord(r);
                    KNOWN_SESSION--;

                }else
                if(ar==null||ar.size()==0){
                    textViewCard.setText("У вас ещё нет слов");
                    wordModelArrayList = new ArrayList<StringTranslation>();
                    wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                    if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);

                } else {



                    //if(wordModelArrayList.size()!=0) {}
                  //  int randomNumber = (int) (Math.random() * (databaseHelper.getAllWords().size()));
                    int randomNumber = (int) (Math.random() * (ar.size()));
                    if (ar.size() != 0) {
                        r = ar.get(randomNumber);


                        wordModelArrayList = databaseHelper.getWord(r);


                    }else {
                        textViewCard.setText("У вас ещё нет слов");
                        wordModelArrayList = new ArrayList<StringTranslation>();
                        wordModelArrayList.add(new StringTranslation("У вас ещё нет слов", "", "", ""));
                        if(showbtn!=null)showbtn.setVisibility(View.INVISIBLE);

                    }

                }
                customAdapter = new MyCustomAdapter(getContext(), wordModelArrayList, r);
                list.setAdapter(customAdapter);

                textViewCard.setText(r);
                showbtn = (ImageButton) convertView.findViewById(R.id.showwordbtn);
                //final String finalR = r;
                showbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ImageButton showbtn = (ImageButton) mSwipeStack.getTopView().findViewById(R.id.showwordbtn);

                        list = (ListView) mSwipeStack.getTopView().findViewById(R.id.word_list_card);
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
