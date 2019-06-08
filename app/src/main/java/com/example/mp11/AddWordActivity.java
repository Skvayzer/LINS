package com.example.mp11;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mp11.MyDatabase.MyDbHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AddWordActivity extends AppCompatActivity {
    private MessageListAdapter mMessageAdapter;
    private FloatingActionButton sendbtn;
    private EditText editText;
    private ListView lv;
    private MessageListAdapter adapter;

    private Button btnStore, btnGetall;
    private EditText etword, etdefinition, etsyns,etex;
    private MyDbHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        final String dict_name=getIntent().getExtras().getString("name");
        databaseHelper = new MyDbHelper(this,dict_name);

        btnStore = (Button) findViewById(R.id.btnstore);
      //  btnGetall = (Button) findViewById(R.id.btnget);
        etword = (EditText) findViewById(R.id.etword);
        etdefinition = (EditText) findViewById(R.id.etdefinition);
        etsyns = (EditText) findViewById(R.id.etsyns);
        etex=(EditText) findViewById(R.id.etex);
        btnStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.addWord(etword.getText().toString(), etdefinition.getText().toString(), etsyns.getText().toString(),etex.getText().toString());

                final SharedPreferences preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor=preferences.edit();
                Gson gson=new Gson();
                String rest=preferences.getString(dict_name +"-rest",null);
                ArrayList<String> rest_ar;
                if(rest!=null){
                    Type type = new TypeToken<List<String>>() {
                    }.getType();
                    rest_ar=gson.fromJson(rest,type);

                }else{
                    rest_ar=new ArrayList<>();
                }
                rest_ar.add(etword.getText().toString());
                editor.putString(dict_name+"-rest",gson.toJson(rest_ar));
                editor.apply();
                etword.setText("");
                etdefinition.setText("");
                etsyns.setText("");
                etex.setText("");
                Toast.makeText(AddWordActivity.this, "Сохранено успешно!", Toast.LENGTH_SHORT).show();
            }
        });

//        btnGetall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(AddWordActivity.this, GetAllWordsActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}
