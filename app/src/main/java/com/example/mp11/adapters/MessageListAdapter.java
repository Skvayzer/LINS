package com.example.mp11.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mp11.Message;
import com.example.mp11.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//адаптер для сообщений чата, пока не используется
public class MessageListAdapter extends BaseAdapter {
    //массив с сообщениями
    List<Message> messages = new ArrayList<Message>();
    Context context;
    String date;

    public MessageListAdapter(Context context) {
        this.context = context;
    }

    public void add(Message message) {
        this.messages.add(message);
        date=message.date;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //достаём текущее время на момент отправки сообщения
    public static String getCurrentTimeUsingDate() {
        Date date = new Date();
        String strDateFormat = "hh:mm:ss a";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }


    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        //если сообщение отправлено нами, то
        if (message.isBelongsToCurrentUser()) {
            convertView = messageInflater.inflate(R.layout.send_message_item, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.my_text_message_body);
            holder.date=(TextView)convertView.findViewById(R.id.text_message_time);
            holder.date.setText(date);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());

        }
        //если человеком, с которым мы переговаривваемся
        else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = messageInflater.inflate(R.layout.recieved_message_item, null);
            holder.avatar = (View) convertView.findViewById(R.id.image_message_profile);
            holder.name = (TextView) convertView.findViewById(R.id.text_message_name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.text_message_body);
            holder.date=(TextView)convertView.findViewById(R.id.text_message_time);
            holder.date.setText(date);
            convertView.setTag(holder);

            holder.name.setText(message.getMemberData().getName());
            holder.messageBody.setText(message.getText());
            //пририсовываем аватарку
            GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
            drawable.setColor(Color.parseColor(message.getMemberData().getColor()));
        }

        return convertView;
    }

}
//холдер для элементов сообщений
class MessageViewHolder {
    public View avatar;
    public TextView name;
    public TextView messageBody;
    public TextView date;
}

