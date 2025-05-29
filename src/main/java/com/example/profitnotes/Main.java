package com.example.profitnotes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/example/profitnotes/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("/com/example/profitnotes/styles.css").toExternalForm());

        stage.setTitle("Прибуткові нотатки");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo.PNG")));
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}