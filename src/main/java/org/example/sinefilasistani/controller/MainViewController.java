package org.example.sinefilasistani.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.sinefilasistani.model.Movie;
import org.example.sinefilasistani.service.DatabaseService;
import org.example.sinefilasistani.service.TMDbService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainViewController {

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab watchlistTab;
    @FXML
    private TilePane discoverTilePane;
    @FXML
    private TilePane watchlistTilePane;

    private TMDbService tmdbService;
    private DatabaseService databaseService;

    @FXML
    public void initialize() {
        System.out.println("MainViewController başarıyla başlatıldı.");

        // --- GÜNCELLENDİ: Singleton olarak çağır ---
        this.tmdbService = TMDbService.getInstance();
        this.databaseService = DatabaseService.getInstance();
        // --- BİTTİ ---

        searchButton.setOnAction(event -> handleSearch());
        loadPopularMovies();

        watchlistTab.setOnSelectionChanged(event -> {
            if (watchlistTab.isSelected()) {
                loadWatchlist();
            }
        });
    }

    // ... (loadWatchlist, loadPopularMovies, createMovieCard, handleSearch metotları HİÇ DEĞİŞMEDİ) ...

    private void loadWatchlist() {
        List<Movie> movieList = databaseService.getAllMoviesFromWatchlist();

        Platform.runLater(() -> {
            watchlistTilePane.getChildren().clear();

            if (movieList.isEmpty()) {
                Label message = new Label("İzleme listeniz boş.");
                message.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                watchlistTilePane.getChildren().add(message);
            } else {
                for (Movie movie : movieList) {
                    VBox movieCard = createMovieCard(movie, "watchlist");
                    watchlistTilePane.getChildren().add(movieCard);
                }
            }
        });
    }

    private void loadPopularMovies() {
        tmdbService.getPopularMovies()
                .thenAccept(movieList -> {
                    Platform.runLater(() -> {
                        discoverTilePane.getChildren().clear();
                        for (Movie movie : movieList) {
                            VBox movieCard = createMovieCard(movie, "discover");
                            discoverTilePane.getChildren().add(movieCard);
                        }
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("Film yüklenirken hata oluştu: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }

    private VBox createMovieCard(Movie movie, String context) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #444; -fx-padding: 10; -fx-background-radius: 8;");
        card.setPrefWidth(160);
        card.setMinWidth(160);
        card.setMaxWidth(160);
        card.setPrefHeight(280);
        card.setMinHeight(280);

        ImageView posterView = new ImageView();
        posterView.setFitWidth(150);
        posterView.setFitHeight(225);

        String posterUrl = movie.getPosterUrl();

        if (posterUrl != null) {
            Image posterImage = new Image(posterUrl, 150, 225, true, true);
            posterView.setImage(posterImage);
        } else {
            posterView.setStyle("-fx-background-color: #555555; -fx-background-radius: 5;");
        }

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setPrefWidth(150);
        titleLabel.setPrefHeight(45);

        card.getChildren().addAll(posterView, titleLabel);

        if (context.equals("watchlist")) {

            Button watchButton = new Button();
            watchButton.setPrefWidth(150);

            if (movie.isWatched()) {
                watchButton.setText("İzlenmedi Olarak İşaretle");
                watchButton.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: white; -fx-font-size: 11px;");
                card.setOpacity(0.7);
            } else {
                watchButton.setText("İzlendi Olarak İşaretle");
                watchButton.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white; -fx-font-size: 11px;");
                card.setOpacity(1.0);
            }

            watchButton.setOnAction(event -> {
                handleUpdateWatchedStatus(movie, !movie.isWatched());
                event.consume();
            });

            Button removeButton = new Button("Listeden Kaldır");
            removeButton.setStyle("-fx-background-color: #c9302c; -fx-text-fill: white; -fx-font-size: 11px;");
            removeButton.setPrefWidth(150);

            removeButton.setOnAction(event -> {
                handleRemoveFromWatchlist(movie);
                event.consume();
            });

            card.getChildren().addAll(watchButton, removeButton);
            card.setPrefHeight(360);
            card.setMinHeight(360);
        }

        card.setOnMouseClicked(event -> {
            if (!event.isConsumed()) {
                showMovieDetails(movie);
            }
        });

        return card;
    }

    private void handleSearch() {
        String searchTerm = searchField.getText();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            System.out.println("Aranacak terim: " + searchTerm);
            mainTabPane.getSelectionModel().select(0);

            tmdbService.searchMovies(searchTerm)
                    .thenAccept(movieList -> {
                        Platform.runLater(() -> {
                            discoverTilePane.getChildren().clear();

                            if (movieList.isEmpty()) {
                                Label message = new Label("'" + searchTerm + "' için sonuç bulunamadı.");
                                message.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                                discoverTilePane.getChildren().add(message);
                            } else {
                                for (Movie movie : movieList) {
                                    VBox movieCard = createMovieCard(movie, "discover");
                                    discoverTilePane.getChildren().add(movieCard);
                                }
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        System.err.println("Arama yapılırken hata oluştu: " + ex.getMessage());
                        ex.printStackTrace();
                        return null;
                    });

        } else {
            System.out.println("Lütfen bir film adı girin.");
            loadPopularMovies();
        }
    }

    private void showMovieDetails(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/sinefilasistani/view/MovieDetailView.fxml"));
            Parent root = loader.load();

            MovieDetailController controller = loader.getController();
            controller.initData(movie); // Bu metot da güncellenecek

            Stage detailStage = new Stage();
            detailStage.setTitle(movie.getTitle() + " - Detaylar");
            detailStage.setScene(new Scene(root));
            detailStage.initModality(Modality.APPLICATION_MODAL);

            detailStage.showAndWait();

            if (watchlistTab.isSelected()) {
                loadWatchlist();
            }

        } catch (IOException e) {
            System.err.println("Detay penceresi yüklenirken hata oluştu!");
            e.printStackTrace();
        }
    }

    private void handleRemoveFromWatchlist(Movie movie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Silme Onayı");
        alert.setHeaderText("Filmi Listeden Kaldır");
        alert.setContentText("'" + movie.getTitle() + "' filmini izleme listenizden kalıcı olarak kaldırmak istediğinize emin misiniz?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            databaseService.removeMovieFromWatchlist(movie.getId());
            loadWatchlist();
        }
    }

    private void handleUpdateWatchedStatus(Movie movie, boolean newStatus) {
        databaseService.updateMovieWatchedStatus(movie.getId(), newStatus);
        loadWatchlist();
    }
}