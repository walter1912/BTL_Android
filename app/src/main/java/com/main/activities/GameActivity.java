package com.main.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.main.R;
import com.main.database.DatabaseHelper;
import com.main.models.Word;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private static final String KEY_SCORE = "key_score";
    private static final String KEY_EDIT_TEXT = "key_edit_text";
    private static final String KEY_KEYS = "key_keys";
    private static final String KEY_CURRENT_ANSWER = "current_answer";
    public static final String EXTRA_SCORE = "extra_score";
//    public static final String EXTRA_CONGRATULATION = "start_activity_congratulation";

    //dialog boss
    private static final String SHARED_PREFS = "shared_prefs";
    private static final String KEY_HIGH_SCORE = "key_high_score";

    private int presCounter = 0;
    private int maxPresCounter;
    private int currentAnswer = 0;
    private int numToGameOver = 0;
    private int score = 0, highScore;
    private boolean isReloadBrick;
    long backPressedTime;

    private ArrayList<String> keys = new ArrayList<>();
    private ArrayList<Word> textAnswers;
    private String textAnswer;
    private TextView mTextQuestion, mTextScreen, mScore;
    private ProgressBar mProgressLevel;
    private EditText editText;
    Animation mAnimation;

    private TextView mHighScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_main);

        // ẩn actionBar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        textAnswers = new ArrayList<>();
        textAnswers = databaseHelper.getAllFavWord();

        mProgressLevel = findViewById(R.id.progressBar_level);
        mProgressLevel.setProgress(0);
        mProgressLevel.setMax(textAnswers.size());

        editText = findViewById(R.id.edit_text);
        if (savedInstanceState != null) {
            editText.setText(savedInstanceState.getString(KEY_EDIT_TEXT));
            currentAnswer = savedInstanceState.getInt(KEY_CURRENT_ANSWER);
            score = savedInstanceState.getInt(KEY_SCORE);
        }

        mScore = findViewById(R.id.text_score);
        mScore.setText(getString(R.string.text_score, score));

        mAnimation = AnimationUtils.loadAnimation(this, R.anim.smallbigforth);
        isReloadBrick = false;

        showGame(savedInstanceState);

    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            saveHighScore();
        }else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    private void showGame(Bundle savedInstanceState){
        textAnswer = textAnswers.get(currentAnswer).getWord();

        if(savedInstanceState != null){
            //neu khong phai reload lai chu thi nhan lai gia tri
            if(!isReloadBrick){
                maxPresCounter = textAnswers.get(currentAnswer).getWord().length();
                keys = savedInstanceState.getStringArrayList(KEY_KEYS);
            }else{
                maxPresCounter = textAnswer.length();
                for(int i = 0; i < textAnswer.length(); i++){
                    keys.add(String.valueOf(textAnswer.charAt(i)));
                }
            }
        }else{
            maxPresCounter = textAnswer.length();
            for(int i = 0; i < textAnswer.length(); i++){
                keys.add(String.valueOf(textAnswer.charAt(i)));
            }
        }
        shuffleArray(keys);

        showWordBrick(savedInstanceState);

    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void addView(LinearLayout viewParent, final String text, Bundle savedInstanceState) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.rightMargin = 10;
        final TextView textView = new TextView( (this));

        textView.setLayoutParams(layoutParams);
        textView.setBackground(this.getResources().getDrawable(R.drawable.bg_game_letter));
        textView.setTextColor(this.getResources().getColor(R.color.colorPurple));
        textView.setGravity(Gravity.CENTER);
        textView.setText(text);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setTextSize(22);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/FredokaOneRegular.ttf");
        Typeface typeface_question = Typeface.createFromAsset(getAssets(), "fonts/PatrickHand-Regular.ttf");

        mTextQuestion = findViewById(R.id.text_question);
        mTextQuestion.setText(textAnswers.get(currentAnswer).getDescription());
        mTextScreen = findViewById(R.id.text_screen);

        mTextQuestion.setTypeface(typeface_question);
        mTextScreen.setTypeface(typeface);

        editText.setTypeface(typeface);
        textView.setTypeface(typeface);

        textView.setOnClickListener((v -> {
            if(presCounter < maxPresCounter){

                editText.setText(editText.getText().toString() + text);
                for(int i = 0; i < keys.size(); i++){
                    if (keys.get(i).equals(text)) {
                        keys.remove(i);
                    }
                }
                textView.startAnimation(mAnimation);
                textView.animate().alpha(0).setDuration(300);
                presCounter++;
                if(keys.isEmpty()){
                    doValidate(savedInstanceState);
                }
            }
        }));
        viewParent.addView(textView);
    }

    private void doValidate(Bundle savedInstanceState) {
        presCounter = 0;
        LinearLayout linearLayout1 = findViewById(R.id.layout_parent_1);
        LinearLayout linearLayout2 = findViewById(R.id.layout_parent_2);
        LinearLayout linearLayout3 = findViewById(R.id.layout_parent_3);

        if(editText.getText().toString().equals(textAnswer)){
            Toast.makeText(GameActivity.this, "Correct",Toast.LENGTH_SHORT).show();
            editText.setText("");
            currentAnswer++;
            numToGameOver = 0;
            isReloadBrick = true;
            score += 100;
            mScore.setText(getString(R.string.text_score, score));

            int currentLevel = mProgressLevel.getProgress();
            if(currentLevel >= mProgressLevel.getMax()){
                currentLevel = 0;
            }
            mProgressLevel.setProgress(currentLevel + 1);
            if(currentAnswer >= textAnswers.size()){
                DialogBoss();
            }else{
                showGame(savedInstanceState);
            }
        }else{
            Toast.makeText(GameActivity.this, "Incorrect",Toast.LENGTH_SHORT).show();
            editText.setText("");
            numToGameOver++;
            isReloadBrick = true;
            showGame(savedInstanceState);
            // thua
            if(numToGameOver == 2){
                Toast.makeText(GameActivity.this, "Game over",Toast.LENGTH_SHORT).show();
                DialogGameOver();
            }
        }
        shuffleArray(keys);
        linearLayout1.removeAllViews();
        linearLayout2.removeAllViews();
        linearLayout3.removeAllViews();

        showWordBrick(savedInstanceState);
    }

    private void shuffleArray(ArrayList<String> keys) {
        Random random = new Random();
        for(int i = keys.size() -1; i > 0; i--){
            int index = random.nextInt(i+1);
            String a = keys.get(index);
            keys.set(index, keys.get(i));
            keys.set(i,a);
        }
    }

    private void showWordBrick (Bundle savedInstanceState) {
        int i;
        if (keys.size() < 5) {
            for (i = 0; i < keys.size(); i++) {
                addView(findViewById(R.id.layout_parent_1), keys.get(i), savedInstanceState);
            }
        } else {
            for (i = 0; i < 5; i++) {
                addView(findViewById(R.id.layout_parent_1), keys.get(i), savedInstanceState);
            }
            if (keys.size() < 10) {
                for (i = 5; i < keys.size(); i++) {
                    addView(findViewById(R.id.layout_parent_2), keys.get(i), savedInstanceState);
                }
            } else {
                for (i = 5; i < 10; i++) {
                    addView(findViewById(R.id.layout_parent_2), keys.get(i), savedInstanceState);
                }
                for (i = 10; i < keys.size(); i++) {
                    addView(findViewById(R.id.layout_parent_3), keys.get(i), savedInstanceState);
                }
            }
        }
    }
    private void DialogBoss() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_game_finish);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg_game_boss);
        showDialogGame(dialog);
    }

    private void DialogGameOver() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_game_over);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg_game_over);
        showDialogGame(dialog);
    }

    private void showDialogGame(Dialog dialog) {
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Animation animationBigBoss = AnimationUtils.loadAnimation(this, R.anim.small_to_big);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/FredokaOneRegular.ttf");



        mTextQuestion = dialog.findViewById(R.id.text_question_game);
        mTextScreen = dialog.findViewById(R.id.text_screen);

        //dialog boss
        TextView textTitle = dialog.findViewById(R.id.text_back_to_home);
        textTitle.setOnClickListener(v -> {
            // quay lại home
            Intent intent = new Intent(GameActivity.this, StartGameScreenActivity.class);
            // xóa tất cả các activity trước
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            dialog.dismiss();
        });

        TextView button = dialog.findViewById(R.id.button_play_again);
        button.setOnClickListener(v -> {
            //play again: gọi lại chính activity này
            Intent intent = new Intent(GameActivity.this, GameActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            dialog.dismiss();
        });

        mScore = dialog.findViewById(R.id.text_score_end);
        mScore.setText(getString(R.string.text_score_end, score));

        mHighScore = dialog.findViewById(R.id.text_high_score);
        loadHighScore();
        if(score > highScore){
            updateHighScore(score);
        }

        ImageView imageView = dialog.findViewById(R.id.image_boss);
        imageView.startAnimation(animationBigBoss);

        mTextQuestion.setTypeface(typeface);
        mHighScore.setTypeface(typeface);
        mScore.setTypeface(typeface);
        textTitle.setTypeface(typeface);
        button.setTypeface(typeface);
    }

    private void loadHighScore() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        mHighScore.setText(getString(R.string.text_high_score, highScore));
    }
    private void updateHighScore(int highScoreNew) {
        highScore = highScoreNew;
        mHighScore.setText(getString(R.string.text_high_score, highScore));
        //         save data in prefreferences
        //        luôn nhận được dữ liệu mới nhất được lưu,
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_HIGH_SCORE, highScore);
        editor.apply();
    }

    private void saveHighScore() {
        Intent resultIntent= new Intent();
        resultIntent.putExtra(EXTRA_SCORE, score);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCORE, score);
        outState.putInt(KEY_CURRENT_ANSWER, currentAnswer);
        outState.putString(KEY_EDIT_TEXT, editText.getText().toString());
        outState.putStringArrayList(KEY_KEYS, keys);
    }
}