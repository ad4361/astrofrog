package sample;

import java.sql.Connection;
import java.sql.SQLException;

public class Model {

    public static User self;

    public Model() {
    }

    public static void setSelf(User user) {
        self = user;
    }

    public static void eraseSelf() {
        self = null;
    }

}
