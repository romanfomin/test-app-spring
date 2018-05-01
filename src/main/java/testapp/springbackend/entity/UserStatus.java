package testapp.springbackend.entity;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Date;

@Entity
public class UserStatus {

    @Id
    private long id;
    private Status status;
    private Timestamp date;

    public UserStatus() {
    }

    public UserStatus(long id, Status status, Timestamp date) {
        this.id = id;
        this.status = status;
        this.date = date;
    }

    public UserStatus(long id, Status status) {

        this.id = id;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}
