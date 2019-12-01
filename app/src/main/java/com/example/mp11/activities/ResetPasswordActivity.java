package com.example.mp11.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

//активность с восстановлением пароля
public class ResetPasswordActivity extends AppCompatActivity {
    private EditText inputEmail;
    private Button btnReset;
    private TextView btnBack;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        inputEmail = (EditText) findViewById(R.id.useremail_reset);
        btnReset = (Button) findViewById(R.id.reset_password);
        btnBack = (TextView) findViewById(R.id.back_reset_password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //берём состояние авторизации в приложении
        auth = FirebaseAuth.getInstance();

        //перейти назад(завершить активность), если нажали на ссылку
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //берём введённый пользователем email
                String email = inputEmail.getText().toString().trim();

                //если он пустой, поругать и ничего не делать
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), getString(R.string.type_email_you_registered), Toast.LENGTH_SHORT).show();
                    return;
                }

                //анимация выполнения действий(типа загрузки)
                progressBar.setVisibility(View.VISIBLE);
                //запрос восстановления пароля на email
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //если всё получилось
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.we_sent_email), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.error_with_email), Toast.LENGTH_SHORT).show();
                                }

                                //спрятать анимацию
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }
}
