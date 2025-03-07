package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

public class MovieChoice extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieRecAdapter movieAdapter;
    private EditText searchInput;
    private Button searchButton;

    private int userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_choice);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Log.d("GenreChoice", "Checking userId: " + userId);
            Toast.makeText(this, "User ID not found. Please select a genre.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<MovieRetriever> movies = loadMoviesFromAsset();
        if (movies != null) {
            movieAdapter = new MovieRecAdapter(this, movies, (movieId)->{
                // Start MainActivity with the selected userId and movieId
                Intent intent = new Intent(MovieChoice.this, MainActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("MOVIE_ID", movieId);
                startActivity(intent);
            });
            recyclerView.setAdapter(movieAdapter);
            searchButton.setOnClickListener(v -> {
                String query = searchInput.getText().toString().trim();
                movieAdapter.filter(query);
            });
        } else {
            Toast.makeText(this, "Failed to load movies", Toast.LENGTH_SHORT).show();
        }
    }

    private List<MovieRetriever> loadMoviesFromAsset() {
        try {
            InputStream is = getAssets().open("movies.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            Gson gson = new Gson();
            Type movieListType = new TypeToken<List<MovieRetriever>>() {}.getType();
            return gson.fromJson(json, movieListType);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
