package com.example.mp11;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mp11.MyDatabase.MyDbHelper;
import com.example.mp11.MyDatabase.WordModel;

public class UpdateDeleteActivity extends AppCompatActivity {
    private com.example.mp11.MyDatabase.WordModel WordModel;
    private EditText etword, etdefinition, etsyns;
    private Button btnupdate, btndelete;
    private MyDbHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_delete);
        Intent intent = getIntent();
        WordModel = (WordModel) intent.getSerializableExtra("word");

        databaseHelper = new MyDbHelper(this);

        etword = (EditText) findViewById(R.id.etword);
        etdefinition = (EditText) findViewById(R.id.etdefinition);
        etsyns = (EditText) findViewById(R.id.etsyns);
        btndelete = (Button) findViewById(R.id.btndelete);
        btnupdate = (Button) findViewById(R.id.btnupdate);

        etword.setText(WordModel.getWord());
        etdefinition.setText(WordModel.getdefinition());
        etsyns.setText(WordModel.getSyns());

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
    }
}
