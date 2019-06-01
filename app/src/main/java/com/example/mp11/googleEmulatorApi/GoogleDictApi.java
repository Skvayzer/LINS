package com.example.mp11.googleEmulatorApi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleDictApi {
    @GET("/")
    Call<GoogleTranslation> define(@Query("define") String define);
}
