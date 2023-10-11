package service;

import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;

import cmd.CmdDefine;

import cmd.ErrorConst;
import cmd.receive.user.RequestCancleTrain;
import cmd.receive.user.RequestGetTrainTroopList;
import cmd.receive.user.RequestTrainingCreate;


import cmd.receive.user.RequestTrainingSuccess;
import cmd.send.building.ResponseCancleTrain;
import cmd.send.building.ResponseGetTrainTroopList;
import cmd.send.building.ResponseTrainingSuccess;
import cmd.send.user.ResponseGetUserInfo;

import cmd.send.user.ResponseTrainingCreate;
import model.Barrack;
import model.Building;
import model.PlayerInfo;

import model.TrainingItem;
import org.apache.commons.lang.exception.ExceptionUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Common;
import util.GameConfig;
import util.config.TroopBaseConfig;
import util.config.TroopConfig;
import util.server.ServerConstant;

import java.util.ArrayList;
import java.util.List;

public class TroopHandler extends BaseClientRequestHandler {
    public static short TROOP_MULTI_IDS = 5000;
    private final Logger logger = LoggerFactory.getLogger("UserHandler");

    public TroopHandler() {
        super();
    }

    public void init() {

    }

    public void handleClientRequest(User user, DataCmd dataCmd) {
        logger.info("requestId: " + dataCmd.getId());
        try {
            switch (dataCmd.getId()) {
                case CmdDefine.TRAIN_TROOP_CREATE:{
                    RequestTrainingCreate reqInfo = new RequestTrainingCreate(dataCmd);
                    trainTroopCreate(user, reqInfo);
                    break;
                }
                case CmdDefine.TRAIN_TROOP_SUCCESS: {
                    RequestTrainingSuccess reqInfo = new RequestTrainingSuccess(dataCmd);
                    trainTroopSuccess(user, reqInfo);
                    break;

                }
                case CmdDefine.GET_TRAINING_LIST: {
                    RequestGetTrainTroopList reqInfo = new RequestGetTrainTroopList(dataCmd);
                    handleGetTrainTroopList(user, reqInfo);
                    break;
                }
                case CmdDefine.CANCLE_TRAIN_TROOP:{
                    RequestCancleTrain reqInfo = new RequestCancleTrain(dataCmd);
                    handleCancleTrainTroop(user, reqInfo);
                    break;
                }
            }
        } catch (Exception e) {
            logger.warn("USERHANDLER EXCEPTION " + e.getMessage());
            logger.warn(ExceptionUtils.getStackTrace(e));
        }

    }

    private void trainTroopCreate(User user, RequestTrainingCreate reqInfo) {
        System.out.println("HANDLE CREATE TROOP");
        try {
            int troopLevel = 1; // sau đổi thành lấy từ building LAB
            //get user from cache
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
            ArrayList<Building> userBuilding = userInfo.getListBuildings();
            Barrack currentBarrack = this.getBarrackById(userBuilding, reqInfo.getBarrackIdId());
            TroopConfig troopConfig = GameConfig.getInstance().troopConfig.get(reqInfo.getTroopCfgId()).get(troopLevel) ;
            TroopBaseConfig troopBaseConfig = GameConfig.getInstance().troopBaseConfig.get(reqInfo.getTroopCfgId());

            if(currentBarrack == null) {
                send(new ResponseTrainingCreate(ErrorConst.BARRACK_NOT_FOUND), user);
                return;
            }

            // check xem nhà lính có đủ cấp để luyện không
            if(troopBaseConfig.barracksLevelRequired > currentBarrack.getLevel()) {
                send(new ResponseTrainingCreate(ErrorConst.TROOP_NOT_UNLOCKED, currentBarrack.getId()), user);
                return;
            }
            // check slot còn trong nhà lính
            if(currentBarrack.getCurrentSpace() + troopBaseConfig.housingSpace > currentBarrack.getMaxSpace()) {
                send(new ResponseTrainingCreate(ErrorConst.BARRACK_FULL, currentBarrack.getId()), user);
                return;
            }
            // check tài nguyên
              // chỉ kiểm tra elixir, ko kiểm tra dark - elixir
            if(userInfo.getElixir() < troopConfig.trainingElixir) {
                send(new ResponseTrainingCreate(ErrorConst.RESOURCE_NOT_ENOUGH), user);
                return;
            }

            // pass hết check thì mới cập nhập tài nguyên, space nhà lính, lasttimetrain
            userInfo.setElixir(userInfo.getElixir() - troopConfig.trainingElixir);
            TrainingItem trainingItem = new TrainingItem(reqInfo.getTroopCfgId(), reqInfo.getTroopCount());
            currentBarrack.pushNewTrainingItem(trainingItem);
            userInfo.saveModel(user.getId());

            send(new ResponseTrainingCreate(ErrorConst.SUCCESS, trainingItem, reqInfo.getBarrackIdId(), currentBarrack.getLastTrainingTime()), user);
        } catch (Exception e) {
            System.out.println("HANDLE TRAIN TROOP CREATE ERROR" + e);
            send(new ResponseGetUserInfo(ErrorConst.UNKNOWN), user);
        }
    }

    private void trainTroopSuccess(User user, RequestTrainingSuccess reqInfo) {
        Barrack currentBarrack;
        PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
        ArrayList<Building> userBuilding = userInfo.getListBuildings();
        currentBarrack = this.getBarrackById(userBuilding, reqInfo.getBarrackId());
        try {

            if (currentBarrack == null) {
                send(new ResponseTrainingSuccess(ErrorConst.BARRACK_NOT_FOUND, currentBarrack.getId()), user);
                return;
            }

            // check done now
            if (reqInfo.checkIsDoneNow()) {
                int doneNowCost = currentBarrack.getDoneNowCost();
                if (doneNowCost > userInfo.getGem()) {
                    send(new ResponseTrainingSuccess(ErrorConst.NOT_ENOUGH_DONE_TRAIN_NOW_COST, currentBarrack.getId()), user);
                    return;
                } else {
                    userInfo.setGem(userInfo.getGem() - doneNowCost);
                    //get all troop in barrack and mark as done
                    ArrayList<TrainingItem> troopItems = currentBarrack.getTrainingItemList();

                    if (userInfo.getCurrentSpace() + currentBarrack.getCurrentSpace() > userInfo.getMaxArmySpace()) {
                        send(new ResponseTrainingSuccess(ErrorConst.ARMY_MAX_SPACE, currentBarrack.getId()), user);
                        return;
                    }

                    userInfo.pushToListTroop(troopItems);
                    currentBarrack.cleanTrainingItemList();
                    int lastTrainTime = Common.currentTimeInSecond();
                    currentBarrack.setLastTrainingTime(lastTrainTime);
                    userInfo.saveModel(user.getId());
                    send(new ResponseTrainingSuccess(ErrorConst.SUCCESS, currentBarrack.getId(), 1, "", lastTrainTime, userInfo.getGem()), user);
                    return;
                }

            } else {
                if (currentBarrack.getTrainingItemList().isEmpty()) {
                    send(new ResponseTrainingSuccess(ErrorConst.BARRACK_TRAIN_LIST_EMPTY, currentBarrack.getId()), user);
                    return;
                }

                String firstTroopCfgId = currentBarrack.getTrainingItemList().get(0).cfgId;
                int trainingTime = (int) Math.ceil((double) GameConfig.getInstance().troopBaseConfig.get(firstTroopCfgId).trainingTime / 10);
                if (Common.currentTimeInSecond() - currentBarrack.getLastTrainingTime() >= trainingTime) {

                    String cfgId = currentBarrack.removeFirstTroop();
                    int lastTrainingTime = Common.currentTimeInSecond();
                    currentBarrack.setLastTrainingTime(lastTrainingTime);
                    ArrayList<TrainingItem> doneList = new ArrayList<>();
                    doneList.add(new TrainingItem(cfgId, 1));
                    userInfo.pushToListTroop(doneList);
                    userInfo.saveModel(user.getId());
                    send(new ResponseTrainingSuccess(ErrorConst.SUCCESS, currentBarrack.getId(), 0, firstTroopCfgId, lastTrainingTime, userInfo.getGem()), user);
                    return;
                } else {
                    send(new ResponseTrainingSuccess(ErrorConst.BARRACK_TRAIN_NOT_DONE, currentBarrack.getId()), user);
                    return;
                }
            }

        } catch (Exception e) {
            System.out.println("HANDLE TRAIN TROOP SUCCESS ERROR:     ");
            send(new ResponseTrainingSuccess(ErrorConst.UNKNOWN, currentBarrack.getId()),user);
        }
    }

    private void handleGetTrainTroopList(User user, RequestGetTrainTroopList reqInfo){
        System.out.println("HANDLE GET TRAINING LIST with barrackId::::" + reqInfo.getBarrackId());
        try {
            // tìm barrack theo đúng id
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
            ArrayList<Building> userBuilding = userInfo.getListBuildings();
            Barrack currentBarrack = this.getBarrackById(userBuilding, reqInfo.getBarrackId());

            if(currentBarrack == null) {
                send(new ResponseGetTrainTroopList(ErrorConst.BARRACK_NOT_FOUND),user);
                return;
            }
            // cập nhập lai danh sách luyện
            ArrayList<TrainingItem> trainingList = currentBarrack.updateTrainingList();

            // lính dc train lưu vào player info;
            userInfo.pushToListTroop(trainingList);

            // lưu thông tin
            userInfo.saveModel(user.getId());

            send(new ResponseGetTrainTroopList(ErrorConst.SUCCESS, currentBarrack.getId(), trainingList, currentBarrack.getLastTrainingTime()), user);
            return;
        }
        catch (Exception e) {
            System.out.println("HANDLE GET TRAINING TROOP LIST ERROR:     ");
            send(new ResponseGetTrainTroopList(ErrorConst.UNKNOWN),user);
        }
    }

    private void handleCancleTrainTroop(User  user, RequestCancleTrain reqInfo){
        System.out.println("HANDLE CANCLE TRAINTROOP "+ reqInfo.getBarrackId() + "   " + reqInfo.getTroopCfgId());
        try {
            // hiện để level = 1 do chưa có lab
            int troopLevel = 1;

            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
            ArrayList<Building> userBuilding = userInfo.getListBuildings();
            Barrack currentBarrack = this.getBarrackById(userBuilding, reqInfo.getBarrackId());
            TroopConfig troopConfig = GameConfig.getInstance().troopConfig.get(reqInfo.getTroopCfgId()).get(troopLevel) ;

            if(currentBarrack == null) {
                send(new ResponseCancleTrain(ErrorConst.BARRACK_NOT_FOUND), user);
            }

            boolean found = currentBarrack.removeTroop(reqInfo.getTroopCfgId());

            if(!found) {
                send(new ResponseCancleTrain(ErrorConst.TROOP_NOT_FOUND),user);
            }

            userInfo.addResources(0,troopConfig.trainingElixir/2,0);

            send(new ResponseCancleTrain(ErrorConst.SUCCESS, currentBarrack.getId(), reqInfo.getTroopCfgId(),
                    currentBarrack.getLastTrainingTime(), troopConfig.trainingElixir/2), user);
            return;

        }catch (Exception e){
            System.out.println("HANDLE CANLE TRAIN TROOP ERROR");
            send(new ResponseCancleTrain(ErrorConst.UNKNOWN), user);
        }
    }

    private ArrayList<Barrack> getBarracksList (ArrayList<Building> buildings) {
        ArrayList<Barrack> barrackList = new ArrayList<>();
        for (int i = 0; i < buildings.size(); i++) {
            Building building = buildings.get(i);
            if (building.getType().startsWith("BAR")) {
                    Barrack barrack = (Barrack) building;
                    barrackList.add(barrack);
            }
        }

        return barrackList;
    }

    private Barrack getBarrackById (ArrayList<Building> buildings, int id){
        for (int i = 0; i < buildings.size(); i++) {
            if(buildings.get(i).getId() == id) {
                return (Barrack) buildings.get(i);
            }
        }
        return null;
    }


}
