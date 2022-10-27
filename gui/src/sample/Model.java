package sample;

import java.sql.Connection;
import java.sql.SQLException;

public class Model {

    public static User self;
    public static String PLname;

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

}
