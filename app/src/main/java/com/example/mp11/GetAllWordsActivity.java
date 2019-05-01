package com.example.mp11;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.mp11.MyDatabase.DbCustomAdapter;
import com.example.mp11.MyDatabase.MyCustomAdapter;
import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.MyDatabase.WordModel;
import com.example.mp11.views.StringTranslation;

import java.util.ArrayList;

public class GetAllWordsActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<WordModel> wordModelArrayList;
    private ArrayList<StringTranslation> wordStringArrayList;
    private DbCustomAdapter customAdapter;
    private MyDbHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_all_words);
        listView = (ListView) findViewById(R.id.lv);

        databaseHelper = new MyDbHelper(this,"TED");

        wordModelArrayList = databaseHelper.getAllWords();
       // wordStringArrayList=databaseHelper.getAllWords(1);
        customAdapter = new DbCustomAdapter(this,wordModelArrayList);
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(GetAllWordsActivity.this, UpdateDeleteActivity.class);
                intent.putExtra("word", wordModelArrayList.get(position));
                startActivity(intent);
            }
        });
    }
}
