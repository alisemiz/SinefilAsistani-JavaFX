package org.example.sinefilasistani.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.sinefilasistani.model.Actor;
import org.example.sinefilasistani.model.Movie;
import org.example.sinefilasistani.service.TMDbService;

import java.io.IOException;

public class ActorDetailController {

    @FXML
    private ImageView actorImageView;
    @FXML
    private Label actorNameLabel;
    @FXML
    private Label biographyLabel;
    @FXML
    private TilePane filmographyTilePane;

    private TMDbService tmdbService;

    // Bu metot, FXML yüklendikten hemen sonra otomatik çalışır
    @FXML
    public void initialize() {
        this.tmdbService = TMDbService.getInstance();
    }

    /**
     * Bu metot, MovieDetailController tarafından çağrılır.
     * Tıklanan oyuncunun ID'sini alır ve bu pencereyi doldurur.
     */
    public void initData(int actorId) {
        // 1. Oyuncu Detaylarını (Biyografi, Fotoğraf) Yükle
        loadActorDetails(actorId);

        // 2. Oyuncunun Filmlerini (Filmografi) Yükle
        loadActorFilmography(actorId);
    }

    // Oyuncu detaylarını (isim, bio, fotoğraf) çeker ve arayüzü günceller
    private void loadActorDetails(int actorId) {
        tmdbService.getActorDetails(actorId)
                .thenAccept(actor -> {
                    Platform.runLater(() -> {
                        actorNameLabel.setText(actor.getName());

                        if (actor.getBiography() != null && !actor.getBiography().isEmpty()) {
                            biographyLabel.setText(actor.getBiography());
                        } else {
                            biographyLabel.setText("Biyografi bilgisi bulunamadı.");
                        }

                        String profileUrl = actor.getProfileUrl();
                        if (profileUrl != null) {
                            actorImageView.setImage(new Image(profileUrl, true));
                        } else {
                            actorImageView.setStyle("-fx-background-color: #555;");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> biographyLabel.setText("Oyuncu detayları yüklenirken hata oluştu."));
                    ex.printStackTrace();
                    return null;
                });
    }

    // Oyuncunun oynadığı filmleri çeker ve arayüzü (TilePane) günceller
    private void loadActorFilmography(int actorId) {
        tmdbService.getActorFilmography(actorId)
                .thenAccept(movieList -> {
                    Platform.runLater(() -> {
                        filmographyTilePane.getChildren().clear();
                        for (Movie movie : movieList) {
                            // Oynadığı filmler için de aynı film kartı tasarımını yeniden kullanıyoruz!
                            VBox movieCard = createMovieCard(movie);
                            filmographyTilePane.getChildren().add(movieCard);
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    /**
     * MainViewController'dan KOPYALANAN yardımcı metot.
     * Filmografi listesindeki filmler için kart oluşturur.
     */
    private VBox createMovieCard(Movie movie) {
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
            posterView.setImage(new Image(posterUrl, 150, 225, true, true));
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

        // Karta tıklandığında, YENİ BİR FİLM DETAY PENCERESİ aç
        card.setOnMouseClicked(event -> {
            // MainViewController'daki metodu burada yeniden kullanıyoruz
            showMovieDetails(movie);
        });

        return card;
    }

    /**
     * MainViewController'dan KOPYALANAN yardımcı metot.
     * Filmografi listesinden bir filme tıklandığında film detaylarını açar.
     */
    private void showMovieDetails(Movie movie) {
        try {
            // Dikkat: Bu metot artık MovieDetailView'ı yüklüyor
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/sinefilasistani/view/MovieDetailView.fxml"));
            Parent root = loader.load();

            MovieDetailController controller = loader.getController();
            controller.initData(movie); // ve onun controller'ına veriyi yolluyor

            Stage detailStage = new Stage();
            detailStage.setTitle(movie.getTitle() + " - Detaylar");
            detailStage.setScene(new Scene(root));
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Film detay penceresi (Oyuncu sayfasından) yüklenirken hata oluştu!");
            e.printStackTrace();
        }
    }
}