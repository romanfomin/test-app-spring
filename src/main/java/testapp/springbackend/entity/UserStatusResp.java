package testapp.springbackend.entity;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class UserStatusResp {

    @Id
    private long id;
    private Status status;
    private Status prevStatus;
    private Date date;

    public UserStatusResp() {
    }

    public UserStatusResp(long id, Status status, Status prevStatus, Date date) {
        this.id = id;
        this.status = status;
        this.prevStatus = prevStatus;
        this.date = date;
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

    public Status getPrevStatus() {
        return prevStatus;
    }

    public void setPrevStatus(Status prevStatus) {
        this.prevStatus = prevStatus;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
