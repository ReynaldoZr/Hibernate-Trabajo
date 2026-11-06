package com.desarrollo.proyectohibernateg3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BibliotecaApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                BibliotecaApplication.class.getResource("libros-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("CRUD");
        stage.setScene(scene);
        stage.show();
    }
}
