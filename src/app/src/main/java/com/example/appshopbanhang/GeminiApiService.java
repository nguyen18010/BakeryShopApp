package com.example.appshopbanhang;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApiService {

    @Headers("Content-Type: application/json")
    @POST("v1/models/gemini-2.5-flash:generateContent")
    Call<GeminiResponse> sendMessage(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );
}
