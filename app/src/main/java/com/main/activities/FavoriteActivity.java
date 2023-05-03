package com.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.main.R;
import com.main.adapters.WordAdapter;
import com.main.database.DatabaseHelper;
import com.main.models.Word;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class FavoriteActivity extends AppCompatActivity {

//    private static final int REQUEST_CODE_CHANGED_STATUS = 10;

    AutoCompleteTextView mAutoTxtSearchFav;
    private ListView listViewFavWord;
    private ArrayList<Word> textAnswers;
    private DatabaseHelper mDatabaseHelper;
    private WordAdapter dataAdapter;
    private Button mBtnLearn;
    private Button mBtnGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        mAutoTxtSearchFav = findViewById(R.id.searchFav);
        listViewFavWord  = findViewById(R.id.list_fav_word);

        mDatabaseHelper = new DatabaseHelper(this);
        textAnswers = mDatabaseHelper.getAllFavWord();
        dataAdapter = new WordAdapter(FavoriteActivity.this, textAnswers);

        // set adapter cho ListView
        listViewFavWord.setAdapter(dataAdapter);
        // Hiển thị nghĩa của từ trong list view khi ấn vào nó
        listViewFavWord.setOnItemClickListener((parent, view, position, id) -> {
            getDescription(textAnswers.get(position).getWord());
            dataAdapter = new WordAdapter(FavoriteActivity.this, textAnswers);

            // set adapter cho ListView
            listViewFavWord.setAdapter(dataAdapter);
        });

        // tìm trong các từ yêu thích
        mAutoTxtSearchFav.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    mAutoTxtSearchFav.setAdapter(new ArrayAdapter<>(FavoriteActivity.this,
                            android.R.layout.simple_list_item_1, mDatabaseHelper.getFavEngWord(s.toString())));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mAutoTxtSearchFav.setOnItemClickListener((parent, view, position, id) -> {
            String mWord = (String) parent.getItemAtPosition(position);
            getDescription(mWord);
        });

        // Nút học từ vụng
        mBtnLearn = findViewById(R.id.btn_learn);
        mBtnLearn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FavoriteActivity.this, StartingScreenActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        // Nút game
        mBtnGame = findViewById(R.id.btn_game);
        mBtnGame.setOnClickListener(v -> {
            Intent i = new Intent(FavoriteActivity.this, StartGameScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }
    public void getDescription(String word) {
        Intent intent = new Intent(FavoriteActivity.this, DisplayWordActivity.class);
        intent.putExtra("word", word);
        startActivity(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        textAnswers = mDatabaseHelper.getAllFavWord();
        dataAdapter = new WordAdapter(FavoriteActivity.this, textAnswers);
        // set adapter cho ListView
        listViewFavWord.setAdapter(dataAdapter);
    }
}
