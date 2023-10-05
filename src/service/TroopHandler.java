package service;

import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;

import cmd.CmdDefine;

import cmd.ErrorConst;
import cmd.receive.user.RequestTrainingCreate;


import cmd.send.user.ResponseGetUserInfo;

import cmd.send.user.ResponseTrainingCreate;
import model.Barrack;
import model.Building;
import model.PlayerInfo;

import model.TrainingItem;
import org.apache.commons.lang.exception.ExceptionUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.GameConfig;
import util.config.TroopBaseConfig;
import util.config.TroopConfig;
import util.server.ServerConstant;

import java.util.ArrayList;

public class TroopHandler extends BaseClientRequestHandler {
    public static short TROOP_MULTI_IDS = 5000;
    private final Logger logger = LoggerFactory.getLogger("UserHandler");

    public TroopHandler() {
        super();
    }

    public void init() {

    }

    public void handleClientRequest(User user, DataCmd dataCmd) {
        System.out.println("requestId: " + dataCmd.getId());
        logger.info("requestId: " + dataCmd.getId());
        try {
            switch (dataCmd.getId()) {
                case CmdDefine.TRAIN_TROOP_CREATE:
                    RequestTrainingCreate reqInfo = new RequestTrainingCreate(dataCmd);
                    trainTroopCreate(user, reqInfo);
                    break;
//                case CmdDefine.GET_MAP_INFO:
//                    getMapInfo(user);
//                    break;
            }
        } catch (Exception e) {
            logger.warn("USERHANDLER EXCEPTION " + e.getMessage());
            logger.warn(ExceptionUtils.getStackTrace(e));
        }

    }

    private void trainTroopCreate(User user, RequestTrainingCreate reqInfo) {

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
                send(new ResponseTrainingCreate(ErrorConst.TROOP_NOT_UNLOCKED), user);
                return;
            }
            // check slot còn trong nhà lính
            if(currentBarrack.getCurrentSpace() + troopBaseConfig.housingSpace > currentBarrack.getMaxSpace()) {
                send(new ResponseTrainingCreate(ErrorConst.BARRACK_FULL), user);
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

            send(new ResponseTrainingCreate(ErrorConst.SUCCESS), user);
        } catch (Exception e) {
            send(new ResponseGetUserInfo(ErrorConst.UNKNOWN), user);
        }
    }

    private void onTrainSuccess () {

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
