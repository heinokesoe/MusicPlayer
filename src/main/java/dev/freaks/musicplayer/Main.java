package dev.freaks.musicplayer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import java.io.IOException;

public class Main extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Main.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    stage.setTitle("Main");
    stage.setScene(scene);
    stage.setTitle("Music Player");
    stage.show();
  }
  public static void main(String[] args) {
    launch();
  }
}