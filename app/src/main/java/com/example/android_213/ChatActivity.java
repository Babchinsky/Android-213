package com.example.android_213;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private final String chatUrl = "https://chat.momentfor.fun/";
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private RecyclerView rvContainer;
    ChatMessageAdapter chatMessageAdapter;
    private ExecutorService pool;
    private EditText etAuthor;
    private EditText etMessage;
    private View ivBell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pool = Executors.newFixedThreadPool(3);
        requestChat();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeBars = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(
                    Math.max(systemBars.left, imeBars.left),
                    Math.max(systemBars.top, imeBars.top),
                    Math.max(systemBars.right, imeBars.right),
                    Math.max(systemBars.bottom, imeBars.bottom)
            );
            return insets;
        });

        rvContainer = findViewById(R.id.chat_rv_container);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        rvContainer.setLayoutManager(linearLayoutManager);
        chatMessageAdapter = new ChatMessageAdapter(chatMessages);
        rvContainer.setAdapter(chatMessageAdapter);

        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendClick);
        etAuthor = findViewById(R.id.chat_et_author);
        etMessage = findViewById(R.id.chat_et_message);
        ivBell = findViewById(R.id.chat_iv_bell);
    }

    private void onSendClick(View view) {
        String author = etAuthor.getText().toString();
        if (author.isBlank()) {
            Toast.makeText(this, R.string.chat_msg_empty_author, Toast.LENGTH_SHORT).show();
            return;
        }

        String message = etMessage.getText().toString();
        if (message.isBlank()) {
            Toast.makeText(this, R.string.chat_msg_empty_message, Toast.LENGTH_SHORT).show();
            return;
        }
        CompletableFuture
                .runAsync(() -> sendMessage(new ChatMessage(author, message)), pool)
                .thenRun(this::requestChat);
    }

    private void requestChat(){
        CompletableFuture
                .supplyAsync(this::loadChat, pool)
                .thenAccept(this::updateChatView);
    }

    private void sendMessage(ChatMessage chatMessage) {
        /*
        Бэк чата принимает сообщение как от формы
        POST /
        Content-Type: application/x-www-form-urlencoded

        author=Author&msg=Message
        */
        Map<String, String> data = new HashMap<>();
        data.put("author", chatMessage.getAuthor());
        data.put("msg", chatMessage.getText());
        if ( Services.postForm(chatUrl, data)){
            runOnUiThread(() -> etMessage.setText(""));
        }
        else {
            Toast.makeText(ChatActivity.this, "Ошибка. Повторите позже", Toast.LENGTH_SHORT).show();
        }
    }

    private int loadChat() {
        try {
            int oldSize = chatMessages.size();
            String text = Services.fetchUrlText(chatUrl);
            JSONObject jsonObject = new JSONObject(text);
            JSONArray arr = jsonObject.getJSONArray("data");
            int len = arr.length();
            for (int i = 0; i < len; i++) {
                ChatMessage chatMessage = ChatMessage.fromJsonObject(arr.getJSONObject(i));
                if (chatMessages.stream().noneMatch(m -> m.getId().equals(chatMessage.getId()))) {
                    chatMessages.add(chatMessage);
                }
            }
            int newSize = chatMessages.size();
            if (newSize > oldSize) {
                chatMessages.sort(Comparator.comparing(ChatMessage::getMoment));
            }
            return newSize - oldSize;
        }
        catch (RuntimeException | JSONException ex) {
            Log.d("loadChat", ex.getCause() + ex.getMessage());
            return 0;
        }
    }

    private void updateChatView(int newMessagesCount) {
        if (newMessagesCount <= 0) {
            return;
        }
        int size = chatMessages.size();
        runOnUiThread(() -> {
            chatMessageAdapter.notifyItemRangeChanged(size - newMessagesCount, newMessagesCount);
            rvContainer.scrollToPosition(size - 1); // -1 -- index from 0
        });
    }

    @Override
    protected void onDestroy() {
        if (pool != null) {
            pool.shutdownNow();
        }
        super.onDestroy();
    }
}