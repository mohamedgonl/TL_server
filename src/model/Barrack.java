package model;

import util.Common;
import util.GameConfig;

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

    public ArrayList<TrainingItem> getTrainingItemList() {
        return trainingItemList;
    }

    public void setTrainingItemList() {

    }

    public void cleanTrainingItemList(){
        this.trainingItemList.clear();
    }

    public String removeFirstTroop() {
        String cfgId = this.trainingItemList.get(0).cfgId;
        if(this.trainingItemList.get(0).count == 1) {
            this.trainingItemList.remove(0);
        }
        else {
            this.trainingItemList.get(0).count -= 1;
        }
        return cfgId;
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

        // update lasttime when train first troop
        if(this.trainingItemList.size() == 0) {
            this.lastTrainingTime = Common.currentTimeInSecond();
        }

        this.trainingItemList.add(trainingItem);


    }

    public int getMaxSpace() {
        return GameConfig.getInstance().barrackConfig.get(this.getType()).get(this.getLevel()).queueLength;
    }

    public int getCurrentSpace (){
        int count = 0;
        for (int i = 0; i < this.trainingItemList.size(); i++) {
            count += this.trainingItemList.get(i).count;
        }
        return count;
    }

    public int getDoneNowCost() {
        int totalTrainingTime = 0;
        for (int i = 0; i < this.trainingItemList.size(); i++) {
            String troopCfgId = this.trainingItemList.get(i).cfgId;
            int trainingTime = GameConfig.getInstance().troopBaseConfig.get(troopCfgId).trainingTime;
            trainingTime = (int) Math.ceil(trainingTime / 10);
            totalTrainingTime += trainingTime;
        }
        int cost =  (totalTrainingTime % 60) * 2;
        return cost;
    }

    public ArrayList<TrainingItem> updateTrainingList(){
        if(this.trainingItemList.size() > 0){
            int timeLeft =  Common.currentTimeInSecond() - lastTrainingTime;
            while (timeLeft > 0) {
                String troopCfgId = this.removeFirstTroop();
                timeLeft -= GameConfig.getInstance().troopBaseConfig.get(troopCfgId).trainingTime;
            }
        }
            return this.trainingItemList;
    }


}