package com.example.mp11;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp11.yandex.dictslate.CurrentTranslation;
import com.example.mp11.yandex.dictslate.Model;
import com.example.mp11.yandex.dictslate.RestApi;
import com.example.mp11.yandex.dictslate.TranslateApi;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PopActivity extends Activity {
    private static String DICT_URI_JSON="https://dictionary.yandex.net/";
    private static final String SAMPLE_KEY = "dict.1.1.20190329T031256Z.531eac57eaa2f4eb.57b8133578fb75df82180bc54fe6602d41795051";
    private static String PREDICTOR_URI_JSON = "https://predictor.yandex.net/";
    private static String PREDICTOR_KEY = "pdct.1.1.20190329T025447Z.ddc959b4c0378977.ad7eb16cc17f103f41d07f0ec3709053ba8f98af";

    private static String TRLATE_URI_JSON="https://translate.yandex.net/";
    private static String TRLATE_KEY="trnsl.1.1.20190223T194026Z.b1b9b8f5c1c1c647.f16aa370569e624242bde6cb461e7e3d9c155685";
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        tv=(TextView)findViewById(R.id.translation);

        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        getWindow().setLayout((int)(width*0.7),(int)(height*0.6));

        WindowManager.LayoutParams params=getWindow().getAttributes();
        params.gravity= Gravity.CENTER;
        params.x=0;
        params.y=-20;

        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        String word=getIntent().getStringExtra("word");

        getReport(word);


    }
    void getReport(String text) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DICT_URI_JSON)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi service = retrofit.create(RestApi.class);
        Call<Model> call = service.lookup(SAMPLE_KEY, "en-ru", text);
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {

                //String textWord = response.toString();
                Gson gson = new Gson();
                if (response.isSuccessful()) {

                    String successResponse = gson.toJson(response.body());
                    Log.d("RESULT", "successResponse: " + successResponse);
                } else {

                    if (null != response.errorBody()) {
                        String errorResponse = null;
                        try {
                            errorResponse = response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("RUSELT", "errorResponse: " + errorResponse);
                    }

                }

                String s="";
                tv.append("\n");
                for(int i=0;i<response.body().def.length;i++){
                    for(int j=0;j<response.body().def[i].tr.length;j++){
                        s=response.body().def[i].tr[j].text + "\t"+"\n";
                        tv.append(s);
                    }
                }




                //Toast.makeText(getApplicationContext(),"Вот дерьмо",Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    Toast.makeText(getApplicationContext(),"Вот дерьмо",Toast.LENGTH_SHORT).show();
                    throwable.printStackTrace();
                }
            }
        });
    }
    void translate(String text){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TRLATE_URI_JSON)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TranslateApi service = retrofit.create(TranslateApi.class);
        Call<CurrentTranslation> call = service.translate(TRLATE_KEY, text,"en-ru");
        call.enqueue(new Callback<CurrentTranslation>() {
            @Override
            public void onResponse(Call<CurrentTranslation> call, Response<CurrentTranslation> response) {

                //String textWord = response.toString();
                Gson gson = new Gson();
                if (response.isSuccessful()) {

                    String successResponse = gson.toJson(response.body());
                    Log.d("RESULT", "successResponse: " + successResponse);
                } else {

                    if (null != response.errorBody()) {
                        String errorResponse = null;
                        try {
                            errorResponse = response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("RUSELT", "errorResponse: " + errorResponse);
                    }

                }

                String s=response.body().text[0];




                //Toast.makeText(getApplicationContext(),"Вот дерьмо",Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onFailure(Call<CurrentTranslation> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    Toast.makeText(getApplicationContext(),"Вот дерьмо",Toast.LENGTH_SHORT).show();
                    throwable.printStackTrace();
                }
            }
        });
    }
    @Override
    protected void onPause(){
        super.onPause();
        Intent i = new Intent();
        i.putExtra("show", 1);
        setResult(RESULT_OK, i);
        finish();
    }
}
