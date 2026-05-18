package com.example.mad_pop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_pop.R;
import com.example.mad_pop.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<Message> messages = new ArrayList<>();
    private final long loggedInUserId;

    public MessageAdapter(long loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    public void submitList(List<Message> data) {
        messages.clear();
        messages.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.body.setText(message.getMessage());
        holder.meta.setText(message.getSenderName() + " • " + formatTime(message.getCreatedAt()));

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.bubble.getLayoutParams();
        if (message.getSenderId() == loggedInUserId) {
            holder.bubble.setBackgroundResource(R.drawable.bg_message_me);
            params.setMarginStart(90);
            params.setMarginEnd(0);
        } else {
            holder.bubble.setBackgroundResource(R.drawable.bg_message_other);
            params.setMarginStart(0);
            params.setMarginEnd(90);
        }
        holder.bubble.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final View bubble;
        final TextView body;
        final TextView meta;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            bubble = itemView.findViewById(R.id.messageBubble);
            body = itemView.findViewById(R.id.tvMessageBody);
            meta = itemView.findViewById(R.id.tvMessageMeta);
        }
    }
}

