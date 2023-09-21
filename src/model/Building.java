package model;

import java.awt.*;

public class Building {
    public Point position;
    private int id;
    private String type;
    private int level;

    public Building(int id, String type, int level, Point position) {
        this.id = id;
        this.type = type;
        this.level = level;
        this.position = position;
    }

    public Building(int id, String type, Point position) {
        this.id = id;
        this.type = type;
        this.level = 1;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public String toString() {
        return String.format("%s|%s|%d|%d", id, type, position.x, position.y);
    }
}
