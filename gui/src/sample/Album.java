package sample;

import javafx.scene.control.Button;

public class Album {

    private int albumID;
    private String albumname;
    private String artistName;
    private String genre;

    private Button button;

    public Album(int albumID, String albumname, String artistName, String genre, Button button) {
        this.albumID = albumID;
        this.albumname = albumname;
        this.artistName = artistName;
        this.genre = genre;
        this.button = button;
    }

    public int getAlbumID() {
        return this. albumID;
    }

    public String getAlbumname() {
        return this.albumname;
    }

    public String getArtistName() {
        return this.artistName;
    }

    public String getGenre() {
        return this.genre;
    }

    public Button getButton() {
        return this.button;
    }

}
