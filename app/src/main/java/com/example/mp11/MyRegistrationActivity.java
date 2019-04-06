package com.example.mp11;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyRegistrationActivity extends AppCompatActivity {
    EditText etlog,etpassword1,etpassword2;
    Button btndone;
    FirebaseAuth Auth;
    FirebaseAuth.AuthStateListener AuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_registration);

        etlog=(EditText)findViewById(R.id.etlogin);
        etpassword1=(EditText)findViewById(R.id.etpassword1);
        etpassword2=(EditText)findViewById(R.id.etpassword2);
        btndone=(Button)findViewById(R.id.regdonebtn);

        Auth=FirebaseAuth.getInstance();
        AuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null){

                    Intent i=new Intent(MyRegistrationActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }
            }
        };
        btndone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email=etlog.getText().toString();
                final String password1=etpassword1.getText().toString();
                final String password2=etpassword2.getText().toString();
                if(password1.equals(password2)){
                    Auth.createUserWithEmailAndPassword(email,password1).addOnCompleteListener(MyRegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        Toast.makeText(MyRegistrationActivity.this,"Что-то пошло не так. Возможно, ввод некорректен", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                }else{
                    Toast.makeText(MyRegistrationActivity.this,"Не совпадают пароли", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Auth.addAuthStateListener(AuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Auth.removeAuthStateListener(AuthListener);
    }
}
