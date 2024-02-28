package dev.freaks.musicplayer;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import java.io.IOException;
import java.util.ArrayList;

public class MainController {
  @FXML
  private TextField userField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private AnchorPane mainPane;
  private MongoCollection<Document> users;
  public static ArrayList songList;
  public static FindIterable<Document> songs;
  private Document result;
  private Parent root;
  private Stage stage;
  private Scene scene;
  private Alert alert;

  public MainController() {
    DataBase db = new DataBase();
    this.users = db.getUsers();
    this.songList = db.getSongList();
    this.songs = db.getSongs();
  }
  public void login(ActionEvent event) throws IOException {
    result = users.find(eq("username", userField.getText())).first();
    if (result == null) {
      showErrorAlert("User does not exist");
    } else if (userField.getText().isEmpty() || passwordField.getText().isEmpty()) {
      showErrorAlert("Please fill both username and password");
    } else if (passwordField.getText().equals(result.getString("password"))) {
      openDashboard();
    } else {
      showErrorAlert("Wrong password");
    }
  }
  public void signup(ActionEvent event) throws IOException {
    result = users.find(eq("username",userField.getText())).first();
    if (!(result == null)) {
      showErrorAlert("User already exist");
    } else if (userField.getText().isEmpty() || passwordField.getText().isEmpty()) {
      showErrorAlert("Please fill both username and password");
    } else {
      users.insertOne(new Document("username", userField.getText()).append("password", passwordField.getText()));
      openDashboard();
    }
  }
  public void showErrorAlert(String str) {
    stage = (Stage) mainPane.getScene().getWindow();
    alert = new Alert(Alert.AlertType.ERROR);
    alert.initModality(Modality.APPLICATION_MODAL);
    alert.initOwner(stage);
    alert.getDialogPane().setHeaderText("Try Again");
    alert.getDialogPane().setContentText(str);
    alert.showAndWait();
  }
  public void openDashboard() throws IOException {
    root = new FXMLLoader(getClass().getResource("Dashboard.fxml")).load();
    stage = (Stage) mainPane.getScene().getWindow();
    scene = new Scene(root);
    stage.setScene(scene);
    stage.setTitle("Music Player");
    stage.show();
  }
}
