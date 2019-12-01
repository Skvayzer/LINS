package com.example.mp11.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mp11.R;

import java.util.ArrayList;

//адаптер для отображения слов в словаре пользователя
public class WordAdapter extends BaseAdapter {
    //массив со словами
    ArrayList<String> words;
    Context context;
    public WordAdapter(Context context, ArrayList<String> list){
        words=list;
        this.context=context;
    }
    @Override
    public int getCount() {
        return words.size();
    }

    @Override
    public String getItem(int position) {
        return words.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String word=words.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.word_item, parent, false);
        }
        //отображение слова
        TextView word_item=(TextView)convertView.findViewById(R.id.word_item);
        word_item.setText(word);

        return convertView;
    }
    //добавление в массив слов для динамической подгрузки, если пользователь долистал до конца список
    public void addToList(ArrayList<String> list){
        if(list!=null)words.addAll(list);

        notifyDataSetChanged();
    }
}
