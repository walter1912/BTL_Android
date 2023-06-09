package com.main.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.main.R;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class StartGameScreenActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_QUIZ = 1;
    private static final String SHARED_PREFS = "shared_prefs";
    private static final String KEY_HIGH_SCORE = "key_high_score";
    private Button mGo;
    private TextView mHighScore;

    private int highScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game_screen);

        // ẩn actionBar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        mGo = findViewById(R.id.button_go);
        mGo.setOnClickListener(v -> {
            // gọi GameActivity
            Intent i = new Intent(StartGameScreenActivity.this, GameActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(i, REQUEST_CODE_QUIZ);
        });

        mHighScore = findViewById(R.id.text_view_highscore);
        loadHighScore();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(StartGameScreenActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_QUIZ) {
            if (resultCode == RESULT_OK) {
                int score = data.getIntExtra(GameActivity.EXTRA_SCORE, 0);
                if (score > highScore) {
                    updateHighScore(score);
                }
            }
        }
    }
    private void loadHighScore() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        mHighScore.setText(getString(R.string.text_high_score, highScore));
    }
    private void updateHighScore(int highscoreNew) {
        highScore = highscoreNew;
        mHighScore.setText(getString(R.string.text_high_score, highScore));
        //         save data in prefreferences
        //        luôn nhận được dữ liệu mới nhất được lưu,
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_HIGH_SCORE, highScore);
        editor.apply();
    }
}