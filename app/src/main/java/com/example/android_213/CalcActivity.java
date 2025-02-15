package com.example.android_213;

import android.annotation.SuppressLint;
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
    private boolean isAction = false;
    private int operatorBtnId;
    private double firstNum;

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
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::onClearEntryClick);
        findViewById(R.id.calc_btn_dot).setOnClickListener(this::onDotClick);
        findViewById(R.id.calc_btn_pm).setOnClickListener(this::onPmClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::onBackspaceClick);
        findViewById(R.id.calc_btn_inv).setOnClickListener(this::onInverseClick);

        findViewById(R.id.calc_btn_equal).setOnClickListener(this::onEqualClick);
        findViewById(R.id.calc_btn_add).setOnClickListener(this::onOperationClick);
        findViewById(R.id.calc_btn_sub).setOnClickListener(this::onOperationClick);
        findViewById(R.id.calc_btn_mul).setOnClickListener(this::onOperationClick);
        findViewById(R.id.calc_btn_div).setOnClickListener(this::onOperationClick);


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
        outState.putCharSequence("tvExpression", tvExpression.getText());
        outState.putBoolean("needClear", needClear);
        outState.putBoolean("isErrorDisplayed", isErrorDisplayed);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tvResult"));
        tvExpression.setText(savedInstanceState.getCharSequence("tvExpression"));
        needClear = savedInstanceState.getBoolean("needClear");
        isErrorDisplayed = savedInstanceState.getBoolean("isErrorDisplayed");
    }

    // endregion

    private void onClearClick(View view) {
        tvResult.setText(zeroDigit);
        tvExpression.setText("");
        isErrorDisplayed = false;
    }

    private void onClearEntryClick(View view){
        tvResult.setText(zeroDigit);
        isErrorDisplayed = false;
    }

    private void onInverseClick(View view) {
        if (isErrorDisplayed) return;
        String resText = tvResult.getText().toString();
        tvExpression.setText(getString(R.string.calc_inv_tpl, resText));
        double x = parseResult(resText);
        if (x == 0) {
            resText = getString(R.string.calc_err_div_zero);
            isErrorDisplayed = true;
        } else {
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
        if (needClear || isErrorDisplayed) {
            resText = "";
            needClear = false;
            isErrorDisplayed = false;
            if (!isAction) {
                tvExpression.setText("");
            }
        }
        if (resText.equals(zeroDigit)) {
            resText = "";
        }
        if (digitLength(resText) < maxDigits) {
            resText += ((Button) view).getText();
        }
        tvResult.setText(resText);
    }

    private void onOperationClick(View view) {
        if (isErrorDisplayed) return;

        String operator = ((Button) view).getText().toString();

        String resText = tvResult.getText().toString();
        tvExpression.setText(String.format("%s %s ", resText, operator));

        needClear = true;
        isAction = true;
        tvResult.setText(resText);

        operatorBtnId = view.getId();
        firstNum = Double.parseDouble(resText);
    }

    @SuppressLint("SetTextI18n")
    private void onEqualClick(View view) {
        isAction = false;
        if (tvResult.getText().toString().isEmpty()) {
            return;
        }
        double secondNum = Double.parseDouble(tvResult.getText().toString());
        double result = 0;

        try {
            if (operatorBtnId == R.id.calc_btn_add) {
                result = firstNum + secondNum;
            } else if (operatorBtnId == R.id.calc_btn_sub) {
                result = firstNum - secondNum;
            } else if (operatorBtnId == R.id.calc_btn_mul) {
                result = firstNum * secondNum;
            } else if (operatorBtnId == R.id.calc_btn_div) {
                if (secondNum != 0) {
                    result = firstNum / secondNum;
                } else {
                    tvResult.setText(getString(R.string.calc_err_div_zero));
                    return;
                }
            }

            tvExpression.setText(tvExpression.getText().toString() + tvResult.getText().toString());

            tvResult.setText(toResult(result));
            needClear = true;
        } catch (Exception e) {
            tvResult.setText(getString(R.string.calc_err));
            isErrorDisplayed = true;
        }
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
        String res;

        // Если число целое, убираем .0
        if (x == (int) x) {
            res = String.valueOf((int) x);
        } else {
            res = String.valueOf(x);
        }

        // Заменяем символы на локализованные (точка, минус, нули)
        res = res.replace(".", dotSymbol)
                .replace("-", minusSymbol)
                .replaceAll("0", zeroDigit);

        // Ограничиваем количество символов
        if (digitLength(res) > maxDigits) {
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