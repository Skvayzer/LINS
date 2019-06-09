package com.example.mp11.ForDictionaries.yandexdictslate;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

//для rest api для сбора определений слова
public interface RestApi {
    @GET("api/v1/dicservice.json/lookup")
    Call<Model> lookup(@Query("key") String key, @Query("lang") String lang, @Query("text") String text);

}