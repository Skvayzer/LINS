package com.example.mp11;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.FirebaseDbHelper.FirebaseDbHelper;
import com.example.mp11.views.CategDictionary;
import com.example.mp11.views.StringTranslation;
import com.google.firebase.database.GenericTypeIndicator;
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
public class DictionariesFragment extends Fragment implements ClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static ArrayList<CategDictionary> dictionaries;
    public static Context fragcontext;

    public static RecyclerView recyclerView;
    MyContentAdapter adapt;
   // static Context context;
    static GridLayoutManager layoutManager;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

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
        MyContentAdapter adapter = new MyContentAdapter(recyclerView.getContext(),this);
        adapt=adapter;
//        adapter.setOnItemClickListener(new MyContentAdapter.ClickListener() {
//            @Override
//            public void onItemClick(int position, View v) {
//                Log.d("Click", "onItemClick position: " + position);
//                Context context=v.getContext();
//                if(position==0){
//                    AlertDialog.Builder pop = new AlertDialog.Builder(context);
//                AlertDialog kek=pop.create();
//                View view=LayoutInflater.from(context).inflate(R.layout.pop_word,null);
//                view.setBackgroundColor(Color.WHITE);
//
//                kek.setView(v);
//                kek.show();
//                }
//            }

//            @Override
//            public void onItemLongClick(int position, View v) {
//                Log.d("Long Click", "onItemLongClick pos = " + position);
//
//            }
 //       });

//        adapter.setClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int pos = recyclerView.indexOfChild(v);
//                Log.d("Click", "onItemClick position: " + pos);
//                Toast.makeText(getContext(),"AAA", Toast.LENGTH_SHORT);
//
//            }
//        });
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
    public void loadDicts(){
        Gson gson=new Gson();
        SharedPreferences preferences = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        String json=preferences.getString("dictionaries",null);
        String[] names=gson.fromJson(json,String[].class);

        if(names!=null)
        for(int i=1;i<names.length&&dictionaries.size()<=names.length+1;i++){
            if(!names[i].equals("")) {
                if(!dictionaries.contains(new CategDictionary(getContext(),names[i])))
                dictionaries.add(new CategDictionary(getContext(), names[i]));
            }
        }


    }
    public void saveDicts(){
        Gson gson=new Gson();
        String[] names=new String[dictionaries.size()];
        for(int i=0;i<dictionaries.size();i++){
            names[i]=dictionaries.get(i).name;
        }
        String result=gson.toJson(names);
        SharedPreferences preferences = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("dictionaries",result);
        editor.apply();
    }
//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//        if (hidden) {
//            saveDicts();
//        } else {
//            loadDicts();
//        }
//    }
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
       // loadDicts();
    }
    @Override
    public void onPause(){
        super.onPause();
        saveDicts();


    }

    @Override
    public void onItemClick(int position, View v) {
        Log.d("fkin Click", "onItemClick position: " + position);
        Context context=v.getContext();
        if(position==0){

                AlertDialog.Builder pop = new AlertDialog.Builder(context);
                final AlertDialog kek=pop.create();
                final View current=LayoutInflater.from(context).inflate(R.layout.add_dict_name,null,false);

                current.setBackgroundColor(Color.WHITE);
                Button btn=(Button)current.findViewById(R.id.add_new_dict);
                final EditText et=(EditText)current.findViewById(R.id.type_new_dict);
                final EditText desc=(EditText)current.findViewById(R.id.new_dict_description);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name=et.getText().toString();
                        String description=desc.getText().toString();
                        if(!name.equals("")&&!description.equals("")){
                            dictionaries.add(new CategDictionary(getContext(),name,description));
                            new CategDictionary(getContext(),name + "-known-words");
                            new CategDictionary(getContext(),name + "-rest-unknown");
                            adapt.notifyDataSetChanged();
                            //dictionaries.clear();
                            //loadDicts();


                            kek.hide();

                        }else{
                            Toast.makeText(getContext(),"Заполните все поля!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                kek.setView(current);
                kek.show();
        }else{
            Intent intent=new Intent(getContext(),GetAllWordsActivity.class);
            intent.putExtra("name",dictionaries.get(position).name);
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(final int position, View v) {
        Log.d("Long fkin Click", "onItemClick position: " + position);
       // final EditText taskEditText = new EditText(getContext());
        if(position!=0) {
            final String name = dictionaries.get(position).name;
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Настройки словаря")
                    .setMessage("Что вы хотите сделать со словарём?") //.setView(taskEditText)
                    .setPositiveButton("Изучать", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CardFragment.CURRENT_DICT_NAME = name;
                            SharedPreferences preferences = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putString("CURRENT_DICT_NAME",name);
                            editor.apply();
                        }
                    })
                    .setNegativeButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getContext().deleteDatabase(name);
                            Gson gson=new Gson();
                            SharedPreferences preferences = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor=preferences.edit();
                            String json=preferences.getString("dictionaries",null);
                            Type type = new TypeToken<List<String>>() {
                            }.getType();
                            ArrayList<String> names=gson.fromJson(json,type);
                            names.remove(name);
                            dictionaries.remove(dictionaries.get(position));
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

    public static class MyContentAdapter extends RecyclerView.Adapter<MyContentAdapter.MyViewHolder> {
        ClickListener mclickListener;
        View.OnClickListener mylistener;
        Context context;
        String text;
        ArrayList<StringTranslation> stlist;
        AlertDialog lol;
        static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            ClickListener clickListener;
            public ImageView picture;
            public TextView name;
            int viewtype;

            public MyViewHolder(View v,ClickListener listener) {
                super(v);

                picture = (ImageView) itemView.findViewById(R.id.tile_picture);
                name = (TextView) itemView.findViewById(R.id.tile_title);
                clickListener=listener;
                v.setOnClickListener(this);
                v.setOnLongClickListener(this);
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Context context = v.getContext();
//                        Intent intent = new Intent(context, GetAllWordsActivity.class);
//                        intent.putExtra("name",name.getText());
//                        context.startActivity(intent);
//                    }
//                });
            }
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


       // private final String[] mProjects;

        //private final Drawable[] mProjectPictures;
        public void setClickListener(View.OnClickListener callback) {
            mylistener = callback;
        }
        public MyContentAdapter(Context context, ClickListener clistener) {
           // Resources resources = context.getResources();
           // mProjects = resources.getStringArray(R.array.projects);
            mclickListener=clistener;
            this.context=context;
//            TypedArray a = resources.obtainTypedArray(R.array.projects_picture);
//            mProjectPictures = new Drawable[a.length()];
//            for (int i = 0; i < mProjectPictures.length; i++) {
//                mProjectPictures[i] = a.getDrawable(i);
//            }
//            a.recycle();
        }
        public MyContentAdapter(Context context, ArrayList<StringTranslation> stlist, AlertDialog lol,String text) {
            this.context=context;
            this.stlist=stlist;
            this.lol=lol;
            this.text=text;
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item,parent, false);
            MyViewHolder holder=new MyViewHolder(v,mclickListener);
            holder.viewtype=viewType;
            if (viewType == 1) {
                holder.name.setText("Добавить новый словарь");
                holder.picture.setBackgroundColor(Color.rgb(150,150,150));

            }

//            v.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                   // mylistener.onClick(view);
//                    Log.d("Click", "onItemClick position: ");
//                }
//            });
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
           // holder.picture.setImageDrawable(mProjectPictures[position % mProjectPictures.length]);
            final Gson gson=new Gson();
            final SharedPreferences preferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor=preferences.edit();
            String json=preferences.getString("dictionaries",null);
            final String[] names=gson.fromJson(json,String[].class);
            if(names!=null) {
                final String name = names[position];
                if (holder.viewtype != 1){
                    holder.name.setText(names[position]);
                }
                if (mclickListener == null) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // dictionaries.get(position).addWord();
                            Toast.makeText(context, name, Toast.LENGTH_SHORT).show();

                           // if (names != null) {
                                CategDictionary dict = new CategDictionary(context, names[position]);
                                dict.addWord(text, stlist);
//                                dict=new CategDictionary(context,names[position]+"-rest-unknown");
//                                dict.addWord(text, stlist,true);
                                    String rest=preferences.getString(name +"-rest",null);
                                ArrayList<String> rest_ar;
                                    if(rest!=null){
                                        Type type = new TypeToken<List<String>>() {
                                        }.getType();
                                        rest_ar=gson.fromJson(rest,type);

                                    }else{
                                       rest_ar=new ArrayList<>();
                                    }
                                rest_ar.add(text);
                                editor.putString(name+"-rest",gson.toJson(rest_ar));
                                editor.apply();
                                lol.hide();
                            }
                       // }
                    });
                }
            }else{
                final String name = dictionaries.get(position).name;
                if (holder.viewtype != 1) holder.name.setText(name);
                else
                if (mclickListener == null) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // dictionaries.get(position).addWord();
                            Toast.makeText(context, name, Toast.LENGTH_SHORT).show();

                            if (names != null) {
                                CategDictionary dict = new CategDictionary(context, name);

                                dict.addWord(text, stlist);
                                FirebaseDbHelper.AddWord(name,text,stlist);
                               // dict=new CategDictionary(context,names[position]+"-rest-unknown");
                               // dict.addWord(text, stlist,true);
                                lol.hide();
                            }
                        }
                    });
                }
            }
//            int pos = layoutManager.findFirstVisibleItemPosition();
//            if(position==pos){
//                holder.name.setText("Добавить новый словарь");
//                holder.picture.setBackgroundColor(Color.rgb(150,150,150));
//
//            }

//                holder.name.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Log.d("Click", "onItemClick position: " + position);
//                    }
//                });
        }
        @Override
        public int getItemViewType(int position) {
            if (position == 0) return 1;
            else return 0;
        }
        @Override
        public int getItemCount() {
            Gson gson=new Gson();
            SharedPreferences preferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=preferences.edit();
            String json=preferences.getString("dictionaries",null);
            String names[]=gson.fromJson(json,String[].class);
            if(names!=null) return names.length;
            return dictionaries.size();
           // return dictionaries.size();
        }

//        public void setOnItemClickListener(ClickListener clickListener) {
//            MyContentAdapter.clickListener = clickListener;
//        }
    }


    }
interface ClickListener {
    void onItemClick(int position, View v);
    void onItemLongClick(int position, View v);
}