package com.example.mp11.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mp11.ForDatabases.WordModel;
import com.example.mp11.R;

import java.util.ArrayList;


//адаптер для слов в getallwordsActivity
public class DbCustomAdapter extends BaseAdapter {

    private Context context;
    //массив со словами
    private ArrayList<WordModel> WordModelArrayList;

    public DbCustomAdapter(Context context, ArrayList<WordModel> WordModelArrayList) {

        this.context = context;
        this.WordModelArrayList = WordModelArrayList;
    }



    @Override
    public int getCount() {
        return WordModelArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return WordModelArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //вид элемента списка
            convertView = inflater.inflate(R.layout.lv_item, null, true);

            
            holder.tvWord = (TextView) convertView.findViewById(R.id.word);



            convertView.setTag(holder);
        }else {
            // getTag() возращает объект viewHolder'a, установленный как тег вьюшки
            holder = (ViewHolder)convertView.getTag();
        }


        //берём слово и формируем его отображение
        WordModel now=WordModelArrayList.get(position);
        String def=now.getWord()+'\n'+'\n',syns="",ex="";
        for(int i=0;i<now.definition.size();i++) {


            def += "Определение: " + now.getdefinition(i) + '\n'+'\n';


            def += "Синонимы: " + now.getSyns(i) + '\n'+'\n';

            def += "Примеры: " + now.getEx(i);

        }

       holder.tvWord.setText(def);



        return convertView;
    }
    //хранит вьюшки на элементе, хорошая практика
    private class ViewHolder {

        protected TextView tvWord;
    }

}