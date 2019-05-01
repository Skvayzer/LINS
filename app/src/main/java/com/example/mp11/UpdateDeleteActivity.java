package com.example.mp11;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.MyDatabase.WordModel;

public class UpdateDeleteActivity extends AppCompatActivity {
    private com.example.mp11.MyDatabase.WordModel WordModel;
    private EditText etword, etdefinition, etsyns;
    private Button btnupdate, btndelete, btnadd;
    private MyDbHelper databaseHelper;
    private LinearLayout ll;
    int num=1;
    public void add() {

        EditText etdef = new EditText(this);
        EditText etsyns = new EditText(this);
        EditText etex = new EditText(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        etdef.setLayoutParams(p);
        etex.setLayoutParams(p);
        etsyns.setLayoutParams(p);
        etdef.setHint("Значения слова");
        etsyns.setHint("Синонимы");
        etex.setHint("Примеры");
        ll.addView(etdef);
        ll.addView(etsyns);
        ll.addView(etex);
        num++;

    }
    public void add(String def, String syns, String ex) {

        EditText etdef = new EditText(this);
        EditText etsyns = new EditText(this);
        EditText etex = new EditText(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        etdef.setLayoutParams(p);
        etex.setLayoutParams(p);
        etsyns.setLayoutParams(p);
        etdef.setHint("Значения слова");
        etsyns.setHint("Синонимы");
        etex.setHint("Примеры");
        etdef.setText(def);
        etsyns.setText(syns);
        etex.setText(ex);
        ll.addView(etdef);
        ll.addView(etsyns);
        ll.addView(etex);
        num++;

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_delete);
        Intent intent = getIntent();
        WordModel = (WordModel) intent.getSerializableExtra("word");
        ll=(LinearLayout)findViewById(R.id.updatelayout);

        databaseHelper = new MyDbHelper(this,"TED");

          etword = (EditText) findViewById(R.id.etword);
//        etdefinition = (EditText) findViewById(R.id.etdefinition);
//        etsyns = (EditText) findViewById(R.id.etsyns);
        btndelete = (Button) findViewById(R.id.btndelete);
        btnupdate = (Button) findViewById(R.id.btnupdate);
        btnadd=(Button)findViewById(R.id.addmeaning);

        etword.setText(WordModel.getWord());
        for(int i=0;i<WordModel.definition.size();i++){
//            View view=new View(this);
//            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            view.setLayoutParams(p);
//
//            view.setBackgroundColor(Color.rgb(255,255,80));
//            ll.addView(view);
            add(WordModel.getdefinition(i),WordModel.getSyns(i),WordModel.getEx(i));

        }
       // etdefinition.setText(WordModel.);
       // etdefinition.setText(WordModel.getdefinition());
       // etsyns.setText(WordModel.getSyns());

        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.updateWord(WordModel.getId(),etword.getText().toString(),etdefinition.getText().toString(),etsyns.getText().toString(),"");
                Toast.makeText(UpdateDeleteActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateDeleteActivity.this,SocialFragment.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.deleteWord(WordModel.getId());
                Toast.makeText(UpdateDeleteActivity.this, "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateDeleteActivity.this,SocialFragment.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                add();
            }
        });
    }
}
