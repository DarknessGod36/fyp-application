package com.example.fyp;

public class Recommendation {
    private String title;

    private String genre;
    private String Poster;
    private String movieid;

    private String url;

    public Recommendation(String title, String posterUrl, String movieUrl) {
        this.title = title;
        this.Poster = posterUrl;
        this.url = movieUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getPoster() {
        return Poster;
    }


    public String getMovieid() {
        return movieid;
    }

    public String getMovieurl() {
        return url;
    }


}