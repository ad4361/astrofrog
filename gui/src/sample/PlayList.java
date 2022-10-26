package sample;

public class PlayList {

    private String plname;
    private String username;
    private int totalDuration;
    private int totalSongs;

    public PlayList(String plname, String username, int totalDuration, int totalSongs) {
        this.plname = plname;
        this.username = username;
        this.totalDuration = totalDuration;
        this.totalSongs = totalSongs;
    }

    public String getPlname() {
        return this.plname;
    }

    public String getUsername() {
        return this.username;
    }

    public int getTotalDuration() {
        return this.totalDuration;
    }

    public int getTotalSongs() {
        return this.totalSongs;
    }

}
