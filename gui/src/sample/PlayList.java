package sample;

import javafx.scene.control.Button;

public class PlayList {

    private String plname;
    private String username;
    private String totalDuration;
    private int totalSongs;
    private Button button;

    public PlayList(String plname, String username, String totalDuration, int totalSongs, Button button) {
        this.plname = plname;
        this.username = username;
        this.totalDuration = totalDuration;
        this.totalSongs = totalSongs;
        this.button = button;
    }

    public String getPlname() {
        return this.plname;
    }

    public String getUsername() {
        return this.username;
    }

    public String getTotalDuration() {
        return this.totalDuration;
    }

    public int getTotalSongs() {
        return this.totalSongs;
    }

    public Button getButton() {
        return this.button;
    }

}
