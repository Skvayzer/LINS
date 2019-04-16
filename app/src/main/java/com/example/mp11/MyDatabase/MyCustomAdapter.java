package com.example.mp11.MyDatabase;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mp11.R;

import java.util.ArrayList;


public class MyCustomAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<WordModel> WordModelArrayList;
    private String curword;

    public MyCustomAdapter(Context context, ArrayList<WordModel> WordModelArrayList,String curword) {

        this.context = context;
        this.WordModelArrayList = WordModelArrayList;
        this.curword=curword;
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
            //holder.tvWord=(TextView)convertView.findViewById(R.id.word);
            holder.tvEx = (TextView) convertView.findViewById(R.id.ex);
            holder.tvSyns = (TextView) convertView.findViewById(R.id.syn_item);


            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

//        String definition="",syns="",ex="";
//        for(WordModel e: WordModelArrayList){
//            if(e.getWord().equals(curword)){
//                definition+=e.getdefinition()+", ";
//                syns+=e.getSyns()+", ";
//                ex+=e.getEx()+", ";
//            }
//        }
        //holder.tvWord.setText(WordModelArrayList.get(position).getWord());
   //     if (WordModelArrayList.get(position).getWord().equals(curword)) {
        String def=WordModelArrayList.get(position).getdefinition();
        String syns=WordModelArrayList.get(position).getSyns();
        if( syns!=null &&!syns.equals("")){
            syns="("+syns.substring(0,syns.length()-2)+")";
            holder.tvSyns.setText(syns);
        }
        String ex=WordModelArrayList.get(position).getEx();
        if( ex!=null && !ex.equals("")){
            holder.tvEx.setText(ex.substring(0,ex.length()-2));
        }
            holder.tvMeanings.setText(def.substring(0,def.length()-2));



//        }else {
////            holder.tvMeanings.setText("");
////            holder.tvSyns.setText("");
////            holder.tvEx.setText("");
//            convertView.setLayoutParams(new AbsListView.LayoutParams(-1,1));
//            convertView.setVisibility(View.INVISIBLE);
//        }


//        holder.tvMeanings.setText(definition.substring(0,definition.length()-3));
//        holder.tvSyns.setText(syns.substring(0,syns.length()-3));
//        holder.tvEx.setText(ex.substring(0,ex.length()-3));

        return convertView;
    }

    private class ViewHolder {

        protected TextView tvWord, tvEx, tvSyns, tvMeanings;
    }

}