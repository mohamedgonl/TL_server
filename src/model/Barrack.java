package model;

import util.Common;
import util.GameConfig;
import util.config.BarrackConfig;

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
        for (int i = 0; i < trainingItemList.size(); i++) {
            if(trainingItemList.get(i).cfgId.equals(trainingItem.cfgId)) {
                trainingItemList.get(i).count += trainingItem.count;
                return;
            }
        }

        // if not found or empty list
        this.trainingItemList.add(trainingItem);

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

    public int getMaxSpace() {
        return ((BarrackConfig) GameConfig.getInstance().getBuildingConfig(this.getType(), this.getLevel())).queueLength;
    }

    public int getCurrentSpace (){
        int count = 0;
        for (int i = 0; i < this.trainingItemList.size(); i++) {
            count += this.trainingItemList.get(i).count;
        }
        return count;
    }


}
