package com.example.fyp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MovieRecAdapter extends RecyclerView.Adapter<MovieRecAdapter.MovieViewHolder> {

    private Context context;
    private List<MovieRetriever> movieList;

    private List<MovieRetriever> movieListFull; // To keep the original list


    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(int movieId);
    }

    public MovieRecAdapter(Context context, List<MovieRetriever> movieList, OnMovieClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.movieListFull = new ArrayList<>(movieList); // Initialize with a copy of the original list
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieRetriever movie = movieList.get(position);
        holder.titleTextView.setText(movie.getTitle());
        holder.genreTextView.setText(movie.getGenre());

        Glide.with(context).load(movie.getPosterUrl()).into(holder.posterImageView);

        holder.posterImageView.setOnClickListener(v -> listener.onMovieClick(movie.getMovieId()));
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public void filter(String query) {
        movieList.clear();
        if (query.isEmpty()) {
            movieList.addAll(movieListFull);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (MovieRetriever movie : movieListFull) {
                if (movie.getTitle().toLowerCase().contains(filterPattern) ||
                        movie.getGenre().toLowerCase().contains(filterPattern)) {
                    movieList.add(movie);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {

        ImageView posterImageView;
        TextView titleTextView;

        TextView genreTextView;



        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.movie_poster);
            titleTextView = itemView.findViewById(R.id.movie_title);
            genreTextView = itemView.findViewById(R.id.movie_genre);
        }
    }
}
