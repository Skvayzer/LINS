package com.example.mp11;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Button btn=(Button)findViewById(R.id.popbtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(Main2Activity.this, v);
                MenuInflater inflater=popup.getMenuInflater();
                inflater.inflate(R.menu.popsubs,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu1:
                                Toast.makeText(getApplicationContext(),
                                        "Eng subs",
                                        Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.menu2:
                                Toast.makeText(getApplicationContext(),
                                        "Rus subs",
                                        Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.menu3:
                                Toast.makeText(getApplicationContext(),
                                        "Freeze subs",
                                        Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popup.show();
            }
        });
    }
}
