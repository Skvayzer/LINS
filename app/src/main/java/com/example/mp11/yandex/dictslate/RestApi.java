package com.example.mp11.yandex.dictslate;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestApi {
    @GET("api/v1/dicservice.json/lookup")
    Call<Model> lookup(@Query("key") String key, @Query("lang") String lang, @Query("text") String text);
    // api/v1/dicservice.json/lookup?key=API-ключ&lang=en-ru&text=time

//    @GET("api/v1/predict.json/complete")
//    Call<Model> predict(@Query("key") String key, @Query("q") String q, @Query("lang") String lang);
}