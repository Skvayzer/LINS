package com.example.mp11.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mp11.R;
import com.example.mp11.yandex.dictslate.Model;

import java.util.Map;

public class TranslationAdapter extends ArrayAdapter<TranslationItem> {
    public TranslationAdapter(Context context, TranslationItem[] arr) {
        super(context, R.layout.pop_item, arr);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final TranslationItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pop_item, null);
        }
        String s="";
        if(item.meanings!=null) {
            for (String a : item.meanings) s += a + ", ";
            ((TextView) convertView.findViewById(R.id.meanings)).setText(item.index + "   " + s.substring(0,s.length()-2));
        }

        s="";
        if(item.syn!= null) {
            for (String a : item.syn) s += a + ", ";
            ((TextView) convertView.findViewById(R.id.syn_item)).setText(String.valueOf("(" + s.substring(0,s.length()-2) + ")"));
        }
        s="";
        if(item.ex!= null) {
            for (Map.Entry<String,String> entry: item.ex.entrySet())
            ((TextView) convertView.findViewById(R.id.ex)).setText(entry.getKey()+" - "+entry.getValue() + '\n');
        }



        return convertView;
    }
}
