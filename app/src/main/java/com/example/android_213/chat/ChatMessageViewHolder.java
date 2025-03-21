package com.example.android_213.chat;

import com.example.android_213.R;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView tvAuthor;
    private TextView tvText;

    public ChatMessageViewHolder(@NonNull View itemView){
        super(itemView);
        tvAuthor = itemView.findViewById(R.id.chat_msg_author);
        tvText = itemView.findViewById(R.id.chat_msg_text);
    }

    public TextView getTvAuthor() {
        return tvAuthor;
    }

    public TextView getTvText() {
        return tvText;
    }
}
/*
Класс-посредник между XML-разметкой представления View и объектом Java
*/
