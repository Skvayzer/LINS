package com.example.mp11;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.mp11.views.SubtitleView;
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
import com.google.gson.Gson;

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
    // CheckBox box,trans;

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

//    @Override
//    public void onCreatePreferences(Bundle bundle, String rootKey) {
//        addPreferencesFromResource(R.xml.preferences);
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)   {
//        View view= inflater.inflate(R.layout.fragment_settings, container, false);
//        btn=(Button)view.findViewById(R.id.govideo);
//        box=(CheckBox)view.findViewById(R.id.chckservice);
//        trans=(CheckBox)view.findViewById(R.id.chcktranslation);
//        Button logout=(Button)view.findViewById(R.id.logout);
//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut();
//                Intent i=new Intent(getActivity(),LoginActivity.class);
//                startActivity(i);
//                getActivity().finish();
//
//            }
//        });
//        box.setSelected(true);
//        trans.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(box.isSelected()){
//                    box.setSelected(false);
//                    EasyWordsBtn.eng=true;
//                }else{
//                    box.setSelected(true);
//                    EasyWordsBtn.eng=false;
//                }
//            }
//        });
//        box.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(box.isSelected()){
//                    box.setSelected(false);
//                    Intent serv=new Intent(getActivity().getApplicationContext(),EasyWordsBtn.class);
//                    getActivity().stopService(serv);
//                }else{
//                    box.setSelected(true);
//                    Intent serv=new Intent(getActivity().getApplicationContext(),EasyWordsBtn.class);
//                    getActivity().startService(serv);
//                }
//            }
//        });
//
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent i=new Intent(getContext(), VideoPlayer.class);
////
////                startActivity(i);
//            }
//        });



        final View view = inflater.inflate(R.layout.fragment_settings, container, false);
//        // The frame you want to embed the parent layout in.
//        final ViewGroup innerContainer = (ViewGroup) view.findViewById(R.id.main_frame);
//        final View innerView = super.onCreateView(inflater, innerContainer, savedInstanceState);
//        if (innerView != null) {
//            innerContainer.addView(innerView);
//        }
        final SharedPreferences preferences = mActivity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        user=FirebaseAuth.getInstance().getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        storageReference= FirebaseStorage.getInstance().getReference("uploads");
        username=(TextView)view.findViewById(R.id.username);

        dictsCount=(TextView)view.findViewById(R.id.users_dicts_count);
        profile_image=(CircleImageView)view.findViewById(R.id.profile_image);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("username").getValue()!=null)
                username.setText(dataSnapshot.child("username").getValue().toString());
                if(user.getPhotoUrl()!=null&&user.getPhotoUrl().equals("default")){
                    profile_image.setImageResource(R.drawable.diam);
                }else{
                  //  profile_image.setImageURI(imageUri);
                    //SharedPreferences preferences = mActivity.getSharedPreferences("pref", Context.MODE_PRIVATE);
                    mUri=preferences.getString("profile_image_url","");
                    if(mUri!=null)
                   Glide.with(mActivity.getApplicationContext()).load(mUri).into(profile_image);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dictsCount.setText(preferences.getString("dicts_count","~"));
        databaseDictsReference= FirebaseDatabase.getInstance().getReference("dictionaries").child(user.getUid());
        databaseDictsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int size=0;
                for(DataSnapshot ds:dataSnapshot.getChildren()) size++;
                dictsCount.setText(String.valueOf(size));

                SharedPreferences.Editor editor=preferences.edit();
                editor.putString("dicts_count",String.valueOf(size));
                editor.apply();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i,IMAGE_REQUEST);
            }
        });


        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        if(!name.equals("")&&name.matches("[a-zA-Z0-9 *]+$")){
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
        account_settings=(LinearLayout)view.findViewById(R.id.account_settings);
        account_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getContext(),AccountSettingsActivity.class);
                startActivity(i);
            }
        });
        app_settings=(LinearLayout)view.findViewById(R.id.app_settings);
        app_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getContext(),SettingsActivity.class);
                startActivity(i);
            }
        });
        return view;
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr=getContext().getContentResolver();
        MimeTypeMap mtm=MimeTypeMap.getSingleton();
        return mtm.getExtensionFromMimeType(cr.getType(uri));
    }
    private void uploadImage(){
        final ProgressDialog pd=new ProgressDialog(getContext());
        pd.setMessage("Загрузка...");
        pd.show();
        if(imageUri!=null){
            final StorageReference sr=storageReference.child("profileImages").child(user.getUid() + "."+getFileExtension(imageUri));
            uploadTask=sr.putFile(imageUri);
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
                        mUri=downloadUri.toString();
                        SharedPreferences preferences = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putString("profile_image_url",mUri);
                        editor.apply();
                        HashMap<String, Object> map=new HashMap<>();
                        map.put("imageURL",mUri);
                        databaseReference.updateChildren(map);
                       // profile_image.setImageURI(imageUri);
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
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        SwitchPreference sp = (SwitchPreference)  getPreferenceManager().findPreference("switch");
//        sp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//
//            }
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if( requestCode == IMAGE_REQUEST&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            ((ImageView)inflatedView.findViewById(R.id.image)).setImageBitmap(photo);
            imageUri=data.getData();
//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1,1)
//                    .start(this);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

                profile_image.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if(uploadTask!=null&&uploadTask.isInProgress()){

            }else{
                uploadImage();
            }
           // StorageReference sr=storageReference.child("profileImages").child(user.getUid());

        }
//        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
//            CropImage.ActivityResult result=CropImage.getActivityResult(data);
//            if(resultCode==RESULT_OK){
//                Uri resultUri=result.getUri();
//            }
//        }
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
