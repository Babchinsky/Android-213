package com.example.android_213;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_213.orm.NbuRate;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RateActivity extends AppCompatActivity {
    private static final String nbuUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private static final String cacheFilename = "nbu_rates_cache.json";
    private static List<NbuRate> cacheNbuRates = null;
    private List<NbuRate> nbuRates;
    private LinearLayout ratesContainer;
    private Drawable rateBg;
    private ExecutorService pool;
    private final Handler handler = new Handler();

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
        rateBg = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.rate_shape);
        ratesContainer = findViewById(R.id.rate_container);
        findViewById(R.id.rate_btn_close).setOnClickListener(v -> finish());
        pool = Executors.newFixedThreadPool(3);
        boolean isNeedReload = true;
        if (cacheNbuRates == null) {
            try (FileInputStream fis = openFileInput(cacheFilename)) {
                Log.d("onCreate", "try restoring cache from file");
                String content = readAllText(fis);
                mapRates(content);

                Log.d("onCreate", "cache from file restored");
                isNeedReload = isRatesExpired();
            } catch (IOException | JSONException ignored) {
                Log.d("onCreate", "file cache failed");
            }
        } else {
            Log.d("onCreate", "cache from memory restored");
            nbuRates = cacheNbuRates;
            isNeedReload = isRatesExpired();
        }

        if (isNeedReload) {
            pool.submit(this::loadRates);
        } else {
            showRates();
        }
        handler.postDelayed(this::periodicAction, 5000);
    }

    private void periodicAction() {
        if (isRatesExpired()) {
            pool.submit(this::loadRates);
            Log.d("periodicAction", "reload started");
        } else {
            Log.d("periodicAction", "rates are actual");
        }
        handler.postDelayed(this::periodicAction, 5000);
    }

    private boolean isRatesExpired() {
        try {
            return nbuRates.get(0).getExchangeDate().before(
                    NbuRate.dateFormat.parse(
                            NbuRate.dateFormat.format(new Date())
                    )
            );
        } catch (ParseException ex) {
            Log.d("isRatesExpired", ex.getCause() + ex.getMessage());
        }
        return false;
    }

    private void mapRates(String jsonText) throws JSONException {
        JSONArray arr = new JSONArray(jsonText);
        int len = arr.length();
        nbuRates = new ArrayList<>();

        for (int i = 0; i < len; i++) {
            nbuRates.add(NbuRate.fromJsonObject(
                    arr.getJSONObject(i)));
        }
        cacheNbuRates = nbuRates;
    }

    private void loadRates() {
        try {
            Log.d("loadRates", "Loading started");
            Thread.sleep(1);
            String text = fetchUrlText(nbuUrl);
            pool.submit(() -> {
                Log.d("loadRates", "try saving file cache");
                try (FileOutputStream fos = openFileOutput(cacheFilename, Context.MODE_PRIVATE)) {
                    fos.write(text.getBytes(StandardCharsets.UTF_8));
                    Log.d("loadRates", "file cache saved");
                } catch (IOException ex) {
                    Log.d("loadRates", ex.getCause() + ex.getMessage());
                }
            });
            mapRates(text);
            runOnUiThread(this::showRates);
        } catch (RuntimeException | JSONException ignored) {
            // runOnUiThread( () -> tvTmp.setText( R.string.rate_load_failed )  ) ;
        } catch (InterruptedException e) {
            Log.d("loadRates", "InterruptedException");
        }
    }

    private void showRates() {
        for (NbuRate rate : nbuRates) {
            ratesContainer.addView(rateView(rate));
        }
    }

    private View rateView(NbuRate rate) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(10, 5, 10, 5);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(layoutParams);
        layout.setBackground(rateBg);

        TextView tv = new TextView(this);
        tv.setText(rate.getCc());
        tv.setLayoutParams(layoutParams);
        layout.addView(tv);

        tv = new TextView(this);
        tv.setText(rate.getRate() + "");
        tv.setLayoutParams(layoutParams);
        layout.addView(tv);

        layout.setOnClickListener(this::onRateClick);
        layout.setTag(rate);
        return layout;
    }

    private void onRateClick(View view) {
        NbuRate rate = (NbuRate) view.getTag();

        // Получаем строку из ресурсов с подставленными значениями
        String message = getString(R.string.rate_info,
                rate.getText(),  // Полное название
                rate.getCc(),   // Код валюты (например, AUD)
                rate.getR030(), // Код r030
                "13.03.2025",   // Фиксированная дата (можно сделать динамической)
                rate.getRate()  // Курс
        );

        new AlertDialog.Builder(this)
                .setTitle("Інформація про курс")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private String fetchUrlText(String href) throws RuntimeException {
        if (cache.containsKey(href)) {
            CacheItem cacheItem = cache.get(href);
            if (cacheItem != null) {   // + check expires
                return cacheItem.text;
            }
        }
        try (InputStream urlStream = new URL(href).openStream()) {
            String text = readAllText(urlStream);
            cache.put(href, new CacheItem(href, text, null));
            return text;
        } catch (android.os.NetworkOnMainThreadException |
                 java.lang.SecurityException | IOException ex) {
            Log.d("loadRates", "MalformedURLException: " + ex.getCause() + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private String readAllText(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int receivedBytes;
        while ((receivedBytes = inputStream.read(buffer)) > 0) {
            byteBuilder.write(buffer, 0, receivedBytes);
        }
        return byteBuilder.toString();
    }

    @Override
    protected void onDestroy() {
        if (pool != null) {
            pool.shutdownNow();
        }
        handler.removeMessages(0);
        super.onDestroy();
    }

    static class CacheItem {
        private String href;
        private String text;
        private Date expires;

        public CacheItem(String href, String text, Date expires) {
            this.href = href;
            this.text = text;
            this.expires = expires;
        }
    }

    private final Map<String, CacheItem> cache = new HashMap<>();
}