package com.example.mp11.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mp11.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DictAdapter extends BaseAdapter {
   // HashMap<String, List<String>> map;
    Context context;
    //ArrayList names=new ArrayList<>();
    //Iterator<Map.Entry<String, List<String>>> it;
    ArrayList<String> names,holders;
 //   public DictAdapter(Context context, HashMap<String, List<String>> map){
 public DictAdapter(Context context, ArrayList<String> names, ArrayList<String> holders){
        this.context=context;
       // this.map=map;
       //it=map.entrySet().iterator();

       // names.addAll(map.entrySet());

     this.names=names;
     this.holders=holders;
    }

    @Override
    public int getCount() {
       //return map.size();
        return names.size();
    }

    @Override
    public String getItem(int position) {
//        if(it.hasNext()){
//            Map.Entry<String, List<String>> pair = (Map.Entry)it.next();
//            return pair.getKey();
//        }


      //  return (Map.Entry) names.get(position);
        return names.get(position);

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String name=getItem(position);
        final String holder=holders.get(position);
       // Map.Entry<String, List<String>> item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.dict_search_item, parent, false);
        }
        TextView dict=(TextView)convertView.findViewById(R.id.search_dict_name);
        TextView hold=(TextView)convertView.findViewById(R.id.search_dict_holder);
        dict.setText(getItem(position));
       // hold.setText(holder);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategDictionary.downloadDict(holder,name,context);
            }
        });

        return convertView;
    }
}
