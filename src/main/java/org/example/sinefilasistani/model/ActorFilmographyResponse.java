package org.example.sinefilasistani.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// TMDb API'sinin /person/{id}/movie_credits yanıtını yakalamak için
public class ActorFilmographyResponse {

    // Yanıttaki "cast" dizisini (oynadığı filmler) bu listeye ata
    // DİKKAT: Buradaki liste, bizim mevcut "Movie" modelimizi yeniden kullanır!
    @SerializedName("cast")
    private List<Movie> filmography;

    public List<Movie> getFilmography() {
        return filmography;
    }
}