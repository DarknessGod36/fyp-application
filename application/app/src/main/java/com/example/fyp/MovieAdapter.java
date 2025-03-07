package com.example.fyp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private List<Recommendation> movieList;
    private int userId;

    public MovieAdapter(Context context, List<Recommendation> movieList, int userId) {
        this.context = context;
        this.movieList = movieList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_rating, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Recommendation movie = movieList.get(position);
        holder.titleTextView.setText(movie.getTitle());
        holder.genreTextView.setText(movie.getGenre());

        String posterUrl = movie.getPoster();
        Log.d("MovieAdapter", "Loading poster URL: " + posterUrl);

        if (posterUrl != null && !posterUrl.isEmpty()) {
            Glide.with(context)
                    .load(posterUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.posterImageView);
        } else {
            holder.posterImageView.setImageResource(R.drawable.error_image);
        }

        holder.rateButton.setOnClickListener(v -> showRatingDialog(userId, movie.getMovieid()));

        holder.posterImageView.setOnClickListener(v -> {
            String movieUrl = movie.getMovieurl();
            if (movieUrl != null && !movieUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(movieUrl));
                context.startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    private void showRatingDialog(int userId, String movieId) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.rating_dialog);

        final RatingBar ratingBar = dialog.findViewById(R.id.dialog_rating_bar);
        Button submitButton = dialog.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            saveRatingToFirebase(userId, movieId, rating);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveRatingToFirebase(int userId, String movieId, int rating) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://recommender-system-53372-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference ratingsRef = database.getReference("ratings");

        Rating ratingObj = new Rating(String.valueOf(userId), movieId, rating);

        ratingsRef.child(String.valueOf(userId))
                .child(movieId)
                .setValue(ratingObj)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("MovieAdapter", "Rating saved successfully.");
                        } else {
                            Log.d("MovieAdapter", "Failed to save rating.");
                        }
                    }
                });
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;
        TextView titleTextView;
        TextView genreTextView;
        Button rateButton;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.poster_image_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            genreTextView = itemView.findViewById(R.id.genre_text_view);
            rateButton = itemView.findViewById(R.id.rate_button);
        }
    }

    public static class Rating {
        private String UserID;
        private String MovieID;
        private int rating;

        public Rating() {
        }

        public Rating(String userID, String movieID, int rating) {
            this.UserID = userID;
            this.MovieID = movieID;
            this.rating = rating;
        }

        public String getUserID() {
            return UserID;
        }

        public void setUserID(String userID) {
            UserID = userID;
        }

        public String getMovieID() {
            return MovieID;
        }

        public void setMovieID(String movieID) {
            MovieID = movieID;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }
    }
}
