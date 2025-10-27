package org.example.sinefilasistani.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // YENİ EKLENDİ
import javafx.geometry.Pos;
import javafx.scene.Parent; // YENİ EKLENDİ
import javafx.scene.Scene; // YENİ EKLENDİ
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality; // YENİ EKLENDİ
import javafx.stage.Stage; // YENİ EKLENDİ
import org.example.sinefilasistani.model.CastMember;
import org.example.sinefilasistani.model.Movie;
import org.example.sinefilasistani.service.DatabaseService;
import org.example.sinefilasistani.service.TMDbService;

import java.io.IOException; // YENİ EKLENDİ
import java.util.List;

public class MovieDetailController {

    @FXML
    private ImageView posterImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label overviewLabel;
    @FXML
    private Button addWatchlistButton;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label releaseDateLabel;
    @FXML
    private HBox castHBox;

    private Movie currentMovie;

    public void initData(Movie movie) {
        this.currentMovie = movie;

        titleLabel.setText(currentMovie.getTitle());
        overviewLabel.setText(currentMovie.getOverview());

        if (currentMovie.getVoteAverage() > 0) {
            String formattedRating = String.format("⭐ %.1f / 10", currentMovie.getVoteAverage());
            ratingLabel.setText(formattedRating);
        } else {
            ratingLabel.setText("Puan Yok");
        }

        if (currentMovie.getReleaseDate() != null && !currentMovie.getReleaseDate().isEmpty()) {
            String year = currentMovie.getReleaseDate().substring(0, 4);
            releaseDateLabel.setText("Yayın Yılı: " + year);
        } else {
            releaseDateLabel.setText("Yıl Bilgisi Yok");
        }

        String posterUrl = currentMovie.getPosterUrl();
        if (posterUrl != null) {
            Image posterImage = new Image(posterUrl, true);
            posterImageView.setImage(posterImage);
        } else {
            posterImageView.setStyle("-fx-background-color: #555555; -fx-background-radius: 5;");
            posterImageView.setFitHeight(400);
            posterImageView.setFitWidth(267);
        }

        checkIfMovieInWatchlist();
        addWatchlistButton.setOnAction(event -> handleAddButton());

        loadMovieCredits();
    }

    private void loadMovieCredits() {
        TMDbService.getInstance().getMovieCredits(currentMovie.getId())
                .thenAccept(castList -> {
                    Platform.runLater(() -> {
                        castHBox.getChildren().clear();
                        int count = 0;
                        for (CastMember member : castList) {
                            if (count >= 10) break;
                            VBox castCard = createCastMemberCard(member);
                            castHBox.getChildren().add(castCard);
                            count++;
                        }
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("Oyuncu kadrosu yüklenirken hata: " + ex.getMessage());
                    return null;
                });
    }

    private VBox createCastMemberCard(CastMember member) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(100);

        ImageView profileView = new ImageView();
        profileView.setFitWidth(80);
        profileView.setFitHeight(120);

        String profileUrl = member.getProfileUrl();
        if (profileUrl != null) {
            Image profileImage = new Image(profileUrl, 80, 120, true, true);
            profileView.setImage(profileImage);
        } else {
            profileView.setStyle("-fx-background-color: #555; -fx-background-radius: 5;");
        }

        Label nameLabel = new Label(member.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        nameLabel.setPrefWidth(100);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setWrapText(true);

        Label characterLabel = new Label(member.getCharacter());
        characterLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 10px;");
        characterLabel.setPrefWidth(100);
        characterLabel.setTextAlignment(TextAlignment.CENTER);
        characterLabel.setWrapText(true);

        card.getChildren().addAll(profileView, nameLabel, characterLabel);

        // --- GÜNCELLENDİ: Oyuncu kartına tıklandığında YENİ PENCEREYİ AÇ ---
        card.setOnMouseClicked(event -> {
            // System.out.println("Oyuncuya tıklandı: ID " + member.getId() + ", Adı: " + member.getName());
            showActorDetails(member.getId()); // YENİ METODU ÇAĞIR
        });

        return card;
    }

    // --- YENİ EKLENDİ: Yeni Oyuncu Detay penceresini açan metot ---
    /**
     * Tıklanan oyuncu için yeni bir detay penceresi açar.
     * @param actorId Detayları gösterilecek oyuncunun ID'si
     */
    private void showActorDetails(int actorId) {
        try {
            // 1. Yeni ActorDetailView.fxml dosyasını yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/sinefilasistani/view/ActorDetailView.fxml"));
            Parent root = loader.load();

            // 2. Yeni FXML'in Controller'ını al (ActorDetailController)
            ActorDetailController controller = loader.getController();

            // 3. Controller'a tıklanan oyuncunun ID'sini gönder
            controller.initData(actorId);

            // 4. Yeni bir pencere (Stage) oluştur ve göster
            Stage actorStage = new Stage();
            actorStage.setTitle("Oyuncu Detayları");
            actorStage.setScene(new Scene(root));
            actorStage.initModality(Modality.APPLICATION_MODAL);
            actorStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Oyuncu detay penceresi yüklenirken hata oluştu!");
            e.printStackTrace();
        }
    }
    // --- YENİ EKLENEN BÖLÜM BİTTİ ---


    private void checkIfMovieInWatchlist() {
        List<Movie> watchlist = DatabaseService.getInstance().getAllMoviesFromWatchlist();
        for (Movie movieInList : watchlist) {
            if (movieInList.getId() == currentMovie.getId()) {
                setAddButtonToAdded();
                break;
            }
        }
    }

    private void setAddButtonToAdded() {
        addWatchlistButton.setText("Listeye Eklendi!");
        addWatchlistButton.setDisable(true);
        addWatchlistButton.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void handleAddButton() {
        if (currentMovie != null) {
            DatabaseService.getInstance().addMovieToWatchlist(currentMovie);
            setAddButtonToAdded();
        }
    }
}