package model;

import bitzero.server.entities.User;
import bitzero.util.ExtensionUtility;
import bitzero.util.socialcontroller.bean.UserInfo;
import util.Common;
import util.GameConfig;
import util.config.BarrackConfig;
import util.config.TroopBaseConfig;
import util.server.ServerConstant;

import java.awt.*;
import java.util.ArrayList;

public class Barrack extends Building {
    private int lastTrainingTime = 0;
    private ArrayList<TrainingItem> trainingItemList = new ArrayList<>();

    public Barrack(int id, String type, int level, Point position) {
        super(id, type, level, position);
        this.lastTrainingTime = Common.currentTimeInSecond();
    }

    public Barrack(int id, String type, int level, Point position, int lastTrainingTime, ArrayList<TrainingItem> trainingItemList){
        super(id, type, level, position);
        this.lastTrainingTime = lastTrainingTime;
        this.trainingItemList = trainingItemList;
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
        return this.trainingItemList.get(0);
    }

    public boolean removeTroop(String troopCfgId) {

        for (int i = 0; i < this.trainingItemList.size(); i++) {
            if (this.trainingItemList.get(i).cfgId.equals(troopCfgId)) {
                if (this.trainingItemList.get(i).count == 1) {
                    this.trainingItemList.remove(i);
                } else {
                    this.trainingItemList.get(i).count -= 1;
                }

                if (i == 0) {
                    this.lastTrainingTime = Common.currentTimeInSecond();
                }
                return true;

            }
        }

        return true;
    }

    public void cleanTrainingItemList() {
        this.trainingItemList.clear();
    }

    public String removeFirstTroop() {
        String cfgId = this.trainingItemList.get(0).cfgId;
        if (this.trainingItemList.get(0).count == 1) {
            this.trainingItemList.remove(0);
        } else {
            this.trainingItemList.get(0).count -= 1;
        }
        return cfgId;
    }

    public void setLastTrainingTime(int lastTrainingTime) {
        this.lastTrainingTime = lastTrainingTime;
    }

    public void pushNewTrainingItem(TrainingItem trainingItem) {
        for (int i = 0; i < trainingItemList.size(); i++) {
            if (trainingItemList.get(i).cfgId.equals(trainingItem.cfgId)) {
                trainingItemList.get(i).count += trainingItem.count;
                return;
            }
        }

        // update lasttime when train first troop
        if (this.trainingItemList.size() == 0) {
            int curent = Common.currentTimeInSecond();
            this.lastTrainingTime = curent;
        }
        this.trainingItemList.add(trainingItem);

    }

    public int getMaxSpace() {
        return ((BarrackConfig) GameConfig.getInstance().getBuildingConfig(this.getType(), this.getLevel())).queueLength;
    }

    public int getCurrentSpace() {
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
            trainingTime = (int) Math.ceil((double) trainingTime / ServerConstant.TRAIN_TIME_RATE);
            totalTrainingTime += trainingTime;
        }
        int cost = (totalTrainingTime % 60) * 2;
        return cost;
    }


    public ArrayList<TrainingItem> updateTrainingList(int currentSpace, int maxSpace) {
        ArrayList<TrainingItem> doneList = new ArrayList<>();
        int curSpace = currentSpace;
        int timeLeft = Common.currentTimeInSecond() - lastTrainingTime;

        for (TrainingItem trainingItem : this.trainingItemList) {

            TroopBaseConfig troopBaseConfig = GameConfig.getInstance().troopBaseConfig.get(trainingItem.cfgId);
            int housingSpace = troopBaseConfig.housingSpace;
            int trainingTime = (int) Math.ceil((double) GameConfig.getInstance().troopBaseConfig.get(trainingItem.cfgId).trainingTime
                    / ServerConstant.TRAIN_TIME_RATE);

            // nếu không đủ space hoặc time cho 1 con lính tiếp thì ngừng ngay
            if (maxSpace - curSpace < housingSpace || timeLeft < trainingTime) {
                break;
            } else {
                int maxAvailByTime = (int) (double) (timeLeft / trainingTime);
                int maxAvailByCount = trainingItem.count;
                int maxAvailBySpace = (maxSpace - curSpace) / housingSpace;

                int trainCount = Math.min(Math.min(maxAvailByTime, maxAvailByCount), maxAvailBySpace);

                trainingItem.count = Math.max(0, trainingItem.count -trainCount);
                curSpace += trainCount * housingSpace;
                timeLeft -= trainCount * trainingTime;

                doneList.add(new TrainingItem(trainingItem.cfgId, trainCount));

                if(trainingItem.count >0) break;
            }
        }
        this.lastTrainingTime = Common.currentTimeInSecond();
        return  doneList;
    }


    public void updateTrainingList(int uId) {
        PlayerInfo userInfo = null;
        try {
            ArrayList<TrainingItem> doneList = new ArrayList<>();

            if (!this.trainingItemList.isEmpty()) {
                int timeLeft = Common.currentTimeInSecond() - lastTrainingTime;
                while (timeLeft > 0 && !this.trainingItemList.isEmpty()) {
                    User user = ExtensionUtility.globalUserManager.getUserById(uId);
                    userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
                    TrainingItem firstTroop = this.getFirstTrainingItem();
                    TroopBaseConfig troopBaseConfig = GameConfig.getInstance().troopBaseConfig.get(firstTroop.cfgId);

                    synchronized (userInfo.getListTroops()) {
                        // nếu con lính tiếp theo vượt max space
                        if (troopBaseConfig.housingSpace + userInfo.getCurrentTroopSpace() > userInfo.getMaxArmySpace()) {
                            return;
                        }

                        String troopCfgId = this.removeFirstTroop();
                        doneList.add(new TrainingItem(troopCfgId, 1));

                        timeLeft -= (int) Math.ceil(
                                (double) GameConfig.getInstance().troopBaseConfig.get(troopCfgId).trainingTime
                                        / ServerConstant.TRAIN_TIME_RATE);
                        userInfo.pushToListTroop(doneList);
                        doneList.clear();
                    }

                }
                this.lastTrainingTime = Common.currentTimeInSecond();

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
