package com.example.mp11.yandex.dictslate;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;



public interface TranslateApi {
        @GET("api/v1.5/tr.json/translate")
        Call<CurrentTranslation> translate(@Query("key") String key, @Query("text") String text, @Query("lang") String lang);
        // api/v1/dicservice.json/lookup?key=API-ключ&lang=en-ru&text=time


    }

