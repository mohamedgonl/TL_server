package model;

import java.awt.*;

public class Building {
    public Point position;
    private int id;
    private String type;
    private int level;
    private Status status;
    private int startTime;
    private int endTime;

    public Building(int id, String type, int level, Point position) {
        this.id = id;
        this.type = type;
        this.level = level;
        this.position = position;
        this.status = Status.DONE;
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

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void startWorking(int startTime, int duration) {
        this.status = Status.ON_WORK;
        this.startTime = startTime;
        this.endTime = startTime + duration;
    }

    public void endWorking() {
        this.status = Status.DONE;
        this.startTime = 0;
        this.endTime = 0;
    }

    public String toString() {
        return String.format("%s|%s|%d|%d", id, type, position.x, position.y);
    }

    public enum Status {
        DONE((short) 0),
        ON_WORK((short) 1);
        private final short value;

        Status(short value) {
            this.value = value;
        }

        public short getValue() {
            return this.value;
        }
    }
}
