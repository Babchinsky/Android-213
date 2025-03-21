package com.example.android_213.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_213.R;
import com.example.android_213.orm.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {
    private final List<ChatMessage> chatMessages;

    public ChatMessageAdapter(List<ChatMessage> chatMessages){
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatMessageLayoutView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.chat_message_layout, parent, false);

        return new ChatMessageViewHolder(chatMessageLayoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.getTvAuthor().setText(chatMessage.getAuthor());
        holder.getTvText().setText(chatMessage.getText());
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
}
