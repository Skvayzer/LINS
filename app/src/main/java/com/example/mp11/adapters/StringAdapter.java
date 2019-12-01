package com.example.mp11.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mp11.ForDictionaries.StringTranslation;
import com.example.mp11.R;

public class StringAdapter extends ArrayAdapter<String> {
    Context context;
    public StringAdapter(Context context, String[] arr) {
        super(context, R.layout.pop_item, arr);
        this.context=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //слово
        final String item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pop_item, null);
        }
        TextView meanings=(TextView) convertView.findViewById(R.id.meanings);
        TextView syns=(TextView) convertView.findViewById(R.id.syn_item);
        TextView ex=(TextView) convertView.findViewById(R.id.ex);
        syns.setVisibility(View.GONE);
        ex.setVisibility(View.GONE);
        if(getCount()!=0) meanings.setText(item);
        else meanings.setText(context.getString(R.string.word_isnot_found));


        return convertView;
    }
}
