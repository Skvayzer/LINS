package com.example.mp11.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.example.mp11.R;
import com.example.mp11.ForDictionaries.UsersDictionary;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

//адаптер для словарей во вкладке social
public class DictAdapter extends BaseAdapter {
    Context context;

    //массив с отображаемыми словарями и данными о них и их владельцах
    ArrayList<UsersDictionary> usersDictionaries;

 public DictAdapter(Context context, ArrayList<UsersDictionary> usersDicts){
        this.context=context;
        usersDictionaries=usersDicts;
    }

    @Override
    public int getCount() {
        return usersDictionaries.size();
    }

    @Override
    public UsersDictionary getItem(int position) {
        return usersDictionaries.get(position);

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    //функция добавления словарей для динамической подгрузки, когда пользователь пролистал до конца
    public void addToList(ArrayList<UsersDictionary> list){
     if(list!=null)usersDictionaries.addAll(list);
     notifyDataSetChanged();
    }
    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
     final UsersDictionary ud=getItem(position);
        final String name=ud.name;
        final String holder=ud.holderName;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.dict_search_item, parent, false);
        }
        TextView dict=(TextView)convertView.findViewById(R.id.search_dict_name);
        TextView hold=(TextView)convertView.findViewById(R.id.search_dict_holder);
        CircleImageView image=(CircleImageView)convertView.findViewById(R.id.profile_image);
        CardView card=(CardView)convertView.findViewById(R.id.card_dict_description);
        //отображаем название словаря и имя пользователя его хозяина
        dict.setText(name);
        hold.setText(holder);
        //загружаем по ссылке из Firebase фото профиля владельца словаря
        Glide.with(context).load(ud.imageUrl).placeholder(context.getResources().getDrawable(R.drawable.diam)).error(context.getResources().getDrawable(R.drawable.diam)).into(image);

        //Picasso.get().load(ud.imageUrl).placeholder(context.getResources().getDrawable(R.drawable.diam)).error(context.getResources().getDrawable(R.drawable.diam)).into(image);
        return convertView;
    }
}
