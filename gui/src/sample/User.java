package sample;

import javafx.scene.control.Button;

import java.sql.Date;
import java.time.LocalDateTime;

public class User {

    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private Date dob;
    private Date creationDate;
    private LocalDateTime lastAccessDate;
    private Button button;

    public User(String username, String firstname, String lastname,
                String email, Date dob, Date creationDate, LocalDateTime lastAccessDate) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.dob = dob;
        this.creationDate = creationDate;
        this.lastAccessDate = lastAccessDate;
    }

    public User(String username, String email, Button button) {
        this.username = username;
        this.email = email;
        this.button = button;
    }

    public String getUsername() {
        return this.username;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getEmail() {
        return this.email;
    }

    public Date getDob() {
        return this.dob;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public LocalDateTime getLastAccessDate() {
        return this.lastAccessDate;
    }

    public Button getButton() {
        return this.button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

}
