package com.example.mp11.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mp11.R;
import com.example.mp11.activities.AccountSettingsActivity;
import com.example.mp11.activities.SettingsActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment //extends PreferenceFragmentCompat {
        extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    int IMAGE_REQUEST=1011;
    DatabaseReference databaseReference;
    DatabaseReference databaseDictsReference;
    Uri imageUri;
    FirebaseUser user;
    StorageTask uploadTask;
    Context context;

    TextView dictsCount;
    Activity mActivity;


    private static Handler handler = new Handler();

    private OnFragmentInteractionListener mListener;

    Button btn;
    TextView username;
    private CircleImageView profile_image;
    private StorageReference storageReference;
    String mUri;
    LinearLayout account_settings,app_settings;


    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }




    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)   {


        final View view = inflater.inflate(R.layout.fragment_settings, container, false);

        final SharedPreferences preferences = mActivity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        //текущий пользователь
        user=FirebaseAuth.getInstance().getCurrentUser();
        //ссылки Firebase на папки профилч
        databaseReference= FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        storageReference= FirebaseStorage.getInstance().getReference("uploads");

        username=(TextView)view.findViewById(R.id.username);
        final TextView level=(TextView)view.findViewById(R.id.level);
        final LinearLayout l_level=(LinearLayout)view.findViewById(R.id.ll_level);


        dictsCount=(TextView)view.findViewById(R.id.users_dicts_count);
        profile_image=(CircleImageView)view.findViewById(R.id.profile_image);
        //считываем данные с Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //если есть имя пользователя, отобразить
                if(dataSnapshot.child("username").getValue()!=null)
                username.setText(dataSnapshot.child("username").getValue().toString());

                //ссылка на фото профиля
                mUri=preferences.getString("profile_image_url","");
                //если она не пустая, отобразить фото
                if(mUri!=null&&!mUri.equals("")) Glide.with(mActivity.getApplicationContext()).load(mUri).into(profile_image);
                //иначе взять ссылку с Firebase, если она есть, и отобразить фото
                else if(dataSnapshot.child("imageURL").getValue()!=null){ mUri=dataSnapshot.child("imageURL").getValue().toString();
                Glide.with(mActivity.getApplicationContext()).load(mUri).into(profile_image);
                }
                //если есть на Firebase, отобразить уровень английского
                if(dataSnapshot.child("level").getValue()!=null){
                    level.setText(dataSnapshot.child("level").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //отображение сохранённых кол-ва словарей,уровня, никнейма
        dictsCount.setText(preferences.getString("dicts_count","~"));
        username.setText(preferences.getString("username","Your profile"));
        level.setText(preferences.getString("level","Unknown"));
        //чтение с Firebase
        databaseDictsReference= FirebaseDatabase.getInstance().getReference();
        databaseDictsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //счёт кол-ва словарей и его отображение
                int size=0;
                DataSnapshot dataSnapshot1=dataSnapshot.child("dictionaries").child(user.getUid());
                for(DataSnapshot ds:dataSnapshot1.getChildren()) size++;
                dictsCount.setText(String.valueOf(size));
                //сохранение кол-ва словарей, никнейма и уровня ангийского
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString("dicts_count",String.valueOf(size));
                String myName=dataSnapshot.child("users").child(user.getUid()).child("username").getValue().toString();
                editor.putString("username",myName);
                if(dataSnapshot.child("users").child(user.getUid()).child("level").getValue()!=null) {
                    String myLevel = dataSnapshot.child("users").child(user.getUid()).child("level").getValue().toString();
                    editor.putString("level",myLevel);
                }
                editor.apply();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //по клику на аватар его можно поменять
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i,IMAGE_REQUEST);
            }
        });

        //по клику на имя пользователя его можно поменяять
        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //всплывающее окно для ввода именни
                AlertDialog.Builder pop = new AlertDialog.Builder(username.getContext());
                final AlertDialog kek=pop.create();
                final View current=LayoutInflater.from(username.getContext()).inflate(R.layout.change_username,null,false);
                current.setBackgroundColor(Color.WHITE);
                Button btn=(Button)current.findViewById(R.id.change_username);
                final EditText et=(EditText)current.findViewById(R.id.new_username);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name=et.getText().toString();
                        //имя может состоять только из букв и цифр
                        if(!name.equals("")&&name.matches("[a-zA-Z0-9 *]+$")){
                            //сохранение имени в Firebase
                           FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("username").setValue(name);
                            kek.hide();
                        }else{
                        Toast.makeText(getContext(),"Введите никнейм!",Toast.LENGTH_SHORT).show();
                    }
                    }
                });
                kek.setView(current);
                kek.show();
            }
        });
        //настройки аккаунта
        account_settings=(LinearLayout)view.findViewById(R.id.account_settings);
        account_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getContext(), AccountSettingsActivity.class);
                startActivity(i);
            }
        });
        //настройки приложения
        app_settings=(LinearLayout)view.findViewById(R.id.app_settings);
        app_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getContext(), SettingsActivity.class);
                startActivity(i);
            }
        });
        //выбор уровня владения английским
        l_level.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //окно с выбором
                AlertDialog.Builder pop = new AlertDialog.Builder(l_level.getContext());
                final AlertDialog kek=pop.create();
                final View current=LayoutInflater.from(l_level.getContext()).inflate(R.layout.selectlevel,null,false);

                current.setBackgroundColor(Color.WHITE);
                Button btn=(Button)current.findViewById(R.id.save);
                final RadioGroup rg = (RadioGroup) current.findViewById(R.id.radioGroup);
                //по клику сохранить
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //берем выбранный пункт и текст оттуда
                       int id= rg.getCheckedRadioButtonId();
                        RadioButton radioButton = (RadioButton) current.findViewById(id);
                        if(radioButton!=null){
                            //сохраняем в Firebase выбранный уровень
                        String your_level=radioButton.getText().toString();
                            databaseReference.child("level").setValue(your_level);
                            kek.hide();
                        }else{
                            Toast.makeText(l_level.getContext(),"Выберите!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                kek.setView(current);
                kek.show();
            }
        });
        return view;
    }
    //получить расширение фото для загрузки в Firebase storage
    private String getFileExtension(Uri uri){
        ContentResolver cr=getContext().getContentResolver();
        MimeTypeMap mtm=MimeTypeMap.getSingleton();
        return mtm.getExtensionFromMimeType(cr.getType(uri));
    }
    //функция загрузки аватара
    private void uploadImage(){
        final ProgressDialog pd=new ProgressDialog(getContext());
        //отображеение окна с загрузкой
        pd.setMessage("Загрузка...");
        pd.show();
        if(imageUri!=null){
            //кладём на Firebase storage аватар
            final StorageReference sr=storageReference.child("profileImages").child(user.getUid() + "."+getFileExtension(imageUri));
            uploadTask=sr.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    //получаем ссылку на скачивание фото
                    return sr.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        //берем ссылку на скачивание и сохраняем на утстройство и Firebase
                        Uri downloadUri=task.getResult();
                        mUri=downloadUri.toString();
                        SharedPreferences preferences = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putString("profile_image_url",mUri);
                        editor.apply();
                        HashMap<String, Object> map=new HashMap<>();
                        map.put("imageURL",mUri);
                        databaseReference.updateChildren(map);
                        //закрытие окошка
                        pd.dismiss();

                    }else{
                        Toast.makeText(getContext(),"Что-то пошло не так",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Что-то пошло не так",Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }else{
            Toast.makeText(getContext(),"Изображение не выбрано",Toast.LENGTH_SHORT).show();
        }
    }

    //после того, как пользователь выбрал в галерее аватар
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        //если это была выборка фото
        if( requestCode == IMAGE_REQUEST&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) {
            //берем uri к фото
            imageUri=data.getData();
            try {
                //конвертируем и устанавливаем на фото профиля
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profile_image.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            //если уже загрузка выполняется
            if(uploadTask!=null&&uploadTask.isInProgress()){

            }
            //иначе загрузить фото
            else{
                uploadImage();
            }
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if (context instanceof Activity){
            mActivity =(Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
