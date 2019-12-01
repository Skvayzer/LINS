package com.example.mp11.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mp11.R;
import com.example.mp11.ForDictionaries.StringTranslation;

import static android.content.Context.CLIPBOARD_SERVICE;

//адаптер для определений слов в pop окошках
public class TranslationAdapter extends ArrayAdapter<StringTranslation> {
    boolean isEng;
    boolean PAI=false;
    boolean allowIndex=true;
    Context context;
    public TranslationAdapter(Context context, StringTranslation[] arr,boolean isEng) {
        super(context, R.layout.pop_item, arr);
        this.isEng=isEng;
        this.context=context;
    }
    public TranslationAdapter(Context context, boolean isEng,StringTranslation[] arr,boolean allowIndex) {
        super(context, R.layout.pop_item, arr);
        this.isEng=isEng;
        this.allowIndex=allowIndex;
        this.context=context;
    }
    public TranslationAdapter(Context context, StringTranslation[] arr,boolean isEng, boolean isPAI) {
        super(context, R.layout.pop_item, arr);
        this.isEng=isEng;
        this.PAI=isPAI;
        this.context=context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //слово
        final StringTranslation item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pop_item, null);
        }
        final TextView meanings=(TextView) convertView.findViewById(R.id.meanings);
        TextView syns=(TextView) convertView.findViewById(R.id.syn_item);
        TextView ex=(TextView) convertView.findViewById(R.id.ex);
        //отображени
        if(!isEng) {
                meanings.setText(item.index + ")  " + item.meaning);
                syns.setText(item.syns);
                ex.setText("\n" + item.ex);

        }else{
            if(!PAI) {
                if(allowIndex) {
                    meanings.setText(Html.fromHtml(item.index + ")  " + item.meaning));
                    syns.setVisibility(View.GONE);
                    ex.setText(Html.fromHtml(item.ex + "<br/>"));
                }else {
                    meanings.setText(Html.fromHtml( item.meaning));
                    syns.setVisibility(View.GONE);
                    ex.setText(Html.fromHtml(item.ex + "<br/>"));
                }


            }else{

                syns.setText(item.word);
                meanings.setVisibility(View.GONE);
                ex.setVisibility(View.GONE);
            }
        }
        if(item.syns==null) syns.setVisibility(View.GONE);
        if(item.ex==null) ex.setVisibility(View.GONE);
        if(item.meaning==null)meanings.setVisibility(View.GONE);
        if(item.meaning.equals("")) meanings.setText(context.getString(R.string.word_isnot_found));


        return convertView;
    }
}
