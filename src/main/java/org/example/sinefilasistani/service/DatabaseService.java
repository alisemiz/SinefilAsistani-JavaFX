package org.example.sinefilasistani.service;

import org.example.sinefilasistani.model.Movie;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static DatabaseService instance;
    private Connection connection;

    private static final String DB_URL = "jdbc:sqlite:sinefil_watchlist.db";

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private DatabaseService() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("SQLite veritabanına başarıyla bağlanıldı.");
            createWatchlistTable();
        } catch (Exception e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- GÜNCELLENDİ: 'vote_average' ve 'release_date' sütunları eklendi ---
    private void createWatchlistTable() {
        String sql = "CREATE TABLE IF NOT EXISTS watchlist ("
                + " tmdb_id INTEGER PRIMARY KEY NOT NULL,"
                + " title TEXT NOT NULL,"
                + " overview TEXT,"
                + " poster_path TEXT,"
                + " is_watched INTEGER DEFAULT 0,"
                + " vote_average REAL," // YENİ SÜTUN (örn: 8.4)
                + " release_date TEXT"  // YENİ SÜTUN (örn: "2024-10-27")
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Watchlist tablosu (puan ve tarih sütunlarıyla) başarıyla oluşturuldu.");
        } catch (Exception e) {
            System.err.println("Tablo oluşturma hatası: " + e.getMessage());
        }
    }

    // --- GÜNCELLENDİ: 'vote_average' ve 'release_date' ekleniyor ---
    public void addMovieToWatchlist(Movie movie) {
        String sql = "INSERT OR IGNORE INTO watchlist(tmdb_id, title, overview, poster_path, vote_average, release_date) VALUES(?,?,?,?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, movie.getId());
            pstmt.setString(2, movie.getTitle());
            pstmt.setString(3, movie.getOverview());

            String posterPath = null;
            if (movie.getPosterUrl() != null) {
                posterPath = movie.getPosterUrl().replace("https://image.tmdb.org/t/p/w500", "");
            }
            pstmt.setString(4, posterPath);

            // YENİ EKLENDİ
            pstmt.setDouble(5, movie.getVoteAverage());
            pstmt.setString(6, movie.getReleaseDate());
            // --- BİTTİ ---

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Film eklendi: " + movie.getTitle());
            } else {
                System.out.println("Film zaten listede: " + movie.getTitle());
            }

        } catch (Exception e) {
            System.err.println("Film ekleme hatası: " + e.getMessage());
        }
    }

    // --- GÜNCELLENDİ: 'vote_average' ve 'release_date' okunuyor ---
    public List<Movie> getAllMoviesFromWatchlist() {
        List<Movie> watchList = new ArrayList<>();
        String sql = "SELECT * FROM watchlist ORDER BY title ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                boolean isWatched = rs.getInt("is_watched") == 1;

                Movie movie = new Movie(
                        rs.getInt("tmdb_id"),
                        rs.getString("title"),
                        rs.getString("overview"),
                        rs.getString("poster_path"),
                        isWatched,
                        rs.getDouble("vote_average"), // YENİ EKLENDİ
                        rs.getString("release_date")   // YENİ EKLENDİ
                );
                watchList.add(movie);
            }
        } catch (Exception e) {
            System.err.println("İzleme listesi okunurken hata: " + e.getMessage());
        }

        return watchList;
    }

    public void removeMovieFromWatchlist(int tmdbId) {
        String sql = "DELETE FROM watchlist WHERE tmdb_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tmdbId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Film silme hatası: " + e.getMessage());
        }
    }

    public void updateMovieWatchedStatus(int tmdbId, boolean isWatched) {
        String sql = "UPDATE watchlist SET is_watched = ? WHERE tmdb_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, isWatched ? 1 : 0);
            pstmt.setInt(2, tmdbId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("İzlendi durumu güncelleme hatası: " + e.getMessage());
        }
    }
}