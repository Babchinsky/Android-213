package com.example.android_213;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    //    private TextView tv1;
    private TextView tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        tv2 = findViewById(R.id.textView2);
        findViewById(R.id.buttonPlus).setOnClickListener(this::onButtonPlusClick);
        findViewById(R.id.buttonMinus).setOnClickListener(this::onButtonMinusClick);
//        tv1 = findViewById(R.id.textView1);
//        findViewById(R.id.button1).setOnClickListener(this::onButton1Click);
    }

//    private void onButton1Click(View view) {
//
////        String txt = tv1.getText().toString();
////        txt += "!";
////        tv1.setText(txt);
//    }

    private void onButtonPlusClick(View view) {
        int number = Integer.parseInt(tv2.getText().toString());
        number += 1;
        tv2.setText(String.valueOf(number));
    }

    private void onButtonMinusClick(View view) {
        int number = Integer.parseInt(tv2.getText().toString());
        number -= 1;
        tv2.setText(String.valueOf(number));
    }
}