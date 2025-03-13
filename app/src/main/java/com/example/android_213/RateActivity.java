package com.example.android_213;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RateActivity extends AppCompatActivity {
    private static final String nbuUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private List<NbuRate> nbuRates;
    private LinearLayout ratesContainer;
    private Drawable rateBg;

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
        new Thread(this::loadRates).start();
    }

    private void loadRates(){
        try {
            String text = fetchUrlText(nbuUrl) ;
            JSONArray arr = new JSONArray(text);
            int len = arr.length();
            nbuRates = new ArrayList<>();

            for (int i = 0; i < len; i++) {
                nbuRates.add(NbuRate.fromJsonObject(
                        arr.getJSONObject(i) ) );
            }

            runOnUiThread( this::showRates  ) ;
        }
        catch (RuntimeException | JSONException ignored){
            // runOnUiThread( () -> tvTmp.setText( R.string.rate_load_failed )  ) ;
        }
    }

    private void showRates(){
        for (NbuRate rate : nbuRates){
            ratesContainer.addView( rateView( rate ) );
        }
    }

    private View rateView(NbuRate rate){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(10,5,10,5);

        LinearLayout layout  = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(layoutParams);
        layout.setBackground(rateBg);

        TextView tv = new TextView(this);
        tv.setText(rate.getCc());
        tv.setLayoutParams(layoutParams);
        layout.addView( tv );

        tv = new TextView(this);
        tv.setText(rate.getRate()+ "");
        tv.setLayoutParams(layoutParams);
        layout.addView( tv );

        layout.setOnClickListener(this::onRateClick);
        layout.setTag( rate );
        return  layout;
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


    private String fetchUrlText(String href) throws RuntimeException{
        try(InputStream urlStream = new URL(href).openStream()){
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            int receivedBytes;
            while ( (receivedBytes = urlStream.read(buffer)) > 0){
                byteBuilder.write(buffer, 0, receivedBytes);
            }
            return byteBuilder.toString();
        }
        catch ( IOException | android.os.NetworkOnMainThreadException | java.lang.SecurityException ex){
            Log.d("loadRates", "MalformedURLException: "+ ex.getCause() + ex.getMessage() );
            throw new RuntimeException( ex );
        }

    }
}