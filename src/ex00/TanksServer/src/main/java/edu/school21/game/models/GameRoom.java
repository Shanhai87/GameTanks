package edu.school21.game.models;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private final User user1;
    private Integer health1;
    private Integer hits1;
    private Integer misses1;
    private double x1;
    private final double y1;
    private final User user2;
    private Integer health2;
    private Integer hits2;
    private Integer misses2;
    private double x2;
    private final double y2;
    private final List<Shell> shellList1;
    private final List<Shell> shellList2;

    public GameRoom(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.health1 = 100;
        this.health2 = 100;
        this.x1 = 295;
        this.x2 = 295;
        this.y1 = 0;
        this.y2 = 0;
        this.shellList1 = new ArrayList<>();
        this.shellList2 = new ArrayList<>();
        this.hits1 = 0;
        this.hits2 = 0;
        this.misses1 = 0;
        this.misses2 = 0;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public synchronized List<Shell> getShellList1() {
        return shellList1;
    }

    public synchronized List<Shell> getShellList2() {
        return shellList2;
    }

    public Integer getHits1() {
        return hits1;
    }

    public Integer getMisses1() {
        return misses1;
    }

    public Integer getHits2() {
        return hits2;
    }

    public Integer getMisses2() {
        return misses2;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public Integer getHealth1() {
        return health1;
    }

    public void setHealth1(Integer health1) {
        this.health1 = health1;
    }

    public Integer getHealth2() {
        return health2;
    }

    public void setHealth2(Integer health2) {
        this.health2 = health2;
    }

    public void setHits1(Integer hits1) {
        this.hits1 = hits1;
    }

    public void setMisses1(Integer misses1) {
        this.misses1 = misses1;
    }

    public void setHits2(Integer hits2) {
        this.hits2 = hits2;
    }

    public void setMisses2(Integer misses2) {
        this.misses2 = misses2;
    }
}
