module dev.freaks.musicplayer {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.media;
  requires javafx.base;
  requires javafx.web;
  requires javafx.graphics;
  requires mongo.java.driver;
  requires com.jfoenix;
  requires de.jensd.fx.glyphs.commons;
  requires de.jensd.fx.glyphs.fontawesome;


  opens dev.freaks.musicplayer to javafx.fxml;
    exports dev.freaks.musicplayer;
}