package testapp.springbackend.entity;

import javax.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue
    private long id;

    private String name;


    public User(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
