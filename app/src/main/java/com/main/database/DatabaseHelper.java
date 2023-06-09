package com.main.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;
import android.util.Log;
import com.main.models.Question;
import com.main.models.Word;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION =1;
    private static final String DB_NAME ="dict.db";
    private static final String AV_TABLE = "av";
    public static final String WORD = "word";
    public static final String DES = "description";
    private static String DB_PATH = "";
    private final Context mContext;
    private static SQLiteDatabase mDataBase;

    public DatabaseHelper(Context context){
        super(context,DB_NAME, null, VERSION );
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void updateFavToFalse() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fav", "FALSE"); // giá trị mới của cột fav

        int rowsAffected = db.update(AV_TABLE, values, null, null);
        // rowsAffected chứa số lượng hàng bị ảnh hưởng bởi phép cập nhật

        db.close();
    }


    public void createDataBase(){
        // nếu database không tồn tại thì copy từ assets

        boolean mDataBaseExist = checkDataBase();
        if (!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                //Copy the database từ assests
                copyDataBase();
                Log.e(TAG, "createDatabase database created");
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    //Open the database
//    public boolean openDataBase() throws SQLException {
//        String mPath = DB_PATH + DB_NAME;
//        //Log.v("mPath", mPath);
//        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
//        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
//        return mDataBase != null;
//    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    // lấy từ tiếng anh giống từ trong input
    public ArrayList<String> getEngWord(String query){
        ArrayList<String> engList= new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        //truy vấn ra những từ gần giống theo từ mình search
        Cursor cursor = sqLiteDatabase.rawQuery(
//                AV_TABLE,
//                new String[]{WORD},
//                WORD + " LIKE ?",
//                new String[]{"%" + query + "%"},
//                null, null,
//                WORD
//                AV_TABLE,
//                new String[]{WORD},
//                WORD + " LIKE ?",
//                new String[]{query + "%"},
//                null, null,
//                null
                "SELECT word FROM av WHERE word LIKE '" + query + "%'", null
        );
        int index = cursor.getColumnIndex(WORD);
        while (cursor.moveToNext()){
            engList.add(cursor.getString(index));
        }
        sqLiteDatabase.close();
        cursor.close();
        return engList;
    }

    // hiển thị từ bằng cột html
    public Word displayWord(String word) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Word ans = new Word();
        String sql = "SELECT * FROM " + AV_TABLE + " WHERE word = '" + word + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            ans.setId(cursor.getInt(cursor.getColumnIndex("id")));
            ans.setHtml(cursor.getString(cursor.getColumnIndex("html")));
            ans.setWord(cursor.getString(cursor.getColumnIndex("word")));
            String fav = cursor.getString(cursor.getColumnIndex("fav"));
            ans.setFav(fav.equals("TRUE"));
        }
        Log.d("ans: ", String.valueOf(ans));
        if(ans.getWord() == null) {
            return new Word(word);
        }
        return ans;
    }

    // hiển thị từ bằng cột html _ p2
    public String getDescription(String word){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String ans = null;
        String sql = "SELECT html FROM "+ AV_TABLE + " WHERE word = '"+ word + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        while (cursor.moveToNext()){
            ans = cursor.getString(cursor.getColumnIndex("html"));
        }
        //nếu phải cần dùng flags thì dùng Html.FROM_HTML_MODE_COMPACT
        //return Html.fromHtml(ans, Html.FROM_HTML_MODE_COMPACT).toString();
        return Html.fromHtml(ans).toString();
    }

    // lấy tất cả các từ yêu thích
    public ArrayList<Word> getAllFavWord(){
        ArrayList<Word> favWordList = new ArrayList<>();
        ;
        mDataBase = this.getReadableDatabase();
        try {
            Cursor cursor = mDataBase.rawQuery("SELECT * FROM av WHERE fav = 'TRUE'", null);
            if (cursor.moveToFirst()){
                do {
                    Word word = new Word();
                    word.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    word.setWord(cursor.getString(cursor.getColumnIndex("word")));
                    word.setHtml(cursor.getString(cursor.getColumnIndex("html")));
                    word.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                    String fav = cursor.getString(cursor.getColumnIndex("fav"));
                    word.setFav(fav.equals("TRUE"));
                    favWordList.add(word);
                }while (cursor.moveToNext());
            }
            cursor.close();
        }catch (Exception e){        }

        return favWordList;
    }

    //biến id là id của word được chọn yêu thích
    public void updateFav(boolean checked, int id){
        mDataBase = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        if(checked){
            //từ đang ở trạng thái được yêu thích
            //khi click vào view phải chuyển thành ko yêu thích là false
            contentValues.put("fav", "FALSE");
        }else{
            contentValues.put("fav", "TRUE");
        }
        mDataBase.update("av", contentValues, "id = " + id,
                null);
    }

    // lấy từ yêu thích giống với input
    public ArrayList<String> getFavEngWord(String query){
        ArrayList<String> engList= new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        //truy vấn ra những từ gần giống theo từ mình search
        Cursor cursor = sqLiteDatabase.query(
                AV_TABLE,
                new String[]{WORD},
                WORD + " LIKE ? AND fav = 'TRUE'" ,
                new String[]{query + "%"},
                null, null,
                WORD
        );
        int index = cursor.getColumnIndex(WORD);
        while (cursor.moveToNext()){
            engList.add(cursor.getString(index));
        }
        sqLiteDatabase.close();
        cursor.close();
        return engList;
    }

    //Quiz
    public ArrayList<Question> getAllQuestion(int number){
        ArrayList<Question> questionList = new ArrayList<Question>(number);
        mDataBase= this.getReadableDatabase();
        Cursor c= mDataBase.rawQuery("SELECT * FROM practice",null);

        if (c.moveToFirst()){
            do {
                Question question= new Question();
                question.setCauhoi(c.getString(c.getColumnIndex("question")));
                question.setCaseA(c.getString(c.getColumnIndex("caseA")));
                question.setCaseB(c.getString(c.getColumnIndex("caseB")));
                question.setCaseC(c.getString(c.getColumnIndex("caseC")));
                question.setTrueCase(c.getInt(c.getColumnIndex("true_case")));
                questionList.add(question);
            }while (c.moveToNext());
        }
        c.close();
        return questionList;
    }

    // từ mới mỗi ngày
    public Cursor getNewWord() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor mCursor = sqLiteDatabase.query(AV_TABLE, new String[] {"rowid _id",WORD,DES},
                null, null, null, null,"RANDOM()", String.valueOf(3));

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}