package com.example.fyp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private int userId;
    private int movieId;
    private Button btnReviews;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnReviews = findViewById(R.id.btn_reviews);

        btnReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // URL of the Google Form
                String url = "https://forms.gle/fbf9jWA2Hj8u3aoG9"; // Put Google Form URL

                // Create an Intent to open the URL
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                // Start the activity
                startActivity(intent);
            }
        });


        userId = getIntent().getIntExtra("USER_ID", -1);
        movieId = getIntent().getIntExtra("MOVIE_ID", -1);

        if (userId != -1 && movieId != -1) {
            Log.d("GenreChoice", "Checking userId(MainActivity): " + userId);
            fetchMovies(userId, movieId);
        } else {
            Toast.makeText(this, "Invalid User or Movie ID", Toast.LENGTH_SHORT).show();
        }

    }
    private void fetchMovies(int userId, int movieId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Recommendation>> call = apiService.getRecommendations(userId, movieId, 10, true);

        call.enqueue(new Callback<List<Recommendation>>() {
            @Override
            public void onResponse(Call<List<Recommendation>> call, Response<List<Recommendation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Recommendation> movies = response.body();
                    movieAdapter = new MovieAdapter(MainActivity.this, movies, userId);
                    recyclerView.setAdapter(movieAdapter);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Recommendation>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
