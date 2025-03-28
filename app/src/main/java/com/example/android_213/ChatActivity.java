package com.example.android_213;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    public static String author;
    private final String chatUrl = "https://chat.momentfor.fun/";
    private final String savedAuthorFilename = "Last.author";
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private RecyclerView rvContainer;
    ChatMessageAdapter chatMessageAdapter;
    private ExecutorService pool;
    private EditText etAuthor;
    private EditText etMessage;
    private View ivBell;
    private Handler handler;
    private boolean isFirstSend;
    private boolean isFirstLoad;
    private Animation bellAnimation;
    private MediaPlayer incomeSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pool = Executors.newFixedThreadPool(3);
        handler = new Handler();
        handler.post(this::repeater);

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


        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendClick);
        etAuthor = findViewById(R.id.chat_et_author);
        etMessage = findViewById(R.id.chat_et_message);
        ivBell = findViewById(R.id.chat_iv_bell);

        ChatActivity.author = null;
        try (FileInputStream fileInputStream = openFileInput(savedAuthorFilename);
             DataInputStream reader = new DataInputStream(fileInputStream)) {
            etAuthor.setText(ChatActivity.author = reader.readUTF());
        } catch (IOException ignore) {

        }

        rvContainer = findViewById(R.id.chat_rv_container);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        rvContainer.setLayoutManager(linearLayoutManager);
        chatMessageAdapter = new ChatMessageAdapter(chatMessages);
        rvContainer.setAdapter(chatMessageAdapter);

        isFirstSend = true;
        isFirstLoad = true;
        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.bell_swing);
        ivBell = findViewById(R.id.chat_iv_bell);
        incomeSound = MediaPlayer.create(this, R.raw.income);
        createNotificationChannel();
    }

    // region Notifications
    private String channelId = "chatChannelId";

    private void createNotificationChannel() {
        String channelName = "Chat Channel";
        NotificationChannel notificationChannel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        String channelDescription = "Chat notification about new messages";
        notificationChannel.setDescription(channelDescription);
        getSystemService(NotificationManager.class)
                .createNotificationChannel(notificationChannel);
    }

    private void notify(ChatMessage chatMessage) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setAutoCancel(true)
                .setContentTitle("Новое уведомление")
                .setContentText("В чат пришло сообщение от " + chatMessage.getAuthor())
                .setContentIntent(pendingIntent)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100500
                );
                return;
            }
        }

        NotificationManagerCompat.from(this).notify(
                Integer.parseInt(chatMessage.getId()),
                notification
        );
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100500){
            // результат соглашения запроса на уведомление
            // заметить его, чтобы повторно не запрашивать
        }
    }
    // endregion

    private void onSendClick(View view) {
        if (isFirstSend) {
            String author = etAuthor.getText().toString();
            if (author.isBlank()) {
                Toast.makeText(this, R.string.chat_msg_empty_author, Toast.LENGTH_SHORT).show();
                return;
            }
            etAuthor.setEnabled(false);
            ChatActivity.author = author;
            try (FileOutputStream fileOutputStream = openFileOutput(savedAuthorFilename, Context.MODE_PRIVATE);
                 DataOutputStream writer = new DataOutputStream(fileOutputStream)) {
                writer.writeUTF(author);
            } catch (IOException ex) {
                Log.d("onSendClick", ex.getMessage() + " ");
            }
        }

        String message = etMessage.getText().toString();
        if (message.isBlank()) {
            Toast.makeText(this, R.string.chat_msg_empty_message, Toast.LENGTH_SHORT).show();
            return;
        }
        isFirstSend = false;
        CompletableFuture
                .runAsync(() -> sendMessage(new ChatMessage(ChatActivity.author, message)), pool)
                .thenRun(this::requestChat);
    }

    private void requestChat() {
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
        if (Services.postForm(chatUrl, data)) {
            runOnUiThread(() -> etMessage.setText(""));
        } else {
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
        } catch (RuntimeException | JSONException ex) {
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
        if (isFirstLoad) {
            isFirstLoad = false;
        } else {
            boolean isNew = false;
            for (int i = 0; i < newMessagesCount; i++) {
                ChatMessage chatMessage = chatMessages.get(size - i -1);
                if (!Objects.equals(ChatActivity.author, chatMessage.getAuthor())) {
                    isNew = true;
                    notify(chatMessage);
                    break;
                }
            }
            if (isNew) {
                ivBell.startAnimation(bellAnimation);
                incomeSound.start();
            }
        }
    }

    private void repeater() {
        requestChat();
        handler.postDelayed(this::repeater, 2000);
    }

    @Override
    protected void onDestroy() {
        if (pool != null) {
            pool.shutdownNow();
        }
        if (handler != null) {
            handler.removeMessages(0);

        }
        super.onDestroy();
    }
}