package model;

import util.Common;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class Barrack extends Building{
    private int lastTrainingTime = 0;
    private ArrayList<TrainingItem> trainingItemList = new ArrayList<>();

    public Barrack(int id, String type, int level, Point position) {
        super(id, type, level, position);
        this.lastTrainingTime = Common.currentTimeInSecond();
    }

    public int getLastTrainingTime() {
        return lastTrainingTime;
    }

    public void setLastTrainingTime(int lastTrainingTime) {
        this.lastTrainingTime = lastTrainingTime;
    }


    public void pushNewTrainingItem(TrainingItem trainingItem) {
        trainingItemList.add(trainingItem);
    }

    @Override
    public void buildSuccess() {
        super.buildSuccess();
//        lastCollectTime = Common.currentTimeInSecond();
    }

    @Override
    public void upgradeSuccess() {
        super.upgradeSuccess();
//        lastCollectTime = Common.currentTimeInSecond();
    }

    @Override
    public void cancelUpgradeSuccess() {
        super.cancelUpgradeSuccess();
//        lastCollectTime = Common.currentTimeInSecond();
    }
}
