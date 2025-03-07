package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenreChoice extends AppCompatActivity {
    private ListView genreListView;
    private Map<String, Integer> genreToUserIdMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_choice);

        genreListView = findViewById(R.id.genre_list_view);

        // Define genres and corresponding user IDs
        genreToUserIdMap = new HashMap<>();
        genreToUserIdMap.put("Action", 1667);
        genreToUserIdMap.put("Adventure", 5337);
        genreToUserIdMap.put("Animation", 417);
        genreToUserIdMap.put("Children", 1);
        genreToUserIdMap.put("Comedy", 1120);
        genreToUserIdMap.put("Crime", 443);
        genreToUserIdMap.put("Documentary", 5962);
        genreToUserIdMap.put("Drama", 4140);
        genreToUserIdMap.put("Fantasy", 1996);
        genreToUserIdMap.put("Film-Noir", 2422);
        genreToUserIdMap.put("Horror", 2129);
        genreToUserIdMap.put("Musical", 2289);
        genreToUserIdMap.put("Mystery", 2555);
        genreToUserIdMap.put("Romance", 3087);
        genreToUserIdMap.put("Sci-Fi", 5801);
        genreToUserIdMap.put("Thriller", 1757);
        genreToUserIdMap.put("War", 4577);
        genreToUserIdMap.put("Western", 3823);




        // Populate ListView with genres
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>(genreToUserIdMap.keySet()));
        genreListView.setAdapter(adapter);

        // Set item click listener
        genreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedGenre = (String) parent.getItemAtPosition(position);
                int userId = genreToUserIdMap.get(selectedGenre);
                Log.d("GenreChoice", "Checking userId: " + userId);
                // Pass the selected user ID to MainActivity
                Intent intent = new Intent(GenreChoice.this, MovieChoice.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });
    }
}
