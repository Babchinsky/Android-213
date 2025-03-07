package com.example.android_213;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {
    private Animation alphaAnimation;
    private Animation scale1Animation;
    private Animation scale2Animation;
    private Animation rotate1Animation;
    private Animation rotate2Animation;
    private Animation translateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.demo_alpha);
        scale1Animation = AnimationUtils.loadAnimation(this, R.anim.demo_scale_1);
        scale2Animation = AnimationUtils.loadAnimation(this, R.anim.demo_scale_2);
        rotate1Animation = AnimationUtils.loadAnimation(this, R.anim.demo_rotate_1);
        rotate2Animation = AnimationUtils.loadAnimation(this, R.anim.demo_rotate_2);
        translateAnimation = AnimationUtils.loadAnimation(this, R.anim.demo_translate);
        findViewById(R.id.anim_v_alpha).setOnClickListener(this::onAlphaClick);
        findViewById(R.id.anim_v_scale_1).setOnClickListener(this::onScale1Click);
        findViewById(R.id.anim_v_scale_2).setOnClickListener(this::onScale2Click);
        findViewById(R.id.anim_v_rotate_1).setOnClickListener(this::onRotate1Click);
        findViewById(R.id.anim_v_rotate_2).setOnClickListener(this::onRotate2Click);
        findViewById(R.id.anim_v_translate).setOnClickListener(this::onTranslateClick);
    }

    private void onAlphaClick(View view) {
        view.startAnimation(alphaAnimation);
    }

    private void onScale1Click(View view) {
        view.startAnimation(scale1Animation);
    }

    private void onScale2Click(View view) {
        view.startAnimation(scale2Animation);
    }

    private void onRotate1Click(View view) {
        view.startAnimation(rotate1Animation);
    }

    private void onRotate2Click(View view) {
        view.startAnimation(rotate2Animation);
    }

    private void onTranslateClick(View view) {
        view.startAnimation(translateAnimation);
    }
}
    /*
    Анимации - плавный переход числовой характеристики между ключевыми значениями.
    Ресурсы анимаций создаются XML файлами в специальной директории anim
     */