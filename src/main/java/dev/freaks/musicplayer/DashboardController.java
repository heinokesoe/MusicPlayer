package dev.freaks.musicplayer;

import com.jfoenix.controls.JFXSlider;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.bson.Document;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class DashboardController implements Initializable {
  @FXML
  private AreaChart<String, Number> areaChart;
  @FXML
  private ListView<String> songListView;
  @FXML
  private Label songTitle;
  @FXML
  private JFXSlider slider;
  @FXML
  private JFXSlider volumeSlider;
  @FXML
  private Label currentTime;
  @FXML
  private Label totalTime;
  @FXML
  private ImageView playButton;
  @FXML
  private ImageView mute;
  @FXML
  private ImageView repeatButton;
  @FXML
  private ImageView shuffleButton;
  private ArrayList songs = MainController.songList;
  private String[] songTitles = new String[songs.size()];
  private String[] songURLs = new String[songs.size()];
  private int[] songDurations = new int[songs.size()];
  private MediaPlayer mediaPlayer;
  private int index = 0;
  private Timer timer;
  private TimerTask timerTask;
  private boolean isPlaying, isMute;
  private Media media;
  private String result,filePath,fileName,urlString;
  private String tmpdir = System.getProperty("java.io.tmpdir");
  private File file;
  private boolean isRepeat = false;
  private boolean isShuffle = false;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    createArrays();
    songListView.getItems().addAll(songTitles);
    slider.setOnMouseReleased(event -> mediaPlayer.seek(Duration.seconds(slider.getValue())));
    slider.valueProperty().addListener((observable, oldValue, newValue) -> {
      int seconds = (int) slider.getValue() % 60;
      int minutes = (int) slider.getValue() / 60;
      String time = String.format("%02d:%02d", minutes, seconds);
      currentTime.setText(time);
    });
    slider.valueFactoryProperty().setValue(param -> new StringBinding() {
      @Override
      protected String computeValue() {
        return "*";
      }
    });
    volumeSlider.setOnMouseReleased(event -> mediaPlayer.setVolume(volumeSlider.getValue() / 100));
    volumeSlider.setOnMouseDragged(event -> mediaPlayer.setVolume(volumeSlider.getValue() / 100));
    songListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
        if (mediaPlayer != null) {
          mediaPlayer.stop();
          sliderClock(false);
        }
        index = songListView.getSelectionModel().getSelectedIndex();
        prepareMedia();
      }
    });
  }
  private void createArrays() {
    for (int i=0; i<=songs.size()-1; i++) {
      Document song = (Document) songs.get(i);
      songTitles[i] = i+1 + ". " + song.getString("title");
      songURLs[i] = song.getString("song_url");
      songDurations[i] = song.getInteger("duration");
    }
  }
  private Image getUiImage(String name) {
    return new Image(getClass().getResource("images") + "/" + name + ".png");
  }
  private void sliderClock(boolean state) {
    if (state) {
      totalTime.setText(String.format("%02d:%02d", (int) slider.getMax() / 60, (int) slider.getMax() % 60));
      timer = new Timer();
      timerTask = new TimerTask() {
        @Override
        public void run() {
          javafx.application.Platform.runLater(() -> {
            slider.setValue(slider.getValue() + 1);
            int seconds = (int) slider.getValue() % 60;
            int minutes = (int) slider.getValue() / 60;
            String time = String.format("%02d:%02d", minutes, seconds);
            currentTime.setText(time);
          });
        }
      };
      timer.scheduleAtFixedRate(timerTask, 0, 1000);
    } else {
      if (timer != null) {
        timerTask.cancel();
        timer.cancel();
        timer.purge();
      }
    }
  }
  private void visualize() {
    XYChart.Series<String, Number> series1 = new XYChart.Series<>();
    XYChart.Data[] series1Data = new XYChart.Data[60];
    for (int i = 0; i < series1Data.length; i++) {
      series1Data[i] = new XYChart.Data<>(Integer.toString(i + 1), 25);
      series1.getData().add(series1Data[i]);
    }
    mediaPlayer.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
      for (int i = 0; i < series1Data.length; i++) {
        float tempValue = magnitudes[i] - mediaPlayer.getAudioSpectrumThreshold();
        if (tempValue < 10) tempValue *= 2;
        if (tempValue < 20) tempValue *= 1.5f;
        series1Data[(i + 25) % 60].setYValue(tempValue);
      }
    });
    areaChart.getData().clear();
    areaChart.getData().add(series1);
  }
  @FXML
  public void mute(MouseEvent event) {
    isMute = !isMute;
    mediaPlayer.setMute(isMute);
    mute.setImage(getUiImage(isMute ? "volumeOffWhite" : "volumeOnWhite"));
  }
  @FXML
  public void play(MouseEvent event) {
    if (!isPlaying) {
      playButton.setImage(getUiImage("playWhiteCircle"));
      mediaPlayer.pause();
      sliderClock(false);
    } else {
      playButton.setImage(getUiImage("pauseWhite"));
      mediaPlayer.play();
      sliderClock(true);
    }
    isPlaying = !isPlaying;
  }
  @FXML
  public void nextSong(MouseEvent event) {
    mediaPlayer.stop();
    if (!isRepeat) {
      if (isShuffle)
        index = new Random().nextInt(songs.size());
      else
        ++index;
      if (index == songs.size())
        index = 0;
    }
    prepareMedia();
    mediaPlayer.setMute(isMute);
  }
  @FXML
  private void previousSong(MouseEvent event) {
    mediaPlayer.stop();
    --index;
    if (index < 0) index = songs.size() - 1;
    prepareMedia();
    mediaPlayer.setOnReady(this::playMusic);
  }
  @FXML
  public void repeatButton(MouseEvent event) {
    isRepeat =! isRepeat;
    repeatButton.setImage(getUiImage(isRepeat ? "repeatOnWhite" : "repeatOffWhite"));
  }
  @FXML
  public void shuffleButton(MouseEvent event) {
    isShuffle =! isShuffle;
    shuffleButton.setImage(getUiImage(isShuffle ? "shuffleOnWhite" : "shuffleOffWhite"));
  }
  private void prepareMedia() {
    songTitle.setText(songTitles[index]);
    playButton.setImage(getUiImage("pauseWhite"));
    fileName = tmpdir + index + ".mp3";
    filePath = "file://" + fileName;
    urlString = songURLs[index];
    file = new File(fileName);
    System.out.print("\nChecking if the song exist in the temp dir: ");
    if (!(file.isFile())) {
      System.out.print("No\n");
      System.out.println("Downloading the song from the server");
      try {
        result = "file://" + downloadSong(urlString,index+".mp3");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      System.out.print("Yes\n");
      System.out.println("Using from the temp dir");
      result = filePath;
    }
    media = new Media(result);
    settingUpMediaPlayer(media);
  }
  private void playMusic() {
    slider.setValue(0);
    slider.setMax(songDurations[index]);
    sliderClock(false);
    areaChart.setVisible(true);
    mediaPlayer.play();
    mediaPlayer.setMute(isMute);
    sliderClock(true);
  }
  private MediaPlayer settingUpMediaPlayer(Media media) {
    mediaPlayer = new MediaPlayer(media);
    visualize();
    System.out.println("Now playing: " + songTitles[index]);
    mediaPlayer.setVolume(volumeSlider.getValue() / 100);
    mediaPlayer.setOnReady(this::playMusic);
    mediaPlayer.setOnEndOfMedia(() -> nextSong(null));
    return mediaPlayer;
  }
  private static String downloadSong(String url, String localFilename) throws IOException {
    InputStream is = null;
    FileOutputStream fos = null;
    String tempDir = System.getProperty("java.io.tmpdir");
    String outputPath = tempDir + localFilename;
    try {
      URLConnection urlConn = new URL(url).openConnection();
      is = urlConn.getInputStream();
      fos = new FileOutputStream(outputPath);
      byte[] buffer = new byte[4096];
      int length;
      while ((length = is.read(buffer)) > 0) {
        fos.write(buffer, 0, length);
      }
      return outputPath;
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } finally {
        if (fos != null) {
          fos.close();
        }
      }
    }
  }
}