package org.example.sinefilasistani.service;

import com.google.gson.Gson;
import org.example.sinefilasistani.model.*; // Hepsini import et
import org.example.sinefilasistani.util.Config;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TMDbService {

    // --- YENİ EKLENDİ: Singleton deseni ---
    private static TMDbService instance;

    public static TMDbService getInstance() {
        if (instance == null) {
            instance = new TMDbService();
        }
        return instance;
    }
    // --- BİTTİ ---

    private static final String API_KEY = Config.getApiKey();
    private static final String BASE_API_URL = "https://api.themoviedb.org/3";

    private static final String POPULAR_MOVIES_URL = BASE_API_URL + "/movie/popular?language=tr-TR&page=1&api_key=" + API_KEY;
    private static final String SEARCH_MOVIES_URL_BASE = BASE_API_URL + "/search/movie?language=tr-TR&page=1&api_key=" + API_KEY;
    private static final String MOVIE_CREDITS_URL_TEMPLATE = BASE_API_URL + "/movie/%d/credits?language=tr-TR&api_key=" + API_KEY;
    // BU YENİ HALİ:
    private static final String ACTOR_DETAILS_URL_TEMPLATE = BASE_API_URL + "/person/%d?language=en-US&api_key=" + API_KEY;
    private static final String ACTOR_FILMOGRAPHY_URL_TEMPLATE = BASE_API_URL + "/person/%d/movie_credits?language=tr-TR&api_key=" + API_KEY;

    private final HttpClient httpClient;
    private final Gson gson;

    // --- GÜNCELLENDİ: 'private' yapıldı ---
    private TMDbService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    private <T> CompletableFuture<T> sendRequestAndParse(HttpRequest request, Class<T> responseClass) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(jsonBody -> gson.fromJson(jsonBody, responseClass));
    }

    public CompletableFuture<List<Movie>> getPopularMovies() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POPULAR_MOVIES_URL))
                .GET().build();

        return this.sendRequestAndParse(request, MovieApiResponse.class)
                .thenApply(MovieApiResponse::getResults);
    }

    public CompletableFuture<List<Movie>> searchMovies(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String finalSearchUrl = SEARCH_MOVIES_URL_BASE + "&query=" + encodedQuery;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(finalSearchUrl))
                    .GET().build();

            return this.sendRequestAndParse(request, MovieApiResponse.class)
                    .thenApply(MovieApiResponse::getResults);

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

    public CompletableFuture<List<CastMember>> getMovieCredits(int movieId) {
        String url = String.format(MOVIE_CREDITS_URL_TEMPLATE, movieId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET().build();

        return this.sendRequestAndParse(request, MovieCreditsResponse.class)
                .thenApply(MovieCreditsResponse::getCast)
                .exceptionally(ex -> {
                    System.err.println("Oyuncu kadrosu alınırken hata: " + ex.getMessage());
                    return Collections.emptyList();
                });
    }

    public CompletableFuture<Actor> getActorDetails(int actorId) {
        String url = String.format(ACTOR_DETAILS_URL_TEMPLATE, actorId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET().build();

        return this.sendRequestAndParse(request, Actor.class)
                .exceptionally(ex -> {
                    System.err.println("Oyuncu detayları alınırken hata: " + ex.getMessage());
                    return null;
                });
    }

    public CompletableFuture<List<Movie>> getActorFilmography(int actorId) {
        String url = String.format(ACTOR_FILMOGRAPHY_URL_TEMPLATE, actorId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET().build();

        return this.sendRequestAndParse(request, ActorFilmographyResponse.class)
                .thenApply(ActorFilmographyResponse::getFilmography)
                .exceptionally(ex -> {
                    System.err.println("Oyuncu filmografisi alınırken hata: " + ex.getMessage());
                    return Collections.emptyList();
                });
    }
}