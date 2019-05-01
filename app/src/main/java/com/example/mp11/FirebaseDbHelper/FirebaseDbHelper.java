package com.example.mp11.FirebaseDbHelper;

import android.content.Context;
import android.widget.Toast;

import com.example.mp11.MainActivity;
import com.example.mp11.views.StringTranslation;
import com.example.mp11.views.TranslationItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseDbHelper {
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
    static DatabaseReference myRef = database.getReference(userID);
    static ArrayList<StringTranslation> list;
    public static void AddWord(String word,ArrayList<StringTranslation> list){



        myRef.child(word).setValue(list);

    }

    public static ArrayList<StringTranslation> getWord(String word){


        myRef.child(word).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<StringTranslation>> t = new GenericTypeIndicator<ArrayList<StringTranslation>>() {};
                list =dataSnapshot.getValue(t);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return list;
    }
}
