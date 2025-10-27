package org.example.sinefilasistani.model;

import com.google.gson.annotations.SerializedName;

// Bir oyuncunun detay sayfasındaki tüm bilgileri temsil eder
public class Actor {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("biography")
    private String biography; // Biyografi

    @SerializedName("birthday")
    private String birthday; // Doğum tarihi

    @SerializedName("profile_path")
    private String profilePath; // Büyük boy profil fotoğrafı

    // Getter'lar
    public int getId() { return id; }
    public String getName() { return name; }
    public String getBiography() { return biography; }
    public String getBirthday() { return birthday; }

    // Profil fotoğrafının tam URL'sini döndüren yardımcı metot
    public String getProfileUrl() {
        if (profilePath != null && !profilePath.isEmpty()) {
            return "https://image.tmdb.org/t/p/w500" + profilePath; // w500 = büyük boy resim
        }
        return null;
    }
}