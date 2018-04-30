package testapp.springbackend.entity;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class UserStatus {

    @Id
    private long id;
    private Status status;
    private Date date;

    public UserStatus() {
    }

    public UserStatus(long id, Status status, Date date) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
