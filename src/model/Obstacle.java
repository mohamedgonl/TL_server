package model;

import java.awt.*;

public class Obstacle extends Building {
    private boolean isRemove = false;

    public Obstacle(int id, String type, int level, Point position) {
        super(id, type, level, position);
        isRemove = false;
    }

    public boolean isRemove() {
        return isRemove;
    }

    public void setRemove(boolean remove) {
        isRemove = remove;
    }

    @Override
    public void startBuilding(int startTime, int duration) {
        if (duration > 0) {
            setStatus(Status.ON_BUILD);
            setStartTime(startTime);
            setEndTime(startTime + duration);
        } else buildSuccess();
    }

    @Override
    public void buildSuccess() {
        setStatus(Status.DONE);
        isRemove = true;
        setStartTime(0);
        setEndTime(0);
    }
}
