package com.example.mp11;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegOrLogActivity extends AppCompatActivity {
    Button regbtn, logbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_or_log);
        regbtn=(Button) findViewById(R.id.regbtn);
        logbtn=(Button)findViewById(R.id.logbtn);
        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(RegOrLogActivity.this, MyRegistrationActivity.class);
                startActivity(i);
                finish();
            }
        });
        logbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(RegOrLogActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


}
