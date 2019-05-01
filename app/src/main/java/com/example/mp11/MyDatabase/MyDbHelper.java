package com.example.mp11.MyDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mp11.views.StringTranslation;

import java.util.ArrayList;
import java.util.StringTokenizer;


public class MyDbHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "word_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_WORD = "words";
    private static final String TABLE_DEFINITION = "words_Definition";
    private static final String TABLE_SYN = "words_Syn";
    private static final String TABLE_EX = "ex_Syn";
    private static final String KEY_ID = "id";
    private static final String KEY_WORD = "word";
    private static final String KEY_DEFINITION = "Definition";
    private static final String KEY_SYN = "Syn";
    private static final String KEY_EX = "ex";

    /*CREATE TABLE students ( id INTEGER PRIMARY KEY AUTOINCREMENT, word TEXT, phone_number TEXT......);*/

    private static final String CREATE_TABLE_WORDS = "CREATE TABLE "
            + TABLE_WORD + "(" + KEY_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_WORD + " TEXT );";

    private static final String CREATE_TABLE_WORD_DEFINITION = "CREATE TABLE "
            + TABLE_DEFINITION + "(" + KEY_ID + " INTEGER,"+ KEY_DEFINITION + " TEXT );";

    private static final String CREATE_TABLE_WORD_SYN = "CREATE TABLE "
            + TABLE_SYN + "(" + KEY_ID + " INTEGER,"+ KEY_SYN + " TEXT );";

    private static final String CREATE_TABLE_WORD_EX = "CREATE TABLE "
            + TABLE_EX + "(" + KEY_ID + " INTEGER,"+ KEY_EX + " TEXT );";

    public MyDbHelper(Context context, String DB_NAME) {
        super(context, DB_NAME, null, DATABASE_VERSION);

        Log.d("table", CREATE_TABLE_WORDS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WORDS);
        db.execSQL(CREATE_TABLE_WORD_DEFINITION);
        db.execSQL(CREATE_TABLE_WORD_SYN);
        db.execSQL(CREATE_TABLE_WORD_EX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_WORD + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_DEFINITION + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_SYN + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_EX + "'");
        onCreate(db);
    }

    public void addWord(String word, String Definition, String Syn,String Ex) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(KEY_WORD, word);
        //db.insert(TABLE_WORD, null, values);
        long id = db.insertWithOnConflict(TABLE_WORD, null, values, SQLiteDatabase.CONFLICT_IGNORE);

       
        ContentValues valuesDefinition = new ContentValues();
        valuesDefinition.put(KEY_ID, id);
        valuesDefinition.put(KEY_DEFINITION, Definition);
        db.insert(TABLE_DEFINITION, null, valuesDefinition);

        
        ContentValues valuesSyn = new ContentValues();
        valuesSyn.put(KEY_ID, id);
        valuesSyn.put(KEY_SYN, Syn);
        db.insert(TABLE_SYN, null, valuesSyn);

        ContentValues valuesEx = new ContentValues();
        valuesSyn.put(KEY_ID, id);
        valuesSyn.put(KEY_EX, Ex);
        db.insert(TABLE_EX, null, valuesEx);
    }

    public ArrayList<WordModel> getAllWords() {
        ArrayList<WordModel> wordModelArrayList = new ArrayList<WordModel>();

        String selectQuery = "SELECT  * FROM " + TABLE_WORD;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);


        if (c.moveToFirst()) {
            do {
                WordModel wordModel = new WordModel();
                wordModel.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                wordModel.setWord(c.getString(c.getColumnIndex(KEY_WORD)));



                String selectDefinitionQuery = "SELECT  * FROM " + TABLE_DEFINITION +" WHERE "+KEY_ID+" = "+ wordModel.getId();
                Log.d("aaaaa",selectDefinitionQuery);

                //SQLiteDatabase dbDefinition = this.getReadableDatabase();
                Cursor cDefinition = db.rawQuery(selectDefinitionQuery, null);

                if (cDefinition.moveToFirst()) {
                    do {
                        wordModel.adddefinition(cDefinition.getString(cDefinition.getColumnIndex(KEY_DEFINITION)));
                    } while (cDefinition.moveToNext());
                }


                String selectSynQuery = "SELECT  * FROM " + TABLE_SYN+" WHERE "+KEY_ID+" = "+ wordModel.getId();;
                //SQLiteDatabase dbSyn = this.getReadableDatabase();
                Cursor cSyn = db.rawQuery(selectSynQuery, null);

                if (cSyn.moveToFirst()) {
                    do {
                        wordModel.addSyns(cSyn.getString(cSyn.getColumnIndex(KEY_SYN)));
                    } while (cSyn.moveToNext());
                }
                String selectExQuery = "SELECT  * FROM " + TABLE_EX+" WHERE "+KEY_ID+" = "+ wordModel.getId();;
                //SQLiteDatabase dbSyn = this.getReadableDatabase();
                Cursor cEx = db.rawQuery(selectExQuery, null);

                if (cEx.moveToFirst()) {
                    do {
                        wordModel.addEx(cEx.getString(cEx.getColumnIndex(KEY_EX)));
                    } while (cEx.moveToNext());
                }


                wordModelArrayList.add(wordModel);
            } while (c.moveToNext());

        }

        return wordModelArrayList;
    }
    public ArrayList<StringTranslation> getAllWords(int k) {
        ArrayList<StringTranslation> wordModelArrayList = new ArrayList<StringTranslation>();

        String selectQuery = "SELECT  * FROM " + TABLE_WORD;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);


        if (c.moveToFirst()) {
            do {
                StringTranslation wordModel = new StringTranslation();
                wordModel.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                wordModel.setWord(c.getString(c.getColumnIndex(KEY_WORD)));



                String selectDefinitionQuery = "SELECT  * FROM " + TABLE_DEFINITION +" WHERE "+KEY_ID+" = "+ wordModel.getId();
                Log.d("aaaaa",selectDefinitionQuery);

                //SQLiteDatabase dbDefinition = this.getReadableDatabase();
                Cursor cDefinition = db.rawQuery(selectDefinitionQuery, null);

                if (cDefinition.moveToFirst()) {
                    do {
                        wordModel.setdefinition(cDefinition.getString(cDefinition.getColumnIndex(KEY_DEFINITION)));
                    } while (cDefinition.moveToNext());
                }


                String selectSynQuery = "SELECT  * FROM " + TABLE_SYN+" WHERE "+KEY_ID+" = "+ wordModel.getId();;
                //SQLiteDatabase dbSyn = this.getReadableDatabase();
                Cursor cSyn = db.rawQuery(selectSynQuery, null);

                if (cSyn.moveToFirst()) {
                    do {
                        wordModel.setsyns(cSyn.getString(cSyn.getColumnIndex(KEY_SYN)));
                    } while (cSyn.moveToNext());
                }
                String selectExQuery = "SELECT  * FROM " + TABLE_EX+" WHERE "+KEY_ID+" = "+ wordModel.getId();;
                //SQLiteDatabase dbSyn = this.getReadableDatabase();
                Cursor cEx = db.rawQuery(selectExQuery, null);

                if (cEx.moveToFirst()) {
                    do {
                        wordModel.setex(cEx.getString(cEx.getColumnIndex(KEY_EX)));
                    } while (cEx.moveToNext());
                }


                wordModelArrayList.add(wordModel);
            } while (c.moveToNext());

        }

        return wordModelArrayList;
    }
    public ArrayList<WordModel> getWord(String word,int k) {
        ArrayList<WordModel> wordModelArrayList = new ArrayList<WordModel>();

        String selectQuery = "SELECT  * FROM " + TABLE_WORD + " WHERE " + KEY_WORD+" = " + word;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);


        if (c.moveToFirst()) {
            do {
                WordModel wordModel = new WordModel();
                wordModel.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                wordModel.setWord(c.getString(c.getColumnIndex(KEY_WORD)));



                String selectDefinitionQuery = "SELECT  * FROM " + TABLE_DEFINITION +" WHERE "+KEY_ID+" = "+ wordModel.getId();
                Log.d("SQLDictionary: ",selectDefinitionQuery);

                //SQLiteDatabase dbDefinition = this.getReadableDatabase();
                Cursor cDefinition = db.rawQuery(selectDefinitionQuery, null);

                if (cDefinition.moveToFirst()) {
                    do {
                        wordModel.adddefinition(cDefinition.getString(cDefinition.getColumnIndex(KEY_DEFINITION)));
                    } while (cDefinition.moveToNext());
                }


                String selectSynQuery = "SELECT  * FROM " + TABLE_SYN+" WHERE "+KEY_ID+" = "+ wordModel.getId();;
                //SQLiteDatabase dbSyn = this.getReadableDatabase();
                Cursor cSyn = db.rawQuery(selectSynQuery, null);

                if (cSyn.moveToFirst()) {
                    do {
                        wordModel.addSyns(cSyn.getString(cSyn.getColumnIndex(KEY_SYN)));
                    } while (cSyn.moveToNext());
                }
                String selectExQuery = "SELECT  * FROM " + TABLE_EX+" WHERE "+KEY_ID+" = "+ wordModel.getId();;
                //SQLiteDatabase dbSyn = this.getReadableDatabase();
                Cursor cEx = db.rawQuery(selectExQuery, null);

                if (cEx.moveToFirst()) {
                    do {
                        wordModel.addEx(cEx.getString(cEx.getColumnIndex(KEY_EX)));
                    } while (cEx.moveToNext());
                }


                wordModelArrayList.add(wordModel);
            } while (c.moveToNext());

        }

        return wordModelArrayList;
    }
    public ArrayList<StringTranslation> getWord(String word) {
        ArrayList<StringTranslation> wordModelArrayList = new ArrayList<StringTranslation>();

        String selectQuery = "SELECT  * FROM " + TABLE_WORD + " WHERE " + KEY_WORD+"=" + "'"+word+"'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);


        if (c.moveToFirst()) {
            do {
                StringTranslation wordModel = new StringTranslation();
                wordModel.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                wordModel.setWord(c.getString(c.getColumnIndex(KEY_WORD)));



                String selectDefinitionQuery = "SELECT  * FROM " + TABLE_DEFINITION +" WHERE "+KEY_ID+" = "+ wordModel.getId();
                Log.d("SQLDictionary: ",selectDefinitionQuery);

                //SQLiteDatabase dbDefinition = this.getReadableDatabase();
                Cursor cDefinition = db.rawQuery(selectDefinitionQuery, null);

                if (cDefinition.moveToFirst()) {
                    do {
                        wordModel.setdefinition(cDefinition.getString(cDefinition.getColumnIndex(KEY_DEFINITION)));
                    } while (cDefinition.moveToNext());
                }


                String selectSynQuery = "SELECT  * FROM " + TABLE_SYN+" WHERE "+KEY_ID+" = "+ wordModel.getId();;
                //SQLiteDatabase dbSyn = this.getReadableDatabase();
                Cursor cSyn = db.rawQuery(selectSynQuery, null);

                if (cSyn.moveToFirst()) {
                    do {
                        wordModel.setsyns(cSyn.getString(cSyn.getColumnIndex(KEY_SYN)));
                    } while (cSyn.moveToNext());
                }
                String selectExQuery = "SELECT  * FROM " + TABLE_EX+" WHERE "+KEY_ID+" = "+ wordModel.getId();;
                //SQLiteDatabase dbSyn = this.getReadableDatabase();
                Cursor cEx = db.rawQuery(selectExQuery, null);

                if (cEx.moveToFirst()) {
                    do {
                        wordModel.setex(cEx.getString(cEx.getColumnIndex(KEY_EX)));
                    } while (cEx.moveToNext());
                }


                wordModelArrayList.add(wordModel);
            } while (c.moveToNext());

        }

        return wordModelArrayList;
    }
    public void updateWord(int id, String word, String Definition, String Syn, String Ex) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(KEY_WORD, word);
        db.update(TABLE_WORD, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});


        ContentValues valuesDefinition = new ContentValues();
        valuesDefinition.put(KEY_DEFINITION, Definition);
        db.update(TABLE_DEFINITION, valuesDefinition, KEY_ID + " = ?", new String[]{String.valueOf(id)});


        ContentValues valuesSyn = new ContentValues();
        valuesSyn.put(KEY_SYN, Syn);
        db.update(TABLE_SYN, valuesSyn, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        ContentValues valuesEx = new ContentValues();
        valuesSyn.put(KEY_EX, Ex);
        db.update(TABLE_EX, valuesEx, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteWord(int id) {


        SQLiteDatabase db = this.getWritableDatabase();


        db.delete(TABLE_WORD, KEY_ID + " = ?",new String[]{String.valueOf(id)});


        db.delete(TABLE_DEFINITION, KEY_ID + " = ?", new String[]{String.valueOf(id)});


        db.delete(TABLE_SYN, KEY_ID + " = ?",new String[]{String.valueOf(id)});
        db.delete(TABLE_EX, KEY_ID + " = ?",new String[]{String.valueOf(id)});
    }

}