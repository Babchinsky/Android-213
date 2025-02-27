package com.example.android_213;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final Random random = new Random();
    private TextView tvScore;
    private TextView tvBestScore;
    private long score;
    private long bestScore;
    private final int N = 4;
    private final int[][] tiles = new int[N][N];
    private final TextView[][] tvTiles = new TextView[N][N];

    @SuppressLint({"ClickableViewAccessibility", "DiscouragedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_layout_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvTiles[i][j] = findViewById( // R.id.game_tv_tile_00
                        getResources().getIdentifier(
                                "game_tv_tile_" + i + j,
                                "id",
                                getPackageName()
                        )
                );
            }
        }
        tvScore = findViewById(R.id.game_tv_score);
        tvBestScore = findViewById(R.id.game_tv_best);
        LinearLayout gameField = findViewById(R.id.game_layout_field);
        /*
         Приведение к квадратной форме
         1. Доступ к элементам активности возможнет только после setContentView
         2. В рамках onCreate элементы готовы, как DOM, но они ещё не отображены и их реальные размеры неизвестны
         3. Реальный размер становится известным после построениея (inflate)
         4. Для того, чтобы выполнить какое-то действие по завершению построения ему следует в очередь уведомлений поставить задачу
        */
        gameField.post(() -> {
            int vw = this.getWindow().getDecorView().getWidth();
            // Создаём новые параметры контейнера и заменяем старые
            int fieldMargin = 20;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    vw - 2 * fieldMargin,
                    vw - 2 * fieldMargin
            );
            layoutParams.setMargins(fieldMargin, fieldMargin, fieldMargin, fieldMargin);
            layoutParams.gravity = Gravity.CENTER;
            gameField.setLayoutParams(layoutParams);
        });

        gameField.setOnTouchListener(new OnSwipeListener(GameActivity.this) {
            @Override
            public void onSwipeBottom() {
                Toast.makeText(GameActivity.this, "OnSwipeBottom", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeLeft() {
                Toast.makeText(GameActivity.this, "OnSwipeLeft", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeRight() {
                if (moveRight()) {
                    spawnTile();
                    updateField();
                }
                else {
                    Toast.makeText(GameActivity.this, "NO right move", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSwipeTop() {
                Toast.makeText(GameActivity.this, "OnSwipeTop", Toast.LENGTH_SHORT).show();
            }
        });

        bestScore = 0L; // TODO: восстановить с сохранённого
        startNewGame();
    }

    private void startNewGame() {
        score = 0L;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
//                tiles[i][j] = (int) Math.pow(2, i + j);
//                if (tiles[i][j] == 1) tiles[i][j] = 0;
                tiles[i][j] = 0;
            }
        }
        spawnTile();
        spawnTile();
        updateField();
    }

    private boolean moveRight() {
        boolean res = false;
        // [2000]         [0002]        [0002]          [0002]
        // [2020]         [0022]        [0004]          [0004]
        // [2220]         [0222]        [0204]          [0024]
        // [2222]         [2222]        [0404]          [0044]
        res = shiftRight();
        for (int i = 0; i < N; i++) {
            for (int j = N - 1; j > 0; j--) {
                if (tiles[i][j] == tiles[i][j - 1] && tiles[i][j] != 0) {
                    tiles[i][j] *= 2;
                    tiles[i][j - 1] = 0;
                    score += tiles[i][j];
                    res = true;
                }
            }
        }
        if (res) {
            shiftRight();
        }
        return res;
    }

    private boolean shiftRight() {
        boolean res = false;
        for (int i = 0; i < N; i++) {
            boolean wasReplace;
            do {
                wasReplace = false;
                for (int j = 0; j < N - 1; j++) {
                    if (tiles[i][j] != 0 && tiles[i][j + 1] == 0) {
                        tiles[i][j + 1] = tiles[i][j];
                        tiles[i][j] = 0;
                        wasReplace = true;
                        res = true;
                    }
                }
            } while (wasReplace);
        }
        return res;
    }

    private boolean spawnTile() {
        // Определяем перечисление всех свободных клеточек, выбираем случайно одну из них
        // значение - с вероятностью 0,1 - 4, 9 - 2
        List<Integer> freeTiles = new ArrayList<>(N * N);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (tiles[i][j] == 0) {
                    // два числа i, j можно соединить в одно по схеме k = N * i + j
                    // разделить их можно как i = k / N, j = k % N
                    freeTiles.add(N * i + j);
                }
            }
        }
        if (freeTiles.isEmpty()) {
            return false;
        }
        int k = freeTiles.get(random.nextInt(freeTiles.size()));
        int i = k / N;
        int j = k % N;
        tiles[i][j] = random.nextInt(10) == 0 ? 4 : 2;
        return true;
    }

    @SuppressLint("DiscouragedApi")
    private void updateField() {
        tvScore.setText(getString(R.string.game_tv_score_tpl, scoreToString(score)));
        tvBestScore.setText(getString(R.string.game_tv_best_tpl, scoreToString(bestScore)));
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvTiles[i][j].setText(String.valueOf(tiles[i][j]));
                // Цвет текста - ищем ресурс "color" относительно к значению клеточки
                tvTiles[i][j].setTextColor( // R.color.game_tile2_fg
                        getResources().getColor(
                                getResources().getIdentifier(
                                        String.format(Locale.ROOT, "game_tile%d_fg", tiles[i][j]),
                                        "color",
                                        getPackageName()
                                ),
                                getTheme() // позволит перключать темы
                        )
                );
                // Цвет фона. Сам фон задётся Drawable с закруглением.
                // Смена цвета достигается оттенком backgroundTint, что в программном выражении
                // есть цветовым фильтром. Вместо смены background - меняем его фильтр
                tvTiles[i][j].getBackground().setColorFilter(
                        getResources().getColor(
                                getResources().getIdentifier(
                                        String.format(Locale.ROOT, "game_tile%d_bg", tiles[i][j]),
                                        "color",
                                        getPackageName()
                                ),
                                getTheme() // позволит перключать темы
                        ),
                        PorterDuff.Mode.SRC_ATOP
                );
                tvTiles[i][j].setTextSize(32.0f); // TODO: рассчитать зависимость от клеточки
            }
        }
    }

    private String scoreToString(long value) {
        return String.valueOf(value);
    }
}