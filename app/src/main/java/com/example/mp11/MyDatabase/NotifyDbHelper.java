package com.example.mp11.MyDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

public class NotifyDbHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "notify_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_WORD = "words";

    private static final String KEY_NOTIFY = "notify";
    private static final String KEY_ID = "id";
    private static final String KEY_WORD = "word";


    /*CREATE TABLE students ( id INTEGER PRIMARY KEY AUTOINCREMENT, word TEXT, phone_number TEXT......);*/

    private static final String CREATE_TABLE_WORDS = "CREATE TABLE "
            + TABLE_WORD + "(" + KEY_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_WORD + " TEXT,"+KEY_NOTIFY +"INTEGER);";



    public NotifyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        Log.d("table", CREATE_TABLE_WORDS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WORDS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_WORD + "'");

        onCreate(db);
    }

    public void addWord(String word, int status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WORD, word);
        values.put(KEY_NOTIFY,status);
        //db.insert(TABLE_WORD, null, values);
        long id = db.insertWithOnConflict(TABLE_WORD, null, values, SQLiteDatabase.CONFLICT_IGNORE);


    }

    public LinkedList<String> getAllWords(int ceiling) {
        ArrayList<WordModel> wordModelArrayList = new ArrayList<WordModel>();

        String selectQuery = "SELECT  * FROM " + TABLE_WORD;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        LinkedList<String> list=new LinkedList<>();
        if (c.moveToFirst()) {
            do {
                int k = c.getInt(c.getColumnIndex(KEY_NOTIFY));
                if (k <= ceiling) {

                    list.add(c.getString(c.getColumnIndex(KEY_WORD)));

                }
            }
                while (c.moveToNext()) ;

            }

        return list;
    }

    public void updateWord(String word, int status) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();

        values.put(KEY_NOTIFY,status);
        db.update(TABLE_WORD, values, KEY_WORD + " = "+word,null);




    }

    public void deleteWord(String word) {


        SQLiteDatabase db = this.getWritableDatabase();


        db.delete(TABLE_WORD, KEY_WORD + " = "+word,null);



    }

}
