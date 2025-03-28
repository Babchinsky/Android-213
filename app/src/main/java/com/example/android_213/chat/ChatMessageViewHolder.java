package com.example.android_213.chat;

import com.example.android_213.ChatActivity;
import com.example.android_213.R;
import com.example.android_213.orm.ChatMessage;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    public static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT);

    private TextView tvAuthor;
    private TextView tvText;
    private TextView tvMoment;
    private final LinearLayout layout;
    private ChatMessage chatMessage;

    public ChatMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        tvAuthor = itemView.findViewById(R.id.chat_msg_author);
        tvText = itemView.findViewById(R.id.chat_msg_text);
        tvMoment = itemView.findViewById(R.id.chat_msg_moment);
        layout = itemView.findViewById(R.id.chat_msg_layout);

    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        showMessage();
    }

    public void showMessage() {
        tvAuthor.setText(chatMessage.getAuthor());
        tvText.setText(chatMessage.getText());

        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(chatMessage.getMoment());

        Calendar currentCalendar = Calendar.getInstance();

        long diffMillis = currentCalendar.getTimeInMillis() - messageCalendar.getTimeInMillis();
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

        String displayMoment;

        if (isSameDay(currentCalendar, messageCalendar)) {
            displayMoment = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(chatMessage.getMoment());
        } else if (diffDays == 1) {
            displayMoment = "вчера, " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(chatMessage.getMoment());
        } else if (diffDays <= 7) {
            displayMoment = diffDays + " дня назад, " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(chatMessage.getMoment());
        } else {
            displayMoment = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(chatMessage.getMoment());
        }

        tvMoment.setText(displayMoment);

        layout.setGravity(
                Objects.equals(this.chatMessage.getAuthor(), (ChatActivity.author))
                        ? Gravity.END
                        : Gravity.START
        );
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

}
/*
Класс-посредник между XML-разметкой представления View и объектом Java
*/
