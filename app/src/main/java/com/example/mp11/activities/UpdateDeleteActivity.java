package com.example.mp11.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mp11.ForDatabases.MyDbHelper;
import com.example.mp11.ForDatabases.WordModel;
import com.example.mp11.R;

import java.util.ArrayList;

//активность для редактирования слова или определения
public class UpdateDeleteActivity extends AppCompatActivity {
    private com.example.mp11.ForDatabases.WordModel WordModel;
    private EditText etword;
    private Button btnupdate, btndelete, btnadd;
    private MyDbHelper databaseHelper;
    private LinearLayout ll;
    //кол-во введённых определений
    int num=0;
    //хранят динамически добавленные editText'ы
    ArrayList<EditText> aretdef=new ArrayList<>(),
    aretsyns=new ArrayList<>(),
    aretex=new ArrayList<>();
    //функция, создающая динамически editText'ы
    public void add() {

        EditText etdef = new EditText(this);
        EditText etsyns = new EditText(this);
        EditText etex = new EditText(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        etdef.setLayoutParams(p);
        etex.setLayoutParams(p);
        etsyns.setLayoutParams(p);
        etdef.setHint(getString(R.string.meanings));
        etsyns.setHint(getString(R.string.synonims));
        etex.setHint(getString(R.string.examples));
        //добавляем к layout'у
        ll.addView(etdef);
        ll.addView(etsyns);
        ll.addView(etex);
        num++;
        aretdef.add(etdef);
        aretsyns.add(etsyns);
        aretex.add(etex);
    }
    //функция для добавления определения, создающая динамически editText'ы
    public void add(String def, String syns, String ex) {

        EditText etdef = new EditText(this);
        EditText etsyns = new EditText(this);
        EditText etex = new EditText(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        etdef.setLayoutParams(p);
        etex.setLayoutParams(p);
        etsyns.setLayoutParams(p);
        etdef.setHint(getString(R.string.meanings));
        etsyns.setHint(getString(R.string.synonims));
        etex.setHint(getString(R.string.examples));
        etdef.setBackgroundColor(getResources().getColor(R.color.graydark));
        etsyns.setBackgroundColor(getResources().getColor(R.color.graydark));
        etex.setBackgroundColor(getResources().getColor(R.color.graydark));

        etdef.setText(def);
        etsyns.setText(syns);
        etex.setText(ex);
        ll.addView(etdef);
        ll.addView(etsyns);
        ll.addView(etex);
        num++;
        aretdef.add(etdef);
        aretsyns.add(etsyns);
        aretex.add(etex);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_delete);
        Intent intent = getIntent();
        //берём из интента класс слова с определениями, синонимами и примерами
        WordModel = (WordModel) intent.getSerializableExtra("word");
        ll=(LinearLayout)findViewById(R.id.updatelayout);
        //берём из интента название словаря этого слова
        final String name=getIntent().getExtras().getString("name");

        //вызываем помошниика для бд по названию текущего словаря
        databaseHelper = new MyDbHelper(this,name);

        etword = (EditText) findViewById(R.id.etword);
        btndelete = (Button) findViewById(R.id.btndelete);
        btnupdate = (Button) findViewById(R.id.btnupdate);
       // btnadd=(Button)findViewById(R.id.addmeaning);

        //показываем текущее слово
        etword.setText(WordModel.getWord());
        //пробегаемся по определениям и добавляем в edittext'ы
        for(int i=0;i<WordModel.definition.size();i++){
            //функция добавления
            add(WordModel.getdefinition(i),WordModel.getSyns(i),WordModel.getEx(i));

        }


        //сохранить изменения
        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < num; i++) {

                    //помошник бд обновляет слово
                    databaseHelper.updateWord(WordModel.getId(), etword.getText().toString(),aretdef.get(i).getText().toString(),
                            aretsyns.get(i).getText().toString(), aretex.get(i).getText().toString());
                }
                    Toast.makeText(UpdateDeleteActivity.this, getString(R.string.changed_successfully), Toast.LENGTH_SHORT).show();

                finish();

            }
        });

        //удалить слово или поределение
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //удаляем слово
                databaseHelper.deleteWord(WordModel.getId());
                Toast.makeText(UpdateDeleteActivity.this, getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
       // btnadd.setVisibility(View.INVISIBLE);

    }
}
