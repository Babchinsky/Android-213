package com.example.android_213;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CalcActivity extends AppCompatActivity {
    private final int maxDigits = 10;
    private TextView tvResult;
    private TextView tvExpression;
    private String zeroDigit;
    private String dotSymbol;
    private String minusSymbol;
    private boolean needClear = false;
    private boolean isErrorDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        findViewById(R.id.calc_btn_0).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_1).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_2).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_3).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_4).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_5).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_6).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_7).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_8).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_9).setOnClickListener(this::onDigitClick);
        findViewById(R.id.calc_btn_c).setOnClickListener(this::onClearClick);
        findViewById(R.id.calc_btn_dot).setOnClickListener(this::onDotClick);
        findViewById(R.id.calc_btn_pm).setOnClickListener(this::onPmClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::onBackspaceClick);
        findViewById(R.id.calc_btn_inv).setOnClickListener(this::onInverseClick);

        tvResult = findViewById(R.id.calc_tv_result);
        tvExpression = findViewById(R.id.calc_tv_expression);
        zeroDigit = getString(R.string.calc_btn_0);
        dotSymbol = getString(R.string.calc_btn_dot);
        minusSymbol = getString(R.string.calc_btn_sub);

        onClearClick(null);
    }

    // region Config change

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("tvResult", tvResult.getText());
        outState.putBoolean("needClear", needClear);
        outState.putBoolean("isErrorDisplayed", isErrorDisplayed);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tvResult"));
        needClear = savedInstanceState.getBoolean("needClear");
        isErrorDisplayed = savedInstanceState.getBoolean("isErrorDisplayed");
    }

    // endregion

    private void onClearClick(View view) {
        tvResult.setText(zeroDigit);
        tvExpression.setText("");
        isErrorDisplayed = false;
    }

    private void onInverseClick(View view) {
        if (isErrorDisplayed) return;
        String resText = tvResult.getText().toString();
        tvExpression.setText(getString(R.string.calc_inv_tpl, resText));
        double x = parseResult(resText);
        if (x == 0){
         resText = getString(R.string.calc_err_div_zero);
         isErrorDisplayed = true;
        }
        else {
            resText = toResult(1.0 / x);
        }
        tvResult.setText((resText));
        needClear = true;
    }

    private void onDotClick(View view) {
        String resText = tvResult.getText().toString();
        if (resText.contains(dotSymbol)) {
            return;
        }
        resText += dotSymbol;
        tvResult.setText((resText));
    }

    private void onBackspaceClick(View view) {
        String resText = tvResult.getText().toString();
        int len = resText.length();

        if (len <= 1) {
            resText = zeroDigit;
        } else {
            resText = resText.substring(0, len - 1);
            if (resText.equals(minusSymbol)) {
                resText = zeroDigit;
            }
        }
        tvResult.setText((resText));
    }

    private void onPmClick(View view) {
        String resText = tvResult.getText().toString();
        if (resText.startsWith(minusSymbol)) {
            resText = resText.substring(1);
        } else if (!resText.equals(zeroDigit)) {
            resText = minusSymbol + resText;
        }
        tvResult.setText((resText));
    }

    private void onDigitClick(View view) {
        String resText = tvResult.getText().toString();
        if(needClear || isErrorDisplayed){
            resText = "";
            tvExpression.setText("");
            needClear = false;
            isErrorDisplayed = false;
        }
        if (resText.equals(zeroDigit)) {
            resText = "";
        }
        if (digitLength(resText) < maxDigits) {
            resText += ((Button) view).getText();
        }
        tvResult.setText(resText);
    }

    private int digitLength(String resText) {
        int len = resText.length();
        if (resText.contains(dotSymbol)) {
            len -= 1;
        }
        if (resText.startsWith(minusSymbol)) {
            len -= 1;
        }
        return len;
    }

    private String toResult(double x) {
        String res = String.valueOf(x)
                .replace(".", dotSymbol)
                .replace("-", minusSymbol)
                .replaceAll("0", zeroDigit);
        if(digitLength(res) > maxDigits){
            res = res.substring(0, maxDigits);
        }
        return res;
    }

    private double parseResult(String resText) {
        return Double.parseDouble(
                resText
                        .replace(dotSymbol, ".")
                        .replace(minusSymbol, "-")
                        .replaceAll(zeroDigit, "0"));
    }
}