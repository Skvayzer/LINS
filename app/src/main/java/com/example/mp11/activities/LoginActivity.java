package com.example.mp11.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText useremail, userpassword;
    private Button loginbtn;
   private FirebaseAuth Auth;
    private FirebaseAuth.AuthStateListener AuthListener;
    TextView link;
    private TextView reset_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        useremail = (EditText) findViewById(R.id.useremail);
        userpassword = (EditText) findViewById(R.id.userpassword);
        link=(TextView)findViewById(R.id.link_signup);
        reset_password=(TextView)findViewById(R.id.link_reset);
        //если нажат переход к регистрации
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //запускаем регистрацию
                Intent i=new Intent(getApplicationContext(), MyRegistrationActivity.class);
                startActivity(i);
                finish();
            }
        });
        //восстановление пароля
        reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(i);

            }
        });
        loginbtn = (Button) findViewById(R.id.loginbtn);
        Auth = FirebaseAuth.getInstance();
        //если пользователь уже вошёл, войти сразу в главную активность и завершить текущую
        AuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };

        //вход в аккаунт
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = useremail.getText().toString();
                String password = userpassword.getText().toString();


                    //проверка введённых данных
                    Auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //если войти не получилось
                                    if (!task.isSuccessful()) {
                                        //если нет подключения к интернету
                                        if(!isNetworkAvaliable(LoginActivity.this)) {
                                            Toast.makeText(LoginActivity.this, "Нет подключения к интернету", Toast.LENGTH_LONG).show();
                                        }else {
                                            //иначе ввёл неправильно
                                            Toast.makeText(LoginActivity.this, "Неверный E-mail или пароль", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                }
                            });



            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        //прикручиваем листенер к авторизации на старте активности, чтобы сразу перейти в главную, если уже пользователь вошёл
        Auth.addAuthStateListener(AuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //удаление листенера
        Auth.removeAuthStateListener(AuthListener);
    }
    //проверка подключения к интернету
    public static boolean isNetworkAvaliable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
                || (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED)) {
            return true;
        } else {
            return false;
        }
    }

}
