package sample;

import java.util.ArrayList;

public class Model {

    public static User self;
    public static String PLname;
    public static ArrayList<String> top3Artists;

    public static void setSelf(User user) {
        self = user;
    }

    public static void eraseSelf() {
        self = null;
    }

    public static void setPlname(String plname) {
        PLname = plname;
    }

    public static void erasePLname() {
        PLname = null;
    }

    public static void setTop3Artists(ArrayList<String> top3ArtistsList) {
        top3Artists = top3ArtistsList;
    }
}
