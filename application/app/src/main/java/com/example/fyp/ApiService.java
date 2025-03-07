package com.example.fyp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("hybrid_recommend")
    Call<List<Recommendation>> getRecommendations(
            @Query("user_id") int userId,
            @Query("product_id") int productId,
            @Query("top_n") int topN,
            @Query("similarity") boolean similarity
    );
}