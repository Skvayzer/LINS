package com.example.mp11.ForDatabases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="dictionary";
    public static final String TABLE_DICT="Dictionary";

    public static final String KEY_ID="_id";
    public static final String KEY_word="word";
    public static final String KEY_MEANING="meaning";
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGES_TABLE="create table "+TABLE_DICT + " ("+KEY_ID + " integer primary key,"+KEY_word +" text,"
                + KEY_MEANING + " text)";
        db.execSQL(CREATE_MESSAGES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>oldVersion) {
            db.execSQL("drop table if exists " + TABLE_DICT);
            onCreate(db);
        }

    }
}
