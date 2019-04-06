package com.example.mp11;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageListAdapter extends BaseAdapter {
//    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
//    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
//    private Context mContext;
//    private List<BaseMessage> mMessageList;
//    public MessageListAdapter(Context context, List<BaseMessage> messageList) {
//        mContext = context;
//        mMessageList = messageList;
//    }
//    @Override
//    public int getItemViewType(int position) {
//        UserAndMessage message = (UserAndMessage) mMessageList.get(position);
//
//        if (message.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
//            // If the current user is the sender of the message
//            return VIEW_TYPE_MESSAGE_SENT;
//        } else {
//            // If some other user sent the message
//            return VIEW_TYPE_MESSAGE_RECEIVED;
//        }
//    }
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view;
//
//        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
//            view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.send_message_item, parent, false);
//            return new SentMessageHolder(view);
//        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
//            view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.recieved_message_item, parent, false);
//            return new ReceivedMessageHolder(view);
//        }
//        return null;
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        UserAndMessage message = (UserAndMessage) mMessageList.get(position);
//
//        switch (holder.getItemViewType()) {
//            case VIEW_TYPE_MESSAGE_SENT:
//                ((SentMessageHolder) holder).bind(message);
//                break;
//            case VIEW_TYPE_MESSAGE_RECEIVED:
//                ((ReceivedMessageHolder) holder).bind(message);
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return 0;
//    }
//    private class SentMessageHolder extends RecyclerView.ViewHolder {
//        TextView messageText, timeText;
//
//        SentMessageHolder(View itemView) {
//            super(itemView);
//
//            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
//            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
//        }
//
//        void bind(UserAndMessage message) {
//            messageText.setText(message.getMessage());
//
//            // Format the stored timestamp into a readable String using method.
//            timeText.setText(Utils.formatDateTime(message.getCreatedAt()));
//        }
//    }
//
//    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
//        TextView messageText, timeText, nameText;
//        ImageView profileImage;
//
//        ReceivedMessageHolder(View itemView) {
//            super(itemView);
//            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
//            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
//            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
//            profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
//        }
//
//        void bind(UserAndMessage message) {
//            messageText.setText(message.getMessage());
//
//            // Format the stored timestamp into a readable String using method.
//            timeText.setText(Utils.formatDateTime(message.getCreatedAt()));
//            nameText.setText(message.getSender().getNickname());
//
//            // Insert the profile image from the URL into the ImageView.
//            Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
//        }
//    }

    List<Message> messages = new ArrayList<Message>();
    Context context;
    String date;

    public MessageListAdapter(Context context) {
        this.context = context;
    }

    public void add(Message message) {
        this.messages.add(message);
        date=message.date;
        notifyDataSetChanged(); // to render the list we need to notify
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

    public static String getCurrentTimeUsingDate() {
        Date date = new Date();
        String strDateFormat = "hh:mm:ss a";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }

    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (message.isBelongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
            convertView = messageInflater.inflate(R.layout.send_message_item, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.my_text_message_body);
            holder.date=(TextView)convertView.findViewById(R.id.text_message_time);
            holder.date.setText(date);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = messageInflater.inflate(R.layout.recieved_message_item, null);
            holder.avatar = (View) convertView.findViewById(R.id.image_message_profile);
            holder.name = (TextView) convertView.findViewById(R.id.text_message_name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.text_message_body);
            holder.date=(TextView)convertView.findViewById(R.id.text_message_time);
            holder.date.setText(date);
            convertView.setTag(holder);

            holder.name.setText(message.getMemberData().getName());
            holder.messageBody.setText(message.getText());
            GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
            drawable.setColor(Color.parseColor(message.getMemberData().getColor()));
        }

        return convertView;
    }

}

class MessageViewHolder {
    public View avatar;
    public TextView name;
    public TextView messageBody;
    public TextView date;
}

