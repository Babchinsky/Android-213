package com.example.android_213;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final Random random = new Random();
    private final String bestScoreFilename = "score.best";
    private TextView tvScore;
    private TextView tvBestScore;
    private Animation spawnAnimation;
    private Animation collapseAnimation;
    //    private final int animTagKey = 1;
    private long score;
    private long bestScore;
    private final int N = 4;
    private final int[][] tiles = new int[N][N];
    private final TextView[][] tvTiles = new TextView[N][N];
    private SavedState savedState;

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

        spawnAnimation = AnimationUtils.loadAnimation(this, R.anim.game_tile_spawn);
        collapseAnimation = AnimationUtils.loadAnimation(this, R.anim.game_tile_colapse);
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
        findViewById(R.id.game_btn_undo).setOnClickListener(this::undoClick);
        findViewById(R.id.game_btn_new).setOnClickListener(this::newClick);
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
                tryMakeMove(MoveDirection.left);
            }

            @Override
            public void onSwipeRight() {
                tryMakeMove(MoveDirection.right);
            }

            @Override
            public void onSwipeTop() {
                Toast.makeText(GameActivity.this, "OnSwipeTop", Toast.LENGTH_SHORT).show();
            }
        });

        bestScore = 0L;
        loadBestScore();
        startNewGame();
    }

    private void tryMakeMove(MoveDirection moveDirection) {
        boolean canMove = false;
        switch (moveDirection) {
            case bottom: break;
            case left: canMove = canMoveLeft(); break;
            case right: canMove = canMoveRight(); break;
            case top: break;
        }
        if (canMove) {
            saveField();
            switch (moveDirection){
                case bottom: break;
                case left: moveLeft(); break;
                case right: moveRight(); break;
                case top: break;
            }
        }
    }

    private void saveField() {
        savedState = new SavedState(score, bestScore, new int[N][N]);
        for (int i = 0; i < N; i++) {
            System.arraycopy(tiles[i], 0, savedState.tiles[i], 0, N);
        }
    }

    private void newClick(View view) {
        new AlertDialog
                .Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.game_tv_title)
                .setMessage("Начать новую игру?")
                .setPositiveButton("Начать", (dlg, btn) -> startNewGame())
                .setNegativeButton("Продолжить", (dlg, btn) -> {
                })
                .setCancelable(false)
                .show();
    }

    private void undoClick(View view) {
        if (savedState != null) {
            score = savedState.score;
            bestScore = savedState.bestScore;
            for (int i = 0; i < N; i++) {
                System.arraycopy(savedState.tiles[i], 0, tiles[i], 0, N);
            }
            savedState = null;
            updateField();
        } else {
            new AlertDialog
                    .Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.game_tv_title)
                    .setMessage("Множественные сохранения доступны по подписке")
                    .setNeutralButton("Закрыть", (dlg, btn) -> {
                    })
                    .setPositiveButton("Подписка", (dlg, btn) -> Toast.makeText(this, "Скоро будет реализовано", Toast.LENGTH_SHORT).show())
                    .setNegativeButton("Выход", (dlg, btn) -> finish())
//                    .setNegativeButtonIcon(AppCompatResources.getDrawable(
//                            this, android.R.drawable.ic_input_delete) )
                    .setCancelable(false)
                    .show();
        }
    }

    private void saveBestScore() {
        /*
        Сохранение данных. Работа с файлами.
        В Android работа с хранилищами разделяется на две группы.
        - локальная группа
        - общаю группа
        Локальная - это директория, выделенная специально под приложение, в ней
        добавляется файловая система и БД. Данная директория удаляется при
        удалении приложения. Доступ к директории не ограниченный.
        Общая группа - внешние файлы, обычно - галерея. Доступ к ней
        ограниченный (запрещённый) и включается только с разрешением пользователя.
        */
        try (FileOutputStream fos = openFileOutput(bestScoreFilename, Context.MODE_PRIVATE)) {
            DataOutputStream writer = new DataOutputStream(fos);
            writer.writeLong(bestScore);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Log.w("GameActivity::saveBestScore", ex.getMessage() + " ");
        }

    }

    private void loadBestScore() {
        try (FileInputStream fis = openFileInput(bestScoreFilename);
             DataInputStream reader = new DataInputStream(fis)
        ) {
            bestScore = reader.readLong();
        } catch (IOException ex) {
            Log.w("GameActivity::loadBestScore", ex.getMessage() + " ");
        }
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

    private boolean canMoveRight() {
        for (int i = 0; i < N; i++) {
            for (int j = 1; j < N; j++) {
                if (tiles[i][j] != 0 && tiles[i][j - 1] == tiles[i][j] ||
                        tiles[i][j] == 0 && tiles[i][j - 1] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveRight() {
        boolean res;
        // [2000]         [0002]        [0002]          [0002]
        // [2020]         [0022]        [0004]          [0004]
        // [2220]         [0222]        [0204]          [0024]
        // [2222]         [2222]        [0404]          [0044]
        res = shiftRight(false);
        for (int i = 0; i < N; i++) {
            for (int j = N - 1; j > 0; j--) {
                if (tiles[i][j] == tiles[i][j - 1] && tiles[i][j] != 0) {
                    tiles[i][j] *= 2;
                    tiles[i][j - 1] = 0;
                    score += tiles[i][j];
                    res = true;
                    tvTiles[i][j].setTag(collapseAnimation);
                }
            }
        }
        if (res) {
            shiftRight(true);
        }
    }

    private boolean canMoveLeft() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (tiles[i][j] != 0 && tiles[i][j + 1] == tiles[i][j] ||
                        tiles[i][j] == 0 && tiles[i][j + 1] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveLeft() {
        boolean res;
        // [2000]         [0002]        [0002]          [0002]
        // [2020]         [0022]        [0004]          [0004]
        // [2220]         [0222]        [0204]          [0024]
        // [2222]         [2222]        [0404]          [0044]
        res = shiftLeft(false);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (tiles[i][j] == tiles[i][j + 1] && tiles[i][j] != 0) {
                    tiles[i][j] *= 2;
                    tiles[i][j + 1] = 0;
                    score += tiles[i][j];
                    res = true;
                    tvTiles[i][j].setTag(collapseAnimation);
                }
            }
        }
        if (res) {
            shiftLeft(true);
        }
    }

    private boolean shiftRight(boolean shiftTags) {
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
                        if (shiftTags) {
                            Object tag = tvTiles[i][j].getTag();
                            tvTiles[i][j].setTag(tvTiles[i][j + 1].getTag());
                            tvTiles[i][j + 1].setTag(tag);
                        }
                    }
                }
            } while (wasReplace);
        }
        return res;
    }

    private boolean shiftLeft(boolean shiftTags) {
        boolean res = false;
        for (int i = 0; i < N; i++) {
            boolean wasReplace;
            do {
                wasReplace = false;
                for (int j = 1; j < N; j++) {
                    if (tiles[i][j] != 0 && tiles[i][j - 1] == 0) {
                        tiles[i][j - 1] = tiles[i][j];
                        tiles[i][j] = 0;
                        wasReplace = true;
                        res = true;
                        if (shiftTags) {
                            Object tag = tvTiles[i][j].getTag();
                            tvTiles[i][j].setTag(tvTiles[i][j - 1].getTag());
                            tvTiles[i][j - 1].setTag(tag);
                        }
                    }
                }
            } while (wasReplace);
        }
        return res;
    }

    private void spawnTile() {
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
            return;
        }
        int k = freeTiles.get(random.nextInt(freeTiles.size()));
        int i = k / N;
        int j = k % N;
        tiles[i][j] = random.nextInt(10) == 0 ? 4 : 2;
        tvTiles[i][j].setTag(spawnAnimation);
    }

    @SuppressLint("DiscouragedApi")
    private void updateField() {
        if (score > bestScore) {
            bestScore = score;
            saveBestScore();
        }
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
                // проверяем, есть ли анимация для данной клеточки
                Object animTag = tvTiles[i][j].getTag();
                if (animTag instanceof Animation) {
                    tvTiles[i][j].startAnimation((Animation) animTag);
                    tvTiles[i][j].setTag(null);
                }
            }
        }
    }

    private String scoreToString(long value) {
        return String.valueOf(value);
    }

    private static class SavedState {
        private final int[][] tiles;
        private final long score;
        private final long bestScore;

        private SavedState(long score, long bestScore, int[][] tiles) {
            this.tiles = tiles;
            this.score = score;
            this.bestScore = bestScore;
        }
    }

    private enum MoveDirection {
        bottom,
        left,
        right,
        top
    }
}