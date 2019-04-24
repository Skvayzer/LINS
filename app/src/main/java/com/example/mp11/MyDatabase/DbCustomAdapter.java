package com.example.mp11.MyDatabase;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mp11.R;

import java.util.ArrayList;


public class DbCustomAdapter extends BaseAdapter {

    private Context context;
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
            convertView = inflater.inflate(R.layout.lv_item, null, true);

            
            holder.tvWord = (TextView) convertView.findViewById(R.id.word);
            holder.tvDefinition = (TextView) convertView.findViewById(R.id.definition);
            holder.tvSyns = (TextView) convertView.findViewById(R.id.syn);


            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        holder.tvWord.setText("Word: "+WordModelArrayList.get(position).getWord());
        holder.tvDefinition.setText("Definition: "+WordModelArrayList.get(position).getdefinition());
        holder.tvSyns.setText("Syns: "+WordModelArrayList.get(position).getSyns());

        return convertView;
    }
    private class ViewHolder {

        protected TextView tvWord, tvDefinition, tvSyns;
    }

}