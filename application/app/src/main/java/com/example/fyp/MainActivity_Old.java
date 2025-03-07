//package com.example.fyp;
//
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class MainActivity_Old extends AppCompatActivity {
//    private EditText userIdEditText, productIdEditText, topNEditText;
//    private Button fetchRecommendationsButton;
//    private TextView recommendationsTextView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        userIdEditText = findViewById(R.id.userIdEditText);
//        productIdEditText = findViewById(R.id.productIdEditText);
//        topNEditText = findViewById(R.id.topNEditText);
//        fetchRecommendationsButton = findViewById(R.id.fetchRecommendationsButton);
//        recommendationsTextView = findViewById(R.id.recommendationsTextView);
//
//        fetchRecommendationsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fetchRecommendations();
//            }
//        });
//    }
//
//    private void fetchRecommendations() {
//        int userId = Integer.parseInt(userIdEditText.getText().toString());
//        int productId = Integer.parseInt(productIdEditText.getText().toString());
//        int topN = Integer.parseInt(topNEditText.getText().toString());
//
//        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
//        Call<List<Recommendation>> call = apiService.getRecommendations(userId, productId, topN, true);
//
//        call.enqueue(new Callback<List<Recommendation>>() {
//            @Override
//            public void onResponse(Call<List<Recommendation>> call, Response<List<Recommendation>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Recommendation> recommendations = response.body();
//                    displayRecommendations(recommendations);
//                } else {
//                    Toast.makeText(MainActivity.this, "Failed to get recommendations", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Recommendation>> call, Throwable t) {
//                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                Log.e("MainActivity", "Error fetching recommendations", t);
//            }
//        });
//    }
//
//    private void displayRecommendations(List<Recommendation> recommendations) {
//        StringBuilder recommendationsText = new StringBuilder("Recommendations:\n");
//        for (Recommendation recommendation : recommendations) {
//            recommendationsText.append(recommendation.getTitle()).append("\n");
//        }
//        recommendationsTextView.setText(recommendationsText.toString());
//    }
//}
