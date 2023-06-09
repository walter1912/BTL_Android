package com.main.activities;

//import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.main.database.DatabaseHelper;
import com.main.R;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper mDatabaseHelper;
    private Button mButtonFavorite, mButtonGame, mButtonLearn, mButtonFloatingWindow;
    private SimpleCursorAdapter dataAdapter;
    private Button mNewWord;
    ListView listViewNewWord;
//    private ImageView mPreviewIv;

//    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ẩn actionBar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
//        mPreviewIv = findViewById(R.id.imageIv);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Jua-Regular.ttf");
        TextView mAppName = findViewById(R.id.text_app_name);
        mAppName.setTypeface(typeface);

        mDatabaseHelper = new DatabaseHelper(this);
        mDatabaseHelper.createDataBase();
        mDatabaseHelper.updateFavToFalse();

        mButtonGame = findViewById(R.id.btn_game_main);
        mButtonGame.setOnClickListener(v -> {
            if (mDatabaseHelper.getAllFavWord().isEmpty()) {
                Toast.makeText(MainActivity.this, "Phải chọn từ yêu thích trước", Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(MainActivity.this, StartGameScreenActivity.class);
                startActivity(i);
            }
        });

        mButtonLearn = findViewById(R.id.btn_learn_word);
        mButtonLearn.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, StartingScreenActivity.class);
            startActivity(i);
        });

        mButtonFavorite = findViewById(R.id.btn_fav_word);
        mButtonFavorite.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, FavoriteActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        mButtonFloatingWindow = findViewById(R.id.btn_looking);
        getPermission();
        mButtonFloatingWindow.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                getPermission();
            } else {
                Intent intent = new Intent(MainActivity.this, FloatingWindow.class);
                startService(intent);
                finish();
            }
        });

        // từ mới mỗi ngày
        mNewWord = findViewById(R.id.new_word_refresh);
        mNewWord.setOnClickListener(v -> displayListView());
        displayListView();
    }

    // đẩy sang DisplayWordActivity
    public void getDescription(String word) {
        String ans = mDatabaseHelper.getDescription(word);
        Intent intent = new Intent(MainActivity.this, DisplayWordActivity.class);
        intent.putExtra("word", word);
        intent.putExtra("answer", ans);
        startActivity(intent);
    }

    private void displayListView() {
        Cursor cursor = mDatabaseHelper.getNewWord();

        // columns
        String[] columns = new String[] {
                DatabaseHelper.WORD,
                DatabaseHelper.DES
        };

        // layouts
        int[] to = new int[] {
                R.id.text_view_eng_word,
                R.id.text_view_description,
        };

        // tạo một adapter sử dụng các thông tin của layout và cursor từ hàm getNewWord() trỏ đến dữ liệu mong muốn
        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.activity_listview,
                cursor,
                columns,
                to,
                0);

        listViewNewWord = findViewById(R.id.list_new_word);
        // set adapter cho ListView
        listViewNewWord.setAdapter(dataAdapter);

        listViewNewWord.setOnItemClickListener((parent, view, position, id) -> {
            // lấy word của item và đẩy sang DisplayWordAcitivity
            Object clickItemObject = parent.getAdapter().getItem(position);
            SQLiteCursor c = (SQLiteCursor)clickItemObject;
            String word= c.getString(c.getColumnIndex(DatabaseHelper.WORD));
            getDescription(word);
        });
    }


    //Floating window
    public void getPermission(){
        //check for alert windows permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){
            Intent intent= new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,1);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Permission denied by user", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}