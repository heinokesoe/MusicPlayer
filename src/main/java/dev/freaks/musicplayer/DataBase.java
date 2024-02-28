package dev.freaks.musicplayer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;

public class DataBase {
  private MongoClientURI uri;
  private MongoClient mongoClient;
  private MongoDatabase db;

  public MongoCollection<Document> getUsers() {
    return this.db.getCollection("users");
  }

  public FindIterable<Document> getSongs() {
    return this.db.getCollection("songs").find();
  }

  public ArrayList getSongList() {
    return this.db.getCollection("songs").find().into(new ArrayList());
  }

  public DataBase() {
    this.uri = new MongoClientURI("mongodb://root:byteburst@freaks.dev/?authSource=admin");
    this.mongoClient = new MongoClient(this.uri);
    this.db = mongoClient.getDatabase("MusicPlayer");
  }
}