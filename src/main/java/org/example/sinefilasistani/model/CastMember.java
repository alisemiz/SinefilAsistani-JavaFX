package org.example.sinefilasistani.model;

import com.google.gson.annotations.SerializedName;

// Bir filmin oyuncu kadrosundaki tek bir kişiyi temsil eder
public class CastMember {

    @SerializedName("id")
    private int id; // Oyuncunun ID'si

    @SerializedName("name")
    private String name; // Oyuncunun adı

    @SerializedName("character")
    private String character; // Oynadığı karakterin adı

    @SerializedName("profile_path")
    private String profilePath; // Oyuncunun profil fotoğrafı yolu

    // Getter'lar
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCharacter() { return character; }

    // Profil fotoğrafının tam URL'sini döndüren yardımcı metot
    public String getProfileUrl() {
        if (profilePath != null && !profilePath.isEmpty()) {
            return "https://image.tmdb.org/t/p/w185" + profilePath; // w185 = küçük boy resim
        }
        return null; // Fotoğrafı yoksa
    }
}