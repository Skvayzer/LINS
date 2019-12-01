package com.example.mp11.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.example.mp11.R;
import com.example.mp11.ForDictionaries.CategDictionary;
import com.example.mp11.ForDictionaries.UsersDictionary;
import com.example.mp11.activities.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DictDescriptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DictDescriptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//фрагмент с описанием словаря пользователя
public class DictDescriptionFragment extends Fragment implements IOnBackPressed {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UsersDictionary ud;
    FirebaseDatabase database;
    private OnFragmentInteractionListener mListener;

    public DictDescriptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DictDescriptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DictDescriptionFragment newInstance(String param1, String param2) {
        DictDescriptionFragment fragment = new DictDescriptionFragment();
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
        View view=inflater.inflate(R.layout.fragment_dict_description, container, false);
        //находим вьюшки
        final TextView description=(TextView)view.findViewById(R.id.dict_description);
        TextView dict=(TextView)view.findViewById(R.id.users_dict_name);
        TextView holder=(TextView)view.findViewById(R.id.username);
        final TextView lastEdit=(TextView)view.findViewById(R.id.lastEdit);
        final TextView words_count=(TextView)view.findViewById(R.id.dicts_words_count);

        LinearLayout downloading=(LinearLayout)view.findViewById(R.id.download_dict);
        LinearLayout view_words=(LinearLayout)view.findViewById(R.id.view_words);

        CircleImageView image=(CircleImageView)view.findViewById(R.id.profile_image);
        //получаем бд Firebase и ссылку на корневой каталог
        database = FirebaseDatabase.getInstance();
        DatabaseReference ref=database.getReference();

        //загружаем картинку профиля по ссылке из объекта словаря пользователя
        Glide.with(getContext()).load(ud.imageUrl).placeholder(getResources().getDrawable(R.drawable.diam)).error(getResources().getDrawable(R.drawable.diam)).into(image);
        //Picasso.get().load(ud.imageUrl).placeholder(getResources().getDrawable(R.drawable.diam)).error(getResources().getDrawable(R.drawable.diam)).into(image);
        //устанавливаем имя пользователя и название словаря
        holder.setText(ud.holderName);
        dict.setText(ud.name);
        //читаем данные с Firebase бд
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //берём из бд Firebase словарь по id его владельца и названию
                DataSnapshot dictInfo=dataSnapshot.child("dictionaries").child(ud.holderId).child(ud.name);
                //загружаем оттуда описание словаря
                description.setText(dictInfo.child("description").getValue().toString());
                //узнаём кол-во слов в словаре и отображаем
                int count=0;
                for(DataSnapshot ds:dictInfo.child("dictionary").getChildren()) count++;
                words_count.setText(String.valueOf(count));
                //узнаём из бд дату последнего редактирования и отображаем
                DataSnapshot lastEditInfo=dataSnapshot.child("dictionaries").child(ud.holderId).child(ud.name).child("lastEdit");
                lastEdit.setText(lastEditInfo.getValue().toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //словарь можно скачать себе по клику
        downloading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //всплыващее окошко с загрузкой
                final ProgressDialog pd=new ProgressDialog(getContext());
                pd.setMessage("Загрузка...");
                pd.show();
                //скачивание словаря себе и копирование его себе в Firebase
                CategDictionary.downloadDict(ud.holderId,ud.name,getContext());
                //закрытие окошка с загрузкой
                pd.dismiss();
            }
        });
        //по клику можно посмотреть слова в этом словаре
        view_words.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //определяем фрагмент, кладём в него информацию об id владельца словаря и названию словаря
                //заменяем текущий фрагмент новым, для просмотра списка слов
                Fragment word_list= ViewWordsFragment.newInstance("kek","lol");
                ((ViewWordsFragment) word_list).userId=ud.holderId;
                ((ViewWordsFragment) word_list).dictName=ud.name;
                ((MainActivity)getActivity()).selectedFragment=word_list;
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, word_list).addToBackStack(null)
                        .commitAllowingStateLoss();

            }
        });
        //по клику на аватарку пользователя можно перейти в его профиль
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //определяем для этого фрагент, кладём туда информацию об id владельца словаря,
                //названии словаря, ссылки на аватар пользователя
                //заменяем текущий фрагмент новым, для просмотра профиля владельца словаря
                Fragment user_profile= UsersProfileFragment.newInstance("kek","lol");
                ((UsersProfileFragment) user_profile).userID=ud.holderId;
                ((UsersProfileFragment) user_profile).nickname=ud.holderName;
                ((UsersProfileFragment) user_profile).imageURL=ud.imageUrl;

                ((MainActivity)getActivity()).selectedFragment=user_profile;
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, user_profile).addToBackStack(null)
                        .commitAllowingStateLoss();

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

    @Override
    public boolean onBackPressed() {
        return false;
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
