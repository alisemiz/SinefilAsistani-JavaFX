package org.example.sinefilasistani.util;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        // "config.properties" dosyasını resources klasöründen yüklüyoruz
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Üzgünüz, 'config.properties' dosyası bulunamadı.");
            } else {
                properties.load(input);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // API anahtarımızı getiren statik metot
    public static String getApiKey() {
        String apiKey = properties.getProperty("tmdb.api.key");
        if (apiKey == null || apiKey.equals("BURAYA_KENDI_API_ANAHTARINIZI_YAPISTIRIN")) {
            System.err.println("API Anahtarı 'config.properties' dosyasında ayarlanmamış!");
            return null;
        }
        return apiKey;
    }
}