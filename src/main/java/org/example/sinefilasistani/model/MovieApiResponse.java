package org.example.sinefilasistani.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Bu sınıf, TMDb API'sinin ana yanıt yapısını temsil eder
public class MovieApiResponse {

    // JSON'daki "results" alanını bu listeye ata
    @SerializedName("results")
    private List<Movie> results;

    public List<Movie> getResults() {
        return results;
    }
}