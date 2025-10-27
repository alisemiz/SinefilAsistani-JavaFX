package org.example.sinefilasistani;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // FXML dosyamızın yolunu belirtiyoruz.
        URL fxmlUrl = getClass().getResource("/org/example/sinefilasistani/view/MainView.fxml");

        if (fxmlUrl == null) {
            System.err.println("FXML dosyası bulunamadı! Lütfen yolu kontrol edin.");
            System.err.println("Aranan yol: /org/example/sinefilasistani/view/MainView.fxml");
            return;
        }

        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root, 900, 700); // Pencere boyutu
        primaryStage.setTitle("Sinefil Asistanı");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}