package sample;

import java.sql.Date;
import java.time.LocalDateTime;

public class User {

    public String username;
    public String password;
    public String firstname;
    public String lastname;
    public String email;
    public Date dob;
    public Date creationDate;
    public LocalDateTime lastAccessDate;

    public User(String username, String password, String firstname, String lastname,
                String email, Date dob, Date creationDate, LocalDateTime lastAccessDate) {
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.dob = dob;
        this.creationDate = creationDate;
        this.lastAccessDate = lastAccessDate;
    }

}
