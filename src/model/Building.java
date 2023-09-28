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

    public void startBuilding(int startTime, int duration) {
        if (duration > 0) {
            this.status = Status.ON_BUILD;
            this.startTime = startTime;
            this.endTime = startTime + duration;
        } else {
            buildSuccess();
        }
    }

    public void startUpgrading(int startTime, int duration) {
        if (duration > 0) {
            this.status = Status.ON_UPGRADE;
            this.startTime = startTime;
            this.endTime = startTime + duration;
        } else {
            upgradeSuccess();
        }
    }

    public void buildSuccess() {
        this.status = Status.DONE;
        this.startTime = 0;
        this.endTime = 0;
    }

    public void upgradeSuccess() {
        this.status = Status.DONE;
        this.startTime = 0;
        this.endTime = 0;
        this.level++;
    }

    public String toString() {
        return String.format("%s|%s|%d|%d", id, type, position.x, position.y);
    }

    public enum Status {
        DONE((short) 0),
        ON_BUILD((short) 1),
        ON_UPGRADE((short) 2);
        private final short value;

        Status(short value) {
            this.value = value;
        }

        public short getValue() {
            return this.value;
        }
    }
}
