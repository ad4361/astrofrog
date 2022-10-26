package sample;

import javafx.scene.control.Button;

import java.sql.Date;

public class Song {

    private int songID;
    private String title;
    private Date releasedate;
    private int length;

    // comes from other relations
    private String artistName;
    private String albumName;
    private int listenCount;

    private Button button;

    public Song(int songID, String title, Date releasedate, int length, String artistName,
                String albumName, int listenCount) {
        this.songID = songID;
        this.title = title;
        this.releasedate = releasedate;
        this.length = length;
        this.artistName = artistName;
        this.albumName = albumName; this.listenCount = listenCount;
    }

    public int getSongID() {
        return this.songID;
    }

    public String getTitle() {
        return this.title;
    }

    public Date getReleasedate() {
        return this.releasedate;
    }

    public int getLength() {
        return this.length;
    }

    public String getArtistName() {
        return this.artistName;
    }

    public String getAlbumName() {
        return this.albumName;
    }

    public int getListenCount() {
        return this.listenCount;
    }
}
