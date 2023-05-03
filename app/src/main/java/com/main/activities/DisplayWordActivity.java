package com.main.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.main.R;
import com.main.adapters.WordAdapter;
import com.main.database.DatabaseHelper;
import com.main.models.Word;

import java.util.Locale;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class DisplayWordActivity extends AppCompatActivity {
    private TextView mTxtWord, mTxtAns;
    private ImageView mBtnBookMark, mBtnSound;
    private DatabaseHelper mDatabaseHelper;
    private Word mWord;
//    private WordAdapter dataAdapter;

    TextToSpeech mTextToSpeech;

//    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_word);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        mDatabaseHelper = new DatabaseHelper(this);

        mTxtWord = findViewById(R.id.txt_word);
        mTxtAns = findViewById(R.id.txt_ans);

        mTxtAns.setMovementMethod(new ScrollingMovementMethod());

        //Lấy đối tượng Word bằng word được truyền vào
        mWord = mDatabaseHelper.displayWord(getIntent().getStringExtra("word"));

        if(Objects.equals(mWord.getHtml(), "word_error")) {
            Toast.makeText(this, "Không tìm thấy từ trong database", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(DisplayWordActivity.this, MainActivity.class);
            startActivity(i);
        }

        // set dữ liệu cho layout
        setData();

        // Xử lý sự kiện trên btn_book_mark
        mBtnBookMark = findViewById(R.id.btn_book_mark);
        setColorBtnBookMark();
        mBtnBookMark.setOnClickListener(v -> {
            mDatabaseHelper.updateFav(mWord.isFav(), mWord.getId());
            mWord.setFav(!mWord.isFav());
            setColorBtnBookMark();
        });

        // Xử lý sự kiện trên btn_sound
        mBtnSound = findViewById(R.id.btn_sound);
        mBtnSound.setOnClickListener(v ->
                mTextToSpeech = new TextToSpeech(getApplicationContext(), status -> {
                    if(status == TextToSpeech.SUCCESS){
                        //Setting language
                        mTextToSpeech.setLanguage(Locale.UK);
                        mTextToSpeech.setSpeechRate(1.0f);
                        mTextToSpeech.speak(mWord.getWord(), TextToSpeech.QUEUE_ADD, null);
                    }
                }));

    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setColorBtnBookMark() {
        //check fav để đổi màu book mark
        if(mWord.isFav()) {
            mBtnBookMark.setColorFilter(getColor(R.color.colorIsFav));
        }else {
            mBtnBookMark.setColorFilter(getColor(R.color.colorPrimary));
        }
    }

    public void setData(){
        if(!mWord.getHtml().isEmpty()){
            mTxtWord.setText(mWord.getWord());
            mTxtAns.setText(Html.fromHtml(mWord.getHtml()).toString());
        }
    }


    public void onClickBtnBack(View view) {
        finish();
    }
}
