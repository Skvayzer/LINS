package com.example.mp11;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class MyRegistrationActivity extends AppCompatActivity {
    EditText etlog,etpassword1,etpassword2,etNick;
    Button btndone;
    FirebaseAuth Auth;
    FirebaseAuth.AuthStateListener AuthListener;
    TextView link;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_registration);

        etlog=(EditText)findViewById(R.id.etlogin);
        etpassword1=(EditText)findViewById(R.id.etpassword1);
        etpassword2=(EditText)findViewById(R.id.etpassword2);
        etNick=(EditText)findViewById(R.id.nickname);
        btndone=(Button)findViewById(R.id.regdonebtn);
        link=(TextView)findViewById(R.id.link_login);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
        Auth=FirebaseAuth.getInstance();
        AuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null){
                    final String nickname=etNick.getText().toString();
                    DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("users")
                            .child(user.getUid()).child("username");
                    ref.setValue(nickname);
                    Uri uri = Uri.parse("android.resource://com.example.mp11/drawable/diam");
                    final StorageReference sr= FirebaseStorage.getInstance().getReference("uploads")
                            .child("profileImages").child(user.getUid() + "."+"png");
                    StorageTask uploadTask= sr.putFile(uri);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()){
                                throw task.getException();
                            }
                            return sr.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                Uri downloadUri=task.getResult();
                                String mUri=downloadUri.toString();
                                SharedPreferences preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor=preferences.edit();
                                editor.putString("profile_image_url",mUri);
                                editor.apply();
                                HashMap<String, Object> map=new HashMap<>();
                                map.put("imageURL",mUri);
                                FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).updateChildren(map);


                            }
                        }
                    });

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
