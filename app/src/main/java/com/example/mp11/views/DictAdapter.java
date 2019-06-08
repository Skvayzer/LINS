package com.example.mp11.views;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mp11.CardFragment;
import com.example.mp11.DictDescriptionFragment;
import com.example.mp11.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class DictAdapter extends BaseAdapter {
   // HashMap<String, List<String>> map;
    Context context;
   // static public FragmentManager fragmentManager;
   // static public FragmentTransaction transaction;
    //ArrayList names=new ArrayList<>();
    //Iterator<Map.Entry<String, List<String>>> it;
   // ArrayList<String> names,holders;
    ArrayList<UsersDictionary> usersDictionaries;
 //   public DictAdapter(Context context, HashMap<String, List<String>> map){
 public DictAdapter(Context context, ArrayList<UsersDictionary> usersDicts){
        this.context=context;
       // this.map=map;
       //it=map.entrySet().iterator();

       // names.addAll(map.entrySet());

     usersDictionaries=usersDicts;
    }

    @Override
    public int getCount() {
       //return map.size();
        return usersDictionaries.size();
    }

    @Override
    public UsersDictionary getItem(int position) {
//        if(it.hasNext()){
//            Map.Entry<String, List<String>> pair = (Map.Entry)it.next();
//            return pair.getKey();
//        }


      //  return (Map.Entry) names.get(position);
        return usersDictionaries.get(position);

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void addToList(ArrayList<UsersDictionary> list){
     if(list!=null)usersDictionaries.addAll(list);
     //if(hold!=null)holders.addAll(hold);
     //   for(int i=0;i<list.size();i++)holders.add("kek");
     notifyDataSetChanged();
    }
    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
     final UsersDictionary ud=getItem(position);
        final String name=ud.name;
        final String holder=ud.holderName;
       // Map.Entry<String, List<String>> item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.dict_search_item, parent, false);
        }
        TextView dict=(TextView)convertView.findViewById(R.id.search_dict_name);
        TextView hold=(TextView)convertView.findViewById(R.id.search_dict_holder);
        CircleImageView image=(CircleImageView)convertView.findViewById(R.id.profile_image);
        CardView card=(CardView)convertView.findViewById(R.id.card_dict_description);
        dict.setText(name);
        hold.setText(holder);
        Glide.with(context).load(ud.imageUrl).into(image);


//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //CategDictionary.downloadDict(ud.holderId,name,context);
//                Toast.makeText(context,"Click",Toast.LENGTH_SHORT).show();
//                Fragment dict_description= DictDescriptionFragment.newInstance("kek","lol");
//                //FragmentTransaction transaction = getFragmentManager().beginTransaction();
//                ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.frame_layout, dict_description)
//                        .commit();
//                // transaction.replace(R.id.frame_layout,dict_description);
//                // transaction.commit();
//
//            }
//        });
//        card.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int action = event.getActionMasked();
//                if (action == MotionEvent.ACTION_DOWN)
//                {
//                    card.setCardBackgroundColor(Color.argb(50,0,0,0));
//                }
//                else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
//                {
//                    card.setCardBackgroundColor(Color.WHITE);
//                }
//
//                return false;
//            }
//        });
//        convertView.setFocusable(true);
//        convertView.setFocusableInTouchMode(true);
//        convertView.setClickable(true);


        return convertView;
    }
}
