package com.example.appshopbanhang;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIApiService {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    Call<OpenAIResponse> sendMessage(
            @Header("Authorization") String apiKey,
            @Body OpenAIRequest request
    );
}
