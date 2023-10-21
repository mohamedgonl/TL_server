package model;

import bitzero.util.socialcontroller.bean.UserInfo;
import util.Common;
import util.GameConfig;
import util.config.BarrackConfig;
import util.config.TroopBaseConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Barrack extends Building{
    private int lastTrainingTime = 0;
    private ArrayList<TrainingItem> trainingItemList = new ArrayList<>();

    public Barrack(int id, String type, int level, Point position) {
        super(id, type, level, position);
        this.lastTrainingTime = Common.currentTimeInSecond();
    }

    public int getLastTrainingTime() {
        return this.lastTrainingTime;
    }

    public ArrayList<TrainingItem> getTrainingItemList() {
        return this.trainingItemList;
    }



    public void setTrainingItemList() {

    }

    public TrainingItem getFirstTrainingItem() {
        return  this.trainingItemList.get(0);
    }

    public boolean removeTroop (String troopCfgId) {

        for (int i = 0; i < this.trainingItemList.size(); i++) {
            if(this.trainingItemList.get(i).cfgId.equals(troopCfgId)){
                if(this.trainingItemList.get(i).count == 1) {
                    this.trainingItemList.remove(i);
                }
                else {
                    this.trainingItemList.get(i).count -= 1;
                }

                if(i == 0) {
                    this.lastTrainingTime = Common.currentTimeInSecond();
                }
                return true;

            }
        }

        return true;
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
            int curent = Common.currentTimeInSecond();
            this.lastTrainingTime = curent;
        }
        this.trainingItemList.add(trainingItem);

    }

    public int getMaxSpace() {
        return ((BarrackConfig) GameConfig.getInstance().getBuildingConfig(this.getType(), this.getLevel())).queueLength;
    }

    public int getCurrentSpace (){
        int count = 0;
        for (int i = 0; i < this.trainingItemList.size(); i++) {
            TrainingItem item = this.trainingItemList.get(i);
            count += item.count * GameConfig.getInstance().troopBaseConfig.get(item.cfgId).housingSpace;
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

    public ArrayList<TrainingItem> updateTrainingList(PlayerInfo userInfo){
        ArrayList<TrainingItem> doneList = new ArrayList<>();
        if(!this.trainingItemList.isEmpty()){
            int timeLeft =  Common.currentTimeInSecond() - lastTrainingTime;
            while (timeLeft > 0 && !this.trainingItemList.isEmpty()) {
                TrainingItem firstTroop = this.getFirstTrainingItem();
                TroopBaseConfig troopBaseConfig = GameConfig.getInstance().troopBaseConfig.get(firstTroop.cfgId);
                // nếu con lính tiếp theo vượt max space
                if(troopBaseConfig.housingSpace + userInfo.getCurrentSpace() > userInfo.getMaxArmySpace()) {
                    break;
                }
                String troopCfgId = this.removeFirstTroop();
                doneList.add(new TrainingItem(troopCfgId,1));
                timeLeft -= (int) Math.ceil((double) GameConfig.getInstance().troopBaseConfig.get(troopCfgId).trainingTime / 10);
            }
            this.lastTrainingTime = Common.currentTimeInSecond();
        }
        return doneList;
    }


}
