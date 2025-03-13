package com.example.android_213;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RateActivity extends AppCompatActivity {
    private static final String nbuUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private TextView tvTmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rate);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvTmp = findViewById(R.id.rate_tv_tmp);
        new Thread(this::loadRates).start();
    }

    private void loadRates() {
        try {
            String text = fetchUrlText(nbuUrl);
            runOnUiThread(() -> tvTmp.setText( text ));
        } catch (RuntimeException ignored) {
            runOnUiThread(() -> tvTmp.setText( R.string.rate_load_fail ));
        }
    }

    private String fetchUrlText(String href) throws RuntimeException {
        try (InputStream urlStream = new URL(href).openStream()) {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            int receivedBytes;
            while((receivedBytes = urlStream.read(buffer))> 0){
                byteBuilder.write(buffer, 0, receivedBytes);
            }
            return byteBuilder.toString();

        }
        catch (android.os.NetworkOnMainThreadException | java.lang.SecurityException | IOException  ex) {
            Log.e("LoadRates", "IOException" + ex.getCause() + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}