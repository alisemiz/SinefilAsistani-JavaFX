package org.example.sinefilasistani.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// TMDb API'sinin /movie/{id}/credits yanıtını yakalamak için
public class MovieCreditsResponse {

    // Yanıttaki "cast" dizisini bu listeye ata
    @SerializedName("cast")
    private List<CastMember> cast;

    public List<CastMember> getCast() {
        return cast;
    }
}