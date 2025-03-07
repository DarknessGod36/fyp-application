package com.example.fyp;

public class MovieRetriever {
    private int movieid;
    private String title;
    private String Poster;

    private String genre;

    public int getMovieId() {
        return movieid;
    }

    public void setMovieId(int movieId) {
        this.movieid = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterUrl() {
        return Poster;
    }

    public void setPosterUrl(String posterUrl) {
        this.Poster = posterUrl;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String movieGenre) {
        this.genre = movieGenre;
    }
}

