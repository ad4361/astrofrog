package sample;

import com.jcraft.jsch.*;

import java.sql.*;
import java.util.Properties;

public class PostgresSSH {

    /*private static Connection getConnection() throws SQLException {

        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", Credentials.username);
        connectionProps.put("password", Credentials.password);

        conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/p32001_20",
                connectionProps);

        System.out.println("Connected to database");
        return conn;
    }*/


    public static Connection getConnection() throws SQLException {

        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user = Credentials.username; //change to your username
        String password = Credentials.password; //change to your password
        String databaseName = "p32001_20"; //change to your database name

        String driverName = "org.postgresql.Driver";
        Connection conn = null;
        Session session = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "localhost", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://localhost:"+ assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");

            // Do something with the database....
            return conn;
            //conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns results from select query.
     * @param query
     * @return
     * @throws SQLException
     */
    public static ResultSet executeSelect(String query) throws SQLException {

        Connection conn = getConnection();
        assert conn != null;
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);

        return rs;
    }


}