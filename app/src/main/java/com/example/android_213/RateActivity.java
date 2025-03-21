package com.example.android_213;

import static com.example.android_213.Services.fetchUrlText;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RateActivity extends AppCompatActivity {
    private static final String nbuUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private List<NbuRate> nbuRates;
    private LinearLayout ratesContainer;
    private Drawable rateBg;
    private ExecutorService pool;
    private final Handler handler = new Handler();
    private static Map<String, List<NbuRate>> ratesCache = new HashMap<>();

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

        EditText dateInput = findViewById(R.id.date_input);
        String currentDate = NbuRate.dateFormat.format(new Date());
        dateInput.setText(currentDate);

        dateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, month1, dayOfMonth) -> {
                        String selectedDate = String.format("%02d.%02d.%d", dayOfMonth, month1 + 1, year1);
                        dateInput.setText(selectedDate);
                        loadRatesForDate(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        dateInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
                    loadRatesForDate(input);
                }
            }
        });

        EditText searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRates(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        pool = Executors.newFixedThreadPool(3);
        nbuRates = new ArrayList<>();
        loadRatesForDate(currentDate); // Загружаем курсы за текущую дату
        handler.postDelayed(this::periodicAction, 5000);
    }

    private void periodicAction() {
        if (isRatesExpired()) {
            loadRates();
            Log.d("periodicAction", "reload started");
        } else {
            Log.d("periodicAction", "rates are actual");
        }
        handler.postDelayed(this::periodicAction, 5000);
    }

    private boolean isRatesExpired() {
        if (nbuRates == null || nbuRates.isEmpty()) {
            return true;
        }
        try {
            Date currentDate = new Date();
            Date exchangeDate = nbuRates.get(0).getExchangeDate();
            return exchangeDate.before(currentDate);
        } catch (Exception ex) {
            Log.d("isRatesExpired", ex.getMessage());
            return true;
        }
    }

    private void loadRates() {
        String currentDate = NbuRate.dateFormat.format(new Date());
        loadRatesForDate(currentDate);
    }

    private void showRates() {
        EditText searchInput = findViewById(R.id.search_input);
        String query = searchInput != null ? searchInput.getText().toString() : "";
        filterRates(query);
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
        tv.setText(String.valueOf(rate.getRate()));
        tv.setLayoutParams(layoutParams);
        layout.addView(tv);

        layout.setOnClickListener(this::onRateClick);
        layout.setTag(rate);
        return layout;
    }

    private void onRateClick(View view) {
        NbuRate rate = (NbuRate) view.getTag();
        String exchangeDateStr = NbuRate.dateFormat.format(rate.getExchangeDate());
        String message = getString(R.string.rate_info,
                rate.getText(),  // Полное название
                rate.getCc(),   // Код валюты (например, AUD)
                rate.getR030(), // Код r030
                exchangeDateStr, // Дата обмена
                rate.getRate()  // Курс
        );
        new AlertDialog.Builder(this)
                .setTitle("Інформація про курс")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


    private String formatDateForApi(String date) {
        String[] parts = date.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format: " + date);
        }
        return parts[2] + parts[1] + parts[0]; // yyyy + MM + dd
    }

    private void loadRatesForDate(String date) {
        String formattedDate = formatDateForApi(date);
        List<NbuRate> cachedRates = getRatesFromCache(formattedDate);
        if (cachedRates != null) {
            nbuRates = cachedRates;
            runOnUiThread(this::showRates);
            Log.d("loadRatesForDate", "Курсы загружены из кеша для даты: " + date);
        } else {
            String url = nbuUrl + "&date=" + formattedDate;
            pool.submit(() -> {
                try {
                    String text = fetchUrlText(url);
                    if (text.startsWith("[")) {
                        saveCacheForDate(formattedDate, text);
                        runOnUiThread(this::showRates);
                    } else {
                        throw new JSONException("Неверный ответ от API: " + text);
                    }
                } catch (Exception e) {
                    Log.e("loadRatesForDate", "Ошибка загрузки курсов за дату: " + date, e);
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(this)
                                .setTitle("Ошибка")
                                .setMessage("Не удалось загрузить курсы за " + date + ": " +
                                        (e.getMessage().contains("Wrong date format") ?
                                                "Данные за будущие даты недоступны" : e.getMessage()))
                                .setPositiveButton("OK", null)
                                .show();
                    });
                }
            });
        }
    }

    private void saveCacheForDate(String date, String jsonText) {
        try {
            JSONArray arr = new JSONArray(jsonText);
            List<NbuRate> rates = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                rates.add(NbuRate.fromJsonObject(arr.getJSONObject(i)));
            }
            ratesCache.put(date, rates);
            nbuRates = rates;
        } catch (JSONException e) {
            Log.e("saveCacheForDate", "Ошибка парсинга JSON для даты: " + date, e);
        }
    }

    private List<NbuRate> getRatesFromCache(String date) {
        return ratesCache.get(date);
    }

    private void filterRates(String query) {
        if (nbuRates == null) return;
        List<NbuRate> filteredRates = new ArrayList<>();
        for (NbuRate rate : nbuRates) {
            if (rate.getText().toLowerCase().contains(query.toLowerCase()) ||
                    rate.getCc().toLowerCase().contains(query.toLowerCase())) {
                filteredRates.add(rate);
            }
        }
        ratesContainer.removeAllViews();
        for (NbuRate rate : filteredRates) {
            ratesContainer.addView(rateView(rate));
        }
    }

    @Override
    protected void onDestroy() {
        if (pool != null) {
            pool.shutdownNow();
        }
        handler.removeMessages(0);
        super.onDestroy();
    }
}