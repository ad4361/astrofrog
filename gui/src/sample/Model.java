package sample;

import java.sql.Connection;
import java.sql.SQLException;

public class Model {

    public Model() {

    }


    public void login(String username, String password) throws SQLException {
        Connection conn = sample.PostgresSSH.getConnection();
        String query = "select * from \"User\" where username = '"+ username + "'";

    }

    public void signup() {
        //TODO
    }



}
