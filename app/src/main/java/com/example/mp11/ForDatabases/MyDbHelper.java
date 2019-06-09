package com.example.mp11.ForDatabases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mp11.ForDictionaries.StringTranslation;

import java.util.ArrayList;


//помошник для sqlite базы данных
public class MyDbHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "word_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_WORD = "words";
    private static final String TABLE_DEFINITION = "words_Definition";
    private static final String TABLE_SYN = "words_Syn";
    private static final String TABLE_EX = "words_Ex";
    private static final String KEY_ID = "id";
    private static final String KEY_WORD = "word";
    private static final String KEY_DEFINITION = "Definition";
    private static final String KEY_SYN = "Syn";
    private static final String KEY_EX = "Ex";


    //для создания таблицы со словами
    private static final String CREATE_TABLE_WORDS = "CREATE TABLE "
            + TABLE_WORD + "(" + KEY_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_WORD + " TEXT );";
    //для создания таблицы с определениями
    private static final String CREATE_TABLE_WORD_DEFINITION = "CREATE TABLE "
            + TABLE_DEFINITION + "(" + KEY_ID + " INTEGER,"+ KEY_DEFINITION + " TEXT );";
    //для создания таблицы с синонимами
    private static final String CREATE_TABLE_WORD_SYN = "CREATE TABLE "
            + TABLE_SYN + "(" + KEY_ID + " INTEGER,"+ KEY_SYN + " TEXT );";
    //для создания таблицы с примерами
    private static final String CREATE_TABLE_WORD_EX = "CREATE TABLE "
            + TABLE_EX + "(" + KEY_ID + " INTEGER,"+ KEY_EX + " TEXT );";

    public MyDbHelper(Context context, String DB_NAME) {
        //создаём бд с именем из конструктора
        super(context, DB_NAME, null, DATABASE_VERSION);

        //Log.d("table", CREATE_TABLE_WORDS);
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

    //открыта ли бд
    public boolean isOpened(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.isOpen();
    }
    //добавление слова в бд
    public void addWord(String word, String Definition, String Syn,String Ex) {
        //получаем бд для записи
        SQLiteDatabase db = this.getWritableDatabase();

        //кладём в таблицу со словами слово
        ContentValues values = new ContentValues();
        values.put(KEY_WORD, word);

        //берём id, под которым будет слово
        long id = db.insertWithOnConflict(TABLE_WORD, null, values, SQLiteDatabase.CONFLICT_IGNORE);


        //в таблицу с определениями кладём то id и само определение
        ContentValues valuesDefinition = new ContentValues();
        valuesDefinition.put(KEY_ID, id);
        valuesDefinition.put(KEY_DEFINITION, Definition);
        db.insert(TABLE_DEFINITION, null, valuesDefinition);

        //в таблицу с синонимами кладём то id и сами синонимы
        ContentValues valuesSyn = new ContentValues();
        valuesSyn.put(KEY_ID, id);
        valuesSyn.put(KEY_SYN, Syn);
        db.insert(TABLE_SYN, null, valuesSyn);

        //в таблицу со значениями кладём то id и сами значения
        ContentValues valuesEx = new ContentValues();
        valuesEx.put(KEY_ID, id);
        valuesEx.put(KEY_EX, Ex);
        db.insert(TABLE_EX, null, valuesEx);
    }

    //для получения всех слов в бд
    public ArrayList<WordModel> getAllWords() {
        //для хранения слов
        ArrayList<WordModel> wordModelArrayList = new ArrayList<WordModel>();

        //запрос на выборку всех слов из бд
        String selectQuery = "SELECT  * FROM " + TABLE_WORD;
        SQLiteDatabase db = this.getReadableDatabase();

        //курсор, пробегающийся по бд согласно выборке
        Cursor c = db.rawQuery(selectQuery, null);

        try {
            //если к первому элементу перейти удалось
            if (c.moveToFirst()) {
                do {
                    //модель слова, хранящая его определения, синонимы, примеры
                    WordModel wordModel = new WordModel();
                    wordModel.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                    wordModel.setWord(c.getString(c.getColumnIndex(KEY_WORD)));


                    //теперь делаем выборку из таблицы с определениями по id
                    String selectDefinitionQuery = "SELECT  * FROM " + TABLE_DEFINITION + " WHERE " + KEY_ID + " = " + wordModel.getId();
                    Cursor cDefinition = db.rawQuery(selectDefinitionQuery, null);
                    try {
                        if (cDefinition.moveToFirst()) {
                            do {
                                //добавление соответствующего определения в модель слова
                                wordModel.adddefinition(cDefinition.getString(cDefinition.getColumnIndex(KEY_DEFINITION)));
                            } while (cDefinition.moveToNext());
                        }
                    } catch (SQLiteCantOpenDatabaseException e) {
                        e.printStackTrace();
                    }
                    //теперь делаем выборку из таблицы с синонимами по id
                    String selectSynQuery = "SELECT  * FROM " + TABLE_SYN + " WHERE " + KEY_ID + " = " + wordModel.getId();
                    Cursor cSyn = db.rawQuery(selectSynQuery, null);
                    try {
                        if (cSyn.moveToFirst()) {
                            do {
                                //добавление соответствующего синонима в модель слова
                                wordModel.addSyns(cSyn.getString(cSyn.getColumnIndex(KEY_SYN)));
                            } while (cSyn.moveToNext());
                        }
                    } catch (SQLiteCantOpenDatabaseException e) {
                        e.printStackTrace();
                    }
                    //теперь делаем выборку из таблицы с примерами по id
                    String selectExQuery = "SELECT  * FROM " + TABLE_EX + " WHERE " + KEY_ID + " = " + wordModel.getId();
                    Cursor cEx = db.rawQuery(selectExQuery, null);
                    try {
                        if (cEx.moveToFirst()) {
                            do {
                                //добавление соответствующих примеров в модель слова
                                wordModel.addEx(cEx.getString(cEx.getColumnIndex(KEY_EX)));
                            } while (cEx.moveToNext());
                        }
                    } catch (SQLiteCantOpenDatabaseException e) {
                        e.printStackTrace();
                    }

                    //добавляем модель слова в общий массив
                    wordModelArrayList.add(wordModel);
                } while (c.moveToNext());

            }
        }catch (SQLiteCantOpenDatabaseException e){
            e.printStackTrace();
        }
        return wordModelArrayList;
    }
    //для выборки слов, начинающихся или содержащий какой-то текст(для поиска по словам)
    public ArrayList<WordModel> getAllWordsStartingWith(String cur) {
        ArrayList<WordModel> wordModelArrayList = new ArrayList<WordModel>();

        //всё так же, как и в getAllWords(), но в самой первой выборке мы берём слова, подобные тому, что будет
        //введено в searchview
        String selectQuery = "SELECT  * FROM " + TABLE_WORD + " WHERE "+KEY_WORD+" LIKE '%"+cur+"%'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);


        if (c.moveToFirst()) {
            do {
                WordModel wordModel = new WordModel();
                wordModel.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                wordModel.setWord(c.getString(c.getColumnIndex(KEY_WORD)));



                String selectDefinitionQuery = "SELECT  * FROM " + TABLE_DEFINITION +" WHERE "+KEY_ID+" = "+ wordModel.getId();



                Cursor cDefinition = db.rawQuery(selectDefinitionQuery, null);

                if (cDefinition.moveToFirst()) {
                    do {
                        wordModel.adddefinition(cDefinition.getString(cDefinition.getColumnIndex(KEY_DEFINITION)));
                    } while (cDefinition.moveToNext());
                }


                String selectSynQuery = "SELECT  * FROM " + TABLE_SYN+" WHERE "+KEY_ID+" = "+ wordModel.getId();;

                Cursor cSyn = db.rawQuery(selectSynQuery, null);

                if (cSyn.moveToFirst()) {
                    do {
                        wordModel.addSyns(cSyn.getString(cSyn.getColumnIndex(KEY_SYN)));
                    } while (cSyn.moveToNext());
                }
                String selectExQuery = "SELECT  * FROM " + TABLE_EX+" WHERE "+KEY_ID+" = "+ wordModel.getId();;

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
    //то же самое, только возвращает функция ArrayList со строковыми переводами, а не в виде объекта класа
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
                Cursor cDefinition = db.rawQuery(selectDefinitionQuery, null);

                if (cDefinition.moveToFirst()) {
                    do {
                        wordModel.setdefinition(cDefinition.getString(cDefinition.getColumnIndex(KEY_DEFINITION)));
                    } while (cDefinition.moveToNext());
                }


                String selectSynQuery = "SELECT  * FROM " + TABLE_SYN+" WHERE "+KEY_ID+" = "+ wordModel.getId();;

                Cursor cSyn = db.rawQuery(selectSynQuery, null);

                if (cSyn.moveToFirst()) {
                    do {
                        wordModel.setsyns(cSyn.getString(cSyn.getColumnIndex(KEY_SYN)));
                    } while (cSyn.moveToNext());
                }
                String selectExQuery = "SELECT  * FROM " + TABLE_EX+" WHERE "+KEY_ID+" = "+ wordModel.getId();;

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
    //получить массив с определениями введённого слова
    //всё так же, только посмотрите нижний комментарий
    //возвращает ArrayList с моделями слова
    public ArrayList<WordModel> getWord(String word,int k) {
        ArrayList<WordModel> wordModelArrayList = new ArrayList<WordModel>();

        //тут добавляем в выборку условие, чтобы искать только определения слова, переданного в функцию
        String selectQuery = "SELECT  * FROM " + TABLE_WORD + " WHERE " + KEY_WORD+" = " + word;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);


        if (c.moveToFirst()) {
            do {
                WordModel wordModel = new WordModel();
                wordModel.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                wordModel.setWord(c.getString(c.getColumnIndex(KEY_WORD)));



                String selectDefinitionQuery = "SELECT  * FROM " + TABLE_DEFINITION +" WHERE "+KEY_ID+" = "+ wordModel.getId();



                Cursor cDefinition = db.rawQuery(selectDefinitionQuery, null);

                if (cDefinition.moveToFirst()) {
                    do {
                        wordModel.adddefinition(cDefinition.getString(cDefinition.getColumnIndex(KEY_DEFINITION)));
                    } while (cDefinition.moveToNext());
                }


                String selectSynQuery = "SELECT  * FROM " + TABLE_SYN+" WHERE "+KEY_ID+" = "+ wordModel.getId();;

                Cursor cSyn = db.rawQuery(selectSynQuery, null);

                if (cSyn.moveToFirst()) {
                    do {
                        wordModel.addSyns(cSyn.getString(cSyn.getColumnIndex(KEY_SYN)));
                    } while (cSyn.moveToNext());
                }
                String selectExQuery = "SELECT  * FROM " + TABLE_EX+" WHERE "+KEY_ID+" = "+ wordModel.getId();;

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
    //так же, как предыдущая, только возвращает массив со строковыми переводами
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



                Cursor cDefinition = db.rawQuery(selectDefinitionQuery, null);

                if (cDefinition.moveToFirst()) {
                    do {
                        wordModel.setdefinition(cDefinition.getString(cDefinition.getColumnIndex(KEY_DEFINITION)));
                    } while (cDefinition.moveToNext());
                }


                String selectSynQuery = "SELECT  * FROM " + TABLE_SYN+" WHERE "+KEY_ID+" = "+ wordModel.getId();;

                Cursor cSyn = db.rawQuery(selectSynQuery, null);

                if (cSyn.moveToFirst()) {
                    do {
                        wordModel.setsyns(cSyn.getString(cSyn.getColumnIndex(KEY_SYN)));
                    } while (cSyn.moveToNext());
                }
                String selectExQuery = "SELECT  * FROM " + TABLE_EX+" WHERE "+KEY_ID+" = "+ wordModel.getId();;

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
    //обновление определения слова по его id
    public void updateWord(int id, String word, String Definition, String Syn, String Ex) {
        SQLiteDatabase db = this.getWritableDatabase();

        //кладём слово
        ContentValues values = new ContentValues();
        values.put(KEY_WORD, word);
        db.update(TABLE_WORD, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        //кладём определение
        ContentValues valuesDefinition = new ContentValues();
        valuesDefinition.put(KEY_DEFINITION, Definition);
        db.update(TABLE_DEFINITION, valuesDefinition, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        //кладём синонимы
        ContentValues valuesSyn = new ContentValues();
        valuesSyn.put(KEY_SYN, Syn);
        db.update(TABLE_SYN, valuesSyn, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        //кладём примеры
        ContentValues valuesEx = new ContentValues();
        valuesEx.put(KEY_EX, Ex);
        db.update(TABLE_EX, valuesEx, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    //удаление слова по id из всех таблиц
    public void deleteWord(int id) {


        SQLiteDatabase db = this.getWritableDatabase();


        db.delete(TABLE_WORD, KEY_ID + " = ?",new String[]{String.valueOf(id)});


        db.delete(TABLE_DEFINITION, KEY_ID + " = ?", new String[]{String.valueOf(id)});


        db.delete(TABLE_SYN, KEY_ID + " = ?",new String[]{String.valueOf(id)});
        db.delete(TABLE_EX, KEY_ID + " = ?",new String[]{String.valueOf(id)});
    }
    //удаление слова по... слову, просто передайте его функции
    public void deleteWord(String word) {

        //ищем в таблице слов слово
        String selectQuery = "SELECT  * FROM " + TABLE_WORD + " WHERE " + KEY_WORD+"=" + "'"+word+"'";
        SQLiteDatabase db1 = this.getReadableDatabase();
        Cursor c = db1.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do{

                //берём его id и удаляем предыдущей функцией
               int id=c.getInt(c.getColumnIndex(KEY_ID));
               deleteWord(id);
        } while (c.moveToNext());





        }



    }
}