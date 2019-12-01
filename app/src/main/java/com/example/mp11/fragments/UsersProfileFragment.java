package com.example.mp11.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.example.mp11.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;


import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UsersProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UsersProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

//фрагмент с профилем владельца словаря
public class UsersProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    TextView dictsCount;
    FirebaseDatabase database;
    public  String nickname, imageURL, userID;

    public UsersProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UsersProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsersProfileFragment newInstance(String param1, String param2) {
        UsersProfileFragment fragment = new UsersProfileFragment();
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
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_users_profile, container, false);
        TextView holder=(TextView)view.findViewById(R.id.username);
        final TextView level=(TextView)view.findViewById(R.id.level);
        dictsCount=(TextView)view.findViewById(R.id.users_dicts_count);
        CircleImageView image=(CircleImageView)view.findViewById(R.id.profile_image);
        //подключаемся к бд Firebase
        database = FirebaseDatabase.getInstance();
        DatabaseReference ref=database.getReference();
        //устанавливаем имя пользователя и его аватар по ссылке, переданную при переходе к этому фрагменту
        holder.setText(nickname);
        Glide.with(getContext()).load(imageURL).placeholder(getResources().getDrawable(R.drawable.diam)).error(getResources().getDrawable(R.drawable.diam)).into(image);
        //Picasso.get().load(imageURL).placeholder(getResources().getDrawable(R.drawable.diam)).error(getResources().getDrawable(R.drawable.diam)).into(image);
        //чтение из Firebase
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DataSnapshot dictInfo=dataSnapshot.child("dictionaries").child(userID);
                //считаем кол-во словарей пользователя и отображаем
                int count=0;
                for(DataSnapshot ds:dictInfo.getChildren()) count++;
                dictsCount.setText(String.valueOf(count));
                //считываем уровень владения английским и отображаем
                DataSnapshot userInfo=dataSnapshot.child("users").child(userID).child("level");
                if(userInfo.getValue()!=null)
                level.setText(userInfo.getValue().toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
