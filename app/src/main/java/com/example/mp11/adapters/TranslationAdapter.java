package com.example.mp11.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mp11.R;
import com.example.mp11.ForDictionaries.StringTranslation;

//адаптер для определений слов в pop окошках
public class TranslationAdapter extends ArrayAdapter<StringTranslation> {
    public TranslationAdapter(Context context, StringTranslation[] arr) {
        super(context, R.layout.pop_item, arr);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //слово
        final StringTranslation item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pop_item, null);
        }
        //отображение
        ((TextView) convertView.findViewById(R.id.meanings)).setText(item.index + "  " +item.meaning);
        ((TextView) convertView.findViewById(R.id.syn_item)).setText("("+item.syns + ")");
        ((TextView) convertView.findViewById(R.id.ex)).setText("\n"+item.ex);


        return convertView;
    }
}
