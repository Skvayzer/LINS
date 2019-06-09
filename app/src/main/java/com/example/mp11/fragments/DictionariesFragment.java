package com.example.mp11.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.R;
import com.example.mp11.activities.GetAllWordsActivity;
import com.example.mp11.ForDictionaries.CategDictionary;
import com.example.mp11.ForDictionaries.StringTranslation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DictionariesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DictionariesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//фрагмент со словарями пользователя, которые есть на устройстве
public class DictionariesFragment extends Fragment implements ClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //тут хранятся сами словари
    public static ArrayList<CategDictionary> dictionaries;
    public static Context fragcontext;
    //для отображения словарей
    public static RecyclerView recyclerView;
    MyContentAdapter adapt;
    static GridLayoutManager layoutManager;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    //для всеобщего доступа к массиву названий словарей
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Gson gson=new Gson();
    public DictionariesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DictionariesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DictionariesFragment newInstance(String param1, String param2) {
        DictionariesFragment fragment = new DictionariesFragment();
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
        //сначала инициализируем массив и добавляем туда один элемент, который будет
        //псевдословарём, потому что по клику на него будет создаваться новый словарь
        dictionaries=new ArrayList<>();
        dictionaries.add(new CategDictionary(getContext(),"add new"));
        fragcontext=getContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        //адаптер для списка словарей
        MyContentAdapter adapter = new MyContentAdapter(recyclerView.getContext(),this);
        adapt=adapter;
        preferences = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor=preferences.edit();
        //прикручиваем адаптер и настраиваем вид recycleview
        recyclerView.setAdapter(adapt);
        recyclerView.setHasFixedSize(true);
        // отступ для плитки
        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
        recyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        layoutManager =  ((GridLayoutManager)recyclerView.getLayoutManager());
        loadDicts();

        return recyclerView;


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
    //фунция загрузки словарей
    public void loadDicts(){
        //берём массив с названиями словарей
        String json=preferences.getString("dictionaries",null);
        String[] names=gson.fromJson(json,String[].class);
        //если он не пустой
        if(names!=null)
            //пробегаемся по массиву
        for(int i=1;i<names.length&&dictionaries.size()<=names.length+1;i++){
            if(!names[i].equals("")) {
                //если такого словаря ещё нет в arrayList'e, добавить
                if(!dictionaries.contains(new CategDictionary(getContext(),names[i])))
                dictionaries.add(new CategDictionary(getContext(), names[i]));
            }
        }


    }

    //функция для действий, когда фрагмент стал виден
    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            onResume();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!getUserVisibleHint())
        {
            return;
        }
    }
    @Override
    public void onPause(){
        super.onPause();
    }
    //клик на словаре
    @Override
    public void onItemClick(int position, View v) {
        Context context=v.getContext();
        //если это первый словарь, который для ооздания других словарей
        if(position==0){
                //создать всплывающее окно, где надо ввести название и описание словаря
                AlertDialog.Builder pop = new AlertDialog.Builder(context);
                final AlertDialog kek=pop.create();
                final View current=LayoutInflater.from(context).inflate(R.layout.add_dict_name,null,false);
                current.setBackgroundColor(Color.WHITE);
                Button btn=(Button)current.findViewById(R.id.add_new_dict);
                //поля для ввода названия и описания
                final EditText et=(EditText)current.findViewById(R.id.type_new_dict);
                final EditText desc=(EditText)current.findViewById(R.id.new_dict_description);
                //нажал создать
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //берём название и описание
                        String name=et.getText().toString().toLowerCase();
                        String description=desc.getText().toString();
                        //если они не пустые
                        if(!name.equals("")&&!description.equals("")){
                            //добавить к массиву словарей словарь, который создали тут же
                            dictionaries.add(new CategDictionary(getContext(),name,description));


                            //достаём из shared preferences массив с названиями словарей
                            Type type = new TypeToken<List<String>>() {
                            }.getType();
                            ArrayList<String> ar=gson.fromJson(preferences.getString("dictionaries",null),type);
                            //если пустой, создать новый
                            if(ar==null||ar.size()==0){
                               ar=new ArrayList<>();
                            }
                            ar.add(name);
                            //сохраняем изменения
                            editor.putString("dictionaries",gson.toJson(ar));
                            editor.apply();
                            kek.hide();
                            adapt.notifyDataSetChanged();

                        }
                        //иначе поругать
                        else{
                            Toast.makeText(getContext(),"Заполните все поля!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //добавляем layout с полями ввода и кнопкой на окно
                kek.setView(current);
                kek.show();
        }
        //иначе это не первый словарь, тогда просто перейти на активность со словами словаря по названию
        else{
            Intent intent=new Intent(getContext(), GetAllWordsActivity.class);
            intent.putExtra("name",dictionaries.get(position).name);
            startActivity(intent);
        }
    }

    //когда пользователь зажал клик на словаре
    @Override
    public void onItemLongClick(final int position, View v) {
        //для первого словаря ничего не делать
        if(position!=0) {
            //берём из массива нажатый словарь
            final String name = dictionaries.get(position).name;
            //всплывающее окошко с настройками словаря
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Настройки словаря")
                    .setMessage("Что вы хотите сделать со словарём?")
                    .setPositiveButton("Изучать", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //пользователь хочет изучать словарь,
                            //тогда делаем текущим словарём во фрагменте с карточками
                            //словарь с выбранным именем
                            CardFragment.CURRENT_DICT_NAME = name;
                            //и кладём его в shared preferences
                            editor.putString("CURRENT_DICT_NAME",name);
                            editor.apply();
                        }
                    })
                    .setNegativeButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //пользователь хочет удалить словарь
                            //удаляем с устройства бд с этим словарём
                            getContext().deleteDatabase(name);
                            //достаём из shared preferences массив с названиями словарей пользователя
                            String json=preferences.getString("dictionaries",null);
                            Type type = new TypeToken<List<String>>() {
                            }.getType();
                            ArrayList<String> names=gson.fromJson(json,type);
                            //удаляем оттуда название удалённого словаря
                            names.remove(name);
                            //удаляем словарь из массива во фрагменте
                            dictionaries.remove(dictionaries.get(position));
                            //сохраняем и применяем изменения
                            editor.putString("dictionaries",gson.toJson(names));
                            editor.apply();
                            adapt.notifyDataSetChanged();
                        }
                    })
                    .create();
            dialog.show();
        }

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

    //адаптер для recycleview для словарей
    public static class MyContentAdapter extends RecyclerView.Adapter<MyContentAdapter.MyViewHolder> {
        //листенер для обработки кликов
        ClickListener mclickListener;
        View.OnClickListener mylistener;
        Context context;
        String text;
        ArrayList<StringTranslation> stlist;
        AlertDialog lol;
        //холдер для всех вьюшек элемента списка
        static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            ClickListener clickListener;
            public ImageView picture;
            public TextView name;
            //тип словаря(первый для создания других / обычный)
            int viewtype;


            public MyViewHolder(View v,ClickListener listener) {
                super(v);
                picture = (ImageView) itemView.findViewById(R.id.tile_picture);
                name = (TextView) itemView.findViewById(R.id.tile_title);
                //устанавливаем листенеры для элемента списка
                clickListener=listener;
                v.setOnClickListener(this);
                v.setOnLongClickListener(this);
            }
            //на клике вызываем определённую функцию из интерфейса
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(getAdapterPosition(), v);
            }

            @Override
            public boolean onLongClick(View v) {
                clickListener.onItemLongClick(getAdapterPosition(), v);
                return true;
            }
        }

        public void setClickListener(View.OnClickListener callback) {
            mylistener = callback;
        }
        //конструктор с листенером, который определяется в фрагменте
        public MyContentAdapter(Context context, ClickListener clistener) {
            mclickListener=clistener;
            this.context=context;
        }
        //конструктор адаптера для отображения словарей сервисом вне приложения
        public MyContentAdapter(Context context, ArrayList<StringTranslation> stlist, AlertDialog lol,String text) {
            this.context=context;
            this.stlist=stlist;
            this.lol=lol;
            this.text=text;
        }
        //когда создаётся элемент
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item,parent, false);
            //инициализируем холдер с листенером
            MyViewHolder holder=new MyViewHolder(v,mclickListener);
            holder.viewtype=viewType;
            //если тип элемента 1, то это первый  словарь-помошник
            if (viewType == 1) {
                //меняем ему текст и делаем серым
                holder.name.setText("Добавить новый словарь");
                holder.picture.setBackgroundColor(Color.rgb(150,150,150));

            }
            return holder;
        }

        //адаптер привязывается к сервису вне приложения
        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            //достаём из shared preferences массив с названиями словарей
            holder.clickListener=mclickListener;
            final Gson gson=new Gson();
            final SharedPreferences preferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor=preferences.edit();
            String json=preferences.getString("dictionaries",null);
            final String[] names=gson.fromJson(json,String[].class);
            if(names!=null&&names.length!=0) {
                //получаем имя словаря согласно позиции
                final String name = names[position];
                //если это не первый словарь-помошник, то меняем текст на нем на его название
                if (holder.viewtype != 1){
                    holder.name.setText(names[position]);
                }
                //если нет листенера
                if (mclickListener == null) {
                    if(holder.viewtype!=1)
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
                                //создаём словарь по имени
                                CategDictionary dict = new CategDictionary(context, names[position]);
                                //бросаем туда появившееся слово
                                dict.addWord(text, stlist);
                                //достаем из shared preferences массив с неизученными словами для данного словаря
                                String rest=preferences.getString(name +"-rest",null);
                                ArrayList<String> rest_ar;
                                //если не пустой
                                    if(rest!=null){
                                        Type type = new TypeToken<List<String>>() {
                                        }.getType();
                                        //десериализируем
                                        rest_ar=gson.fromJson(rest,type);
                                    }else{
                                        //иначе создаем новый
                                       rest_ar=new ArrayList<>();
                                    }
                                    //добавляем туда появившееся слово
                                rest_ar.add(text);
                                    //сохраняем
                                editor.putString(name+"-rest",gson.toJson(rest_ar));
                                editor.apply();
                                lol.hide();
                            }
                       // }
                    });
                    else{
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

//                            Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
//
//                            if (names != null) {
//                                CategDictionary dict = new CategDictionary(context, name);
//
//                                dict.addWord(text, stlist);
//
//                                lol.hide();
//                            }
                                //создать всплывающее окно, где надо ввести название и описание словаря
                                AlertDialog.Builder pop = new AlertDialog.Builder(context);

                                final AlertDialog kek=pop.create();
                                kek.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                final View current=LayoutInflater.from(context).inflate(R.layout.add_dict_name,null,false);
                                current.setBackgroundColor(Color.WHITE);
                                Button btn=(Button)current.findViewById(R.id.add_new_dict);
                                //поля для ввода названия и описания
                                final EditText et=(EditText)current.findViewById(R.id.type_new_dict);

                                final EditText desc=(EditText)current.findViewById(R.id.new_dict_description);

                                et.setHintTextColor(Color.GRAY);
                                et.setTextColor(Color.GRAY);
                                desc.setTextColor(Color.GRAY);
                                desc.setHintTextColor(Color.GRAY);
                                et.setHighlightColor(Color.BLUE);
                                desc.setHighlightColor(Color.BLUE);
                                //нажал создать
                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //берём название и описание
                                        String name=et.getText().toString().toLowerCase();
                                        String description=desc.getText().toString();
                                        //если они не пустые
                                        if(!name.equals("")&&!description.equals("")){
                                            //достаём из shared preferences массив с названиями словарей
                                            Type type = new TypeToken<List<String>>() {
                                            }.getType();
                                            ArrayList<String> ar=gson.fromJson(preferences.getString("dictionaries",null),type);
                                            //если пустой, создать новый
                                            if(ar==null||ar.size()==0){
                                                ar=new ArrayList<>();
                                            }
                                            ar.add(name);
                                            //сохраняем изменения
                                            editor.putString("dictionaries",gson.toJson(ar));
                                            editor.apply();
                                            kek.hide();
                                            notifyDataSetChanged();

                                        }
                                        //иначе поругать
                                        else{
                                            Toast.makeText(context,"Заполните все поля!",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                //добавляем layout с полями ввода и кнопкой на окно
                                kek.setView(current);
                                kek.show();
                            }
                        });
                    }
                }
            }else{

//                final String name = dictionaries.get(position).name;
//                if (holder.viewtype != 1) holder.name.setText(name);
//                else
                if (mclickListener == null) {

                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return 1;
            else return 0;
        }
        //кол-во словарей пользователя
        @Override
        public int getItemCount() {
            Gson gson=new Gson();
            SharedPreferences preferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=preferences.edit();
            String json=preferences.getString("dictionaries",null);
            String names[]=gson.fromJson(json,String[].class);
            if(names!=null) return names.length;
            return dictionaries.size();

        }

    }


    }
