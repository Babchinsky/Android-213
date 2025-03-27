package com.example.android_213.chat;

import com.example.android_213.R;
import com.example.android_213.orm.ChatMessage;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    public static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT);

    private TextView tvAuthor;
    private TextView tvText;
    private TextView tvMoment;
    private ChatMessage chatMessage;

    public ChatMessageViewHolder(@NonNull View itemView){
        super(itemView);
        tvAuthor = itemView.findViewById(R.id.chat_msg_author);
        tvText = itemView.findViewById(R.id.chat_msg_text);
        tvMoment = itemView.findViewById(R.id.chat_msg_moment);
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        showMessage();
    }

    public void showMessage(){
        tvAuthor.setText(chatMessage.getAuthor());
        tvText.setText(chatMessage.getText());
        /*
        TODO: не выводить дату(только время), если дата - сегодня
         заменять дату на "вчера" или день назад, а такжюже "2 дня назад" и так далее
        */
        tvMoment.setText(dateFormat.format(this.chatMessage.getMoment()));
    }
}
/*
Класс-посредник между XML-разметкой представления View и объектом Java
*/
