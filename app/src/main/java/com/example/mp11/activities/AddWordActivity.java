package com.example.mp11.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.example.mp11.ForDatabases.MyDbHelper;
import com.example.mp11.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

//активность для добавления нового слова или определения в базу данных
public class AddWordActivity extends AppCompatActivity {

    private Button btnStore;
    private EditText etword, etdefinition, etsyns,etex;
    private MyDbHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        //достаём название открытого словаря
        final String dict_name=getIntent().getExtras().getString("name");
        //открываем помошник sqlite базы анных
        databaseHelper = new MyDbHelper(this,dict_name);

        btnStore = (Button) findViewById(R.id.btnstore);

        etword = (EditText) findViewById(R.id.etword);
        etdefinition = (EditText) findViewById(R.id.etdefinition);
        etsyns = (EditText) findViewById(R.id.etsyns);
        etex=(EditText) findViewById(R.id.etex);
        btnStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //добавление слова или определения в бд
                databaseHelper.addWord(etword.getText().toString(), etdefinition.getText().toString(), etsyns.getText().toString(),etex.getText().toString());

                //достаём shared preferences
                final SharedPreferences preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor=preferences.edit();
                Gson gson=new Gson();
                //достаём оттуда json с массивом слов, которые пользователь ещё не повторял после занесения в бд
                String rest=preferences.getString(dict_name +"-rest",null);
                ArrayList<String> rest_ar;
                if(rest!=null){
                    //тип arraylist'a для gson
                    Type type = new TypeToken<List<String>>() {
                    }.getType();
                    //десериализация
                    rest_ar=gson.fromJson(rest,type);

                }else{
                    //если массива ещё нет, создать новый
                    rest_ar=new ArrayList<>();
                }
                //добавление слова в этот массив
                rest_ar.add(etword.getText().toString());
                //сохраняем обратно в shared preferences
                editor.putString(dict_name+"-rest",gson.toJson(rest_ar));
                editor.apply();
                //очищаем поля
                etword.setText("");
                etdefinition.setText("");
                etsyns.setText("");
                etex.setText("");

                Toast.makeText(AddWordActivity.this, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
            }
        });


    }
}
