package service;

import bitzero.server.BitZeroServer;
import bitzero.server.core.BZEventParam;
import bitzero.server.core.BZEventType;
import bitzero.server.core.IBZEvent;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;
import cmd.CmdDefine;
import cmd.ErrorConst;
import cmd.receive.user.RequestCheatResource;
import cmd.send.user.*;
import event.eventType.DemoEventParam;
import event.eventType.DemoEventType;
import extension.FresherExtension;
import model.Barrack;
import model.Building;
import model.PlayerInfo;
import model.TrainingItem;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Common;
import util.server.ServerConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserHandler extends BaseClientRequestHandler {
    public static short USER_MULTI_IDS = 1000;
    private final Logger logger = LoggerFactory.getLogger("UserHandler");

    public UserHandler() {
        super();
    }

    public void init() {
        getExtension().addEventListener(BZEventType.USER_DISCONNECT, this);
        getExtension().addEventListener(BZEventType.USER_RECONNECTION_SUCCESS, this);

        /**
         *  register new event, so the core will dispatch event type to this class
         */
        getExtension().addEventListener(DemoEventType.CHANGE_NAME, this);
    }

    private FresherExtension getExtension() {
        return (FresherExtension) getParentExtension();
    }

    public void handleServerEvent(IBZEvent ibzevent) {

        if (ibzevent.getType() == BZEventType.USER_DISCONNECT)
            this.userDisconnect((User) ibzevent.getParameter(BZEventParam.USER));
        else if (ibzevent.getType() == DemoEventType.CHANGE_NAME)
            this.userChangeName((User) ibzevent.getParameter(DemoEventParam.USER), (String) ibzevent.getParameter(DemoEventParam.NAME));
    }

    public void handleClientRequest(User user, DataCmd dataCmd) {
        logger.info("requestId: " + dataCmd.getId());
        try {
            switch (dataCmd.getId()) {
                case CmdDefine.GET_USER_INFO:
                    getUserInfo(user);
                    break;
                case CmdDefine.GET_MAP_INFO:
                    getMapInfo(user);
                    break;
                case CmdDefine.GET_ARMY_INFO:
                    getArmyInfo(user);
                    break;
                case CmdDefine.GET_TIME_SERVER:
                    getTimeServer(user);
                    break;
                case CmdDefine.CHEAT_RESOURCE:
                    RequestCheatResource reqCheatResource = new RequestCheatResource(dataCmd);
                    cheatResource(user, reqCheatResource);
                    break;
            }
        } catch (Exception e) {
            logger.warn("USERHANDLER EXCEPTION " + e.getMessage());
            logger.warn(ExceptionUtils.getStackTrace(e));
        }

    }

    private void getUserInfo(User user) {
        try {
            //get user from cache
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (userInfo == null) {
                send(new ResponseGetUserInfo(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            send(new ResponseGetUserInfo(ErrorConst.SUCCESS, userInfo), user);
        } catch (Exception e) {
            send(new ResponseGetUserInfo(ErrorConst.UNKNOWN), user);
        }

    }

    private void getMapInfo(User user) {
        try {
            //get user from cache
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (userInfo == null) {
                send(new ResponseGetMapInfo(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            send(new ResponseGetMapInfo(ErrorConst.SUCCESS, userInfo), user);
        } catch (Exception e) {
            send(new ResponseGetMapInfo(ErrorConst.UNKNOWN), user);
        }
    }

    private void getArmyInfo(User user) {
        try {
            //get user from cache
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (userInfo == null) {
                send(new ResponseGetArmyInfo(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            ArrayList<Barrack> barracks = this.getBarracksList(userInfo.getListBuildings());

//                barracks.parallelStream().forEach(barrack -> {
//                   barrack.updateTrainingList(userInfo.getId());
//                });


            while (userInfo.getCurrentTroopSpace() < userInfo.getMaxArmySpace()) {
                Map<Integer, Integer> nextTimes = new HashMap<>();
                for (Barrack barrack :
                        barracks) {
                    int nextTime =  barrack.getNextDoneTime();
                    if(nextTime == -1) {
                        nextTimes.remove(barrack.getId());
                    }
                    else {

                    nextTimes.putIfAbsent(barrack.getId(), nextTime);
                    }
                }
                if(nextTimes.isEmpty()) break;

                Map.Entry<Integer, Integer> minNextTime = getMinValueEntry(nextTimes);
                if(minNextTime.getValue() <= Common.currentTimeInSecond()) {
                    Barrack barrack =  getBarrackById(barracks, minNextTime.getKey());
                    if(barrack.getFirstSpaceIncrease() + userInfo.getCurrentTroopSpace() <= userInfo.getMaxArmySpace()){
                        ArrayList<TrainingItem> doneList = new ArrayList<>();
                        String troopCfgId = barrack.removeFirstTroop();
                        doneList.add(new TrainingItem(troopCfgId, 1));
                        barrack.setLastTrainingTime(minNextTime.getValue());
                        userInfo.pushToListTroop(doneList);
                    }
                    else {
                        break;
                    }
                }
                else {
                    break;
                }
            }
            userInfo.saveModel(user.getId());
            send(new ResponseGetArmyInfo(ErrorConst.SUCCESS, userInfo.getListTroops()), user);
        } catch (Exception e) {
            System.out.println("HANDLE GET ARMY ERROR " + e.getMessage());
            e.printStackTrace();
            send(new ResponseGetArmyInfo(ErrorConst.UNKNOWN), user);
        }
    }

    private static Map.Entry<Integer, Integer> getMinValueEntry(Map<Integer, Integer> map) {
        Map.Entry<Integer, Integer> minEntry = null;

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (minEntry == null || entry.getValue() < minEntry.getValue()) {
                minEntry = entry;
            }
        }

        return minEntry;
    }

    private static Barrack getBarrackById (ArrayList<Barrack> barracks, int id) {
        for (Barrack barrack :
                barracks) {
            if(barrack.getId() == id) return barrack;
        }
        return null;
    }


    private ArrayList<Barrack> getBarracksList(ArrayList<Building> buildings) {
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

    private void getTimeServer(User user) {
        try {
            send(new ResponseGetTimeServer(ErrorConst.SUCCESS, Common.currentTimeInSecond()), user);
        } catch (Exception e) {
            send(new ResponseGetTimeServer(ErrorConst.UNKNOWN), user);
        }
    }

    private void cheatResource(User user, RequestCheatResource reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseCheatResource(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (userInfo == null) {
                send(new ResponseCheatResource(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            if (reqData.getGold() >= 0)
                userInfo.setGold(Math.min(reqData.getGold(), userInfo.getGoldCapacity()));
            if (reqData.getElixir() >= 0)
                userInfo.setElixir(Math.min(reqData.getElixir(), userInfo.getElixirCapacity()));
            if (reqData.getGem() >= 0)
                userInfo.setGem(reqData.getGem());

            userInfo.saveModel(user.getId());
            send(new ResponseCheatResource(ErrorConst.SUCCESS, userInfo.getGold(), userInfo.getElixir(), userInfo.getGem()), user);
        } catch (Exception e) {
            send(new ResponseCheatResource(ErrorConst.UNKNOWN), user);
        }
    }

    private void userDisconnect(User user) {
        // log user disconnect
    }

    private void userChangeName(User user, String name) {
        List<User> allUser = BitZeroServer.getInstance().getUserManager().getAllUsers();
        for (User aUser : allUser) {
            // notify user's change
        }
    }
}
