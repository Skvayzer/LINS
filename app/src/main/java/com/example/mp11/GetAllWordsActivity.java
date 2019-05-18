package com.example.mp11;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v7.widget.SearchView;

import com.example.mp11.MyDatabase.DbCustomAdapter;
import com.example.mp11.MyDatabase.MyCustomAdapter;
import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.MyDatabase.WordModel;
import com.example.mp11.views.StringTranslation;

import java.util.ArrayList;

public class GetAllWordsActivity extends AppCompatActivity {
    private ListView listView;
    private SearchView find;
    private ImageButton add_btn;
    private ArrayList<WordModel> wordModelArrayList;
    private ArrayList<StringTranslation> wordStringArrayList;
    private DbCustomAdapter customAdapter;
    private MyDbHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_all_words);
        listView = (ListView) findViewById(R.id.lv);
        find=(SearchView) findViewById(R.id.searchView);
        find.setQueryHint("Найти слово");
        add_btn=(ImageButton)findViewById(R.id.add_new_word);
        final String name=getIntent().getExtras().getString("name");

        databaseHelper = new MyDbHelper(this,name);

        wordModelArrayList = databaseHelper.getAllWords();
       // wordStringArrayList=databaseHelper.getAllWords(1);
        customAdapter = new DbCustomAdapter(this,wordModelArrayList);
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(GetAllWordsActivity.this, UpdateDeleteActivity.class);
                intent.putExtra("word", wordModelArrayList.get(position));
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GetAllWordsActivity.this,AddWordActivity.class);
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });
        find.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return  false; }

            @Override
            public boolean onQueryTextChange(String newText) {


                newText = newText.toLowerCase();

                if(newText.length()==0){
                    wordModelArrayList = databaseHelper.getAllWords();
                    // wordStringArrayList=databaseHelper.getAllWords(1);
                    customAdapter = new DbCustomAdapter(getApplicationContext(),wordModelArrayList);
                    listView.setAdapter(customAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(GetAllWordsActivity.this, UpdateDeleteActivity.class);
                            intent.putExtra("word", wordModelArrayList.get(position));
                            intent.putExtra("name",name);
                            startActivity(intent);
                        }
                    });
                }else{
                    wordModelArrayList = databaseHelper.getAllWordsStartingWith(newText);
                    // wordStringArrayList=databaseHelper.getAllWords(1);
                    customAdapter = new DbCustomAdapter(getApplicationContext(),wordModelArrayList);
                    listView.setAdapter(customAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(GetAllWordsActivity.this, UpdateDeleteActivity.class);
                            intent.putExtra("word", wordModelArrayList.get(position));
                            intent.putExtra("name",name);
                            startActivity(intent);
                        }
                    });
                }




                return true;
            }
        });
    }
}
