package com.example.android_213;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_213.chat.ChatMessageAdapter;
import com.example.android_213.orm.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private final String chatUrl = "https://chat.momentfor.fun/";
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    ChatMessageAdapter chatMessageAdapter;
    private ExecutorService pool;
    private EditText etAuthor;
    private EditText etMessage;
    private View ivBell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pool = Executors.newFixedThreadPool(3);
        pool.submit(this::loadChat);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        RecyclerView rvContainer = findViewById(R.id.chat_rv_container);
        rvContainer.setLayoutManager(new LinearLayoutManager(this));
        chatMessageAdapter = new ChatMessageAdapter(chatMessages);
        rvContainer.setAdapter(chatMessageAdapter);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendClick);
        etAuthor = findViewById(R.id.chat_et_author);
        etMessage = findViewById(R.id.chat_et_message);
        ivBell = findViewById(R.id.chat_iv_bell);
    }

    private void onSendClick(View view) {
        String author = etAuthor.getText().toString();
        if(author.isBlank()){
            Toast.makeText(this, R.string.chat_msg_empty_author, Toast.LENGTH_SHORT).show();
            return;
        }

        String message = etMessage.getText().toString();
        if(message.isBlank()){
            Toast.makeText(this, R.string.chat_msg_empty_message, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void loadChat() {
        try {
            String text = Services.fetchUrlText(chatUrl);
            JSONObject jsonObject = new JSONObject(text);
            JSONArray arr = jsonObject.getJSONArray("data");
            int len = arr.length();
            for (int i = 0; i < len; i++) {
                chatMessages.add(
                        ChatMessage.fromJsonObject(
                                arr.getJSONObject(i)
                        )
                );
            }
            runOnUiThread(() -> chatMessageAdapter.notifyDataSetChanged());
//            runOnUiThread(() -> chatMessageAdapter.notifyItemRangeInserted(0, len));
        } catch (RuntimeException | JSONException ex) {
            Log.d("loadChat", ex.getCause() + ex.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        if (pool != null) {
            pool.shutdownNow();
        }
        super.onDestroy();
    }
}