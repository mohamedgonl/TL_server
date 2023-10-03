package model;

import util.Common;

import java.awt.*;

public class CollectorBuilding extends Building{
    private int lastCollectTime = 0;

    public CollectorBuilding(int id, String type, int level, Point position) {
        super(id, type, level, position);
        this.lastCollectTime = Common.currentTimeInSecond();
    }

    public int getLastCollectTime() {
        return lastCollectTime;
    }

    public void setLastCollectTime(int lastCollectTime) {
        this.lastCollectTime = lastCollectTime;
    }

    public void collect() {
        this.lastCollectTime = Common.currentTimeInSecond();
    }

    @Override
    public void buildSuccess() {
        super.buildSuccess();
        lastCollectTime = Common.currentTimeInSecond();
    }

    @Override
    public void upgradeSuccess() {
        super.upgradeSuccess();
        lastCollectTime = Common.currentTimeInSecond();
    }

    @Override
    public void cancelUpgradeSuccess() {
        super.cancelUpgradeSuccess();
        lastCollectTime = Common.currentTimeInSecond();
    }
}
