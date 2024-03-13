package edu.school21.game.models;

public class User {
    private Long id;
    private String name;
    private String password;
    private String status;
    private Integer hits;
    private Integer misses;

    public User() {}
    public User(String name) {
        this.id = null;
        this.name = name;
        this.password = null;
        this.status = "OFFLINE";
        this.hits = 0;
        this.misses = 0;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }


    public String getStatus() {
        return status;
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public void setMisses(int misses) {
        this.misses = misses;
    }
}
