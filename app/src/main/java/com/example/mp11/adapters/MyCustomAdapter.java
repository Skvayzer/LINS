package com.example.mp11.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mp11.R;
import com.example.mp11.ForDictionaries.StringTranslation;

import java.util.ArrayList;


//адаптер для списка определений на карточках
public class MyCustomAdapter extends BaseAdapter {

    private Context context;
    //массив переводов(определений, синонимов, примеров) в виде строк
    private ArrayList<StringTranslation> WordModelArrayList;

    public MyCustomAdapter(Context context, ArrayList<StringTranslation> WordModelArrayList,String curword) {

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
            convertView = inflater.inflate(R.layout.pop_item, null, true);

            
            holder.tvMeanings = (TextView) convertView.findViewById(R.id.meanings);
            holder.tvEx = (TextView) convertView.findViewById(R.id.ex);
            holder.tvSyns = (TextView) convertView.findViewById(R.id.syn_item);


            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        //отображение определений, синонимов и примеров
        String def=WordModelArrayList.get(position).getdefinition();
        String syns=WordModelArrayList.get(position).getSyns();
        if( syns!=null &&!syns.equals("")){
            syns=syns;
            holder.tvSyns.setText(syns);
        }
        String ex=WordModelArrayList.get(position).getEx();
        if( ex!=null && !ex.equals("")){
            holder.tvEx.setText(ex);
        }
        if( def!=null && !def.equals("")){
            holder.tvMeanings.setText(def);
        }

        return convertView;
    }

    private class ViewHolder {

        protected TextView tvWord, tvEx, tvSyns, tvMeanings;
    }

}