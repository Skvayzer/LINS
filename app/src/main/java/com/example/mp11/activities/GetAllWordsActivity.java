package com.example.mp11.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v7.widget.SearchView;

import com.example.mp11.adapters.DbCustomAdapter;
import com.example.mp11.ForDatabases.MyDbHelper;
import com.example.mp11.ForDatabases.WordModel;
import com.example.mp11.R;
import com.example.mp11.ForDictionaries.StringTranslation;

import java.util.ArrayList;

//активность со всеми словами из выбранного словаря
public class GetAllWordsActivity extends AppCompatActivity {
    private ListView listView;
    private SearchView find;
    private ImageButton add_btn;
    //для хранения слов и переводов
    private ArrayList<WordModel> wordModelArrayList;


    private DbCustomAdapter customAdapter;
    private MyDbHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_all_words);
        listView = (ListView) findViewById(R.id.lv);
        //для поиска слов
        find=(SearchView) findViewById(R.id.searchView);
        find.setQueryHint("Найти слово");
        //кнопка для добавления нового слова или определения
        add_btn=(ImageButton)findViewById(R.id.add_new_word);
        //достаём название выбранного словаря из интента
        final String name=getIntent().getExtras().getString("name");

        //открываем бд со словарём по названию
        databaseHelper = new MyDbHelper(this,name);

        //получаем все слова
        wordModelArrayList = databaseHelper.getAllWords();

        //адаптер для listview и переводов слов
        customAdapter = new DbCustomAdapter(this,wordModelArrayList);
        listView.setAdapter(customAdapter);

        //по клику на элемент
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //интент для активности с редактированием слова
                Intent intent = new Intent(GetAllWordsActivity.this, UpdateDeleteActivity.class);
                //кладём в интент само выбранное слово и название его словаря
                intent.putExtra("word", wordModelArrayList.get(position));
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //интент для активности добавления нового слова или определения
                Intent intent=new Intent(GetAllWordsActivity.this,AddWordActivity.class);
                //кладём в интент название текущего словаря
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });
        //на изменении введённого текста в searchview
        find.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return  false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                //на изменении введённого текста в searchview

                //берём текст, переводим в нижний регистр
                newText = newText.toLowerCase();

                //если searchview снова пустой
                if(newText.length()==0){
                    //берём все слова из словаря
                    wordModelArrayList = databaseHelper.getAllWords();




                }else{
                    //если searchview не пустой и там что-то ввели

                    //берем все слова из словаря, которые начинаются с того, что ввёл пользователь в searchview,
                    //или содержат как фрагмент
                    wordModelArrayList = databaseHelper.getAllWordsStartingWith(newText);



                }

                //прикручиваем их к адаптеру и листу
                customAdapter = new DbCustomAdapter(getApplicationContext(),wordModelArrayList);
                listView.setAdapter(customAdapter);
                //на нажатии элемента листа
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //интент для активности с редактированием слова
                        Intent intent = new Intent(GetAllWordsActivity.this, UpdateDeleteActivity.class);
                        //кладём в интент само выбранное слово и название его словаря
                        intent.putExtra("word", wordModelArrayList.get(position));
                        intent.putExtra("name",name);
                        startActivity(intent);
                    }
                });

                return true;
            }
        });
    }
}
