package org.example.sinefilasistani.model;

import com.google.gson.annotations.SerializedName;

public class Movie {

    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("overview")
    private String overview;

    // --- YENİ EKLENDİ: Puan ve Tarih ---
    @SerializedName("vote_average")
    private double voteAverage; // örn: 8.4
    @SerializedName("release_date")
    private String releaseDate; // örn: "2024-10-27"
    // --- BİTTİ ---

    private boolean isWatched = false;

    /**
     * Veritabanından veri okurken kullanmak için kurucu metot.
     * --- GÜNCELLENDİ: 'voteAverage' ve 'releaseDate' eklendi ---
     */
    public Movie(int id, String title, String overview, String posterPath, boolean isWatched, double voteAverage, String releaseDate) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.isWatched = isWatched;
        this.voteAverage = voteAverage; // YENİ EKLENDİ
        this.releaseDate = releaseDate; // YENİ EKLENDİ
    }

    // --- Getter metotları ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public boolean isWatched() { return isWatched; }

    // --- YENİ EKLENDİ ---
    public double getVoteAverage() { return voteAverage; }
    public String getReleaseDate() { return releaseDate; }
    // --- BİTTİ ---


    public String getPosterUrl() {
        if (posterPath != null && !posterPath.isEmpty()) {
            if (posterPath.startsWith("http")) {
                return posterPath;
            }
            return "https://image.tmdb.org/t/p/w500" + posterPath;
        } else {
            return null;
        }
    }
}