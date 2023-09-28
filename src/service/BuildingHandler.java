package service;

import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;
import cmd.CmdDefine;
import cmd.ErrorConst;
import cmd.receive.building.RequestBuyBuilding;
import cmd.receive.building.RequestCancelBuild;
import cmd.send.building.ResponseBuyBuilding;
import cmd.send.building.ResponseCancelBuild;
import model.Building;
import model.PlayerInfo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.BuildingUtils;
import util.Common;
import util.config.BaseBuildingConfig;
import util.config.GameConfig;
import util.config.TownHallConfig;
import util.server.ServerConstant;

import java.lang.reflect.Field;
import java.util.Map;

public class BuildingHandler extends BaseClientRequestHandler {
    public static short BUILDING_MULTI_IDS = 2000;
    private final Logger logger = LoggerFactory.getLogger("BuildingHandler");

    public BuildingHandler() {
        super();
    }

    public void init() {
    }

    public void handleClientRequest(User user, DataCmd dataCmd) {
        logger.info("requestId: " + dataCmd.getId());
        try {
            switch (dataCmd.getId()) {
                case CmdDefine.BUY_BUILDING:
                    RequestBuyBuilding reqData = new RequestBuyBuilding(dataCmd);
                    buyBuilding(user, reqData);
                    break;
                case CmdDefine.CANCEL_BUILD:
                    RequestCancelBuild reqCancelBuild = new RequestCancelBuild(dataCmd);
                    cancelBuild(user, reqCancelBuild);
                    break;
            }
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            logger.warn(ExceptionUtils.getStackTrace(e));
        }

    }

    private void buyBuilding(User user, RequestBuyBuilding reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseBuyBuilding(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseBuyBuilding(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            GameConfig gameConfig = GameConfig.getInstance();
            String type = reqData.getType();

            //get building detail
            BaseBuildingConfig building = BuildingUtils.getBuilding(type, 1);

            //check resources
            if (playerInfo.getGold() < building.gold || playerInfo.getElixir() < building.elixir || playerInfo.getGem() < building.coin) {
                send(new ResponseBuyBuilding(ErrorConst.NOT_ENOUGH_RESOURCES), user);
                return;
            }

            //check builder
            if (building.buildTime > 0 && playerInfo.getAvaiableBuilders() == 0) {
                send(new ResponseBuyBuilding(ErrorConst.NOT_ENOUGH_BUILDER), user);
                return;
            }

            //check town hall lv
            if (playerInfo.getTownHallLv() < building.townHallLevelRequired) {
                send(new ResponseBuyBuilding(ErrorConst.TOWNHALL_LEVEL_TOO_LOW), user);
                return;
            }

            //check amount building
            Map<Integer, BaseBuildingConfig> townHallMap = gameConfig.townHallConfig.get(playerInfo.getTownHallType());
            TownHallConfig townHall = (TownHallConfig) townHallMap.get(playerInfo.getTownHallLv());

            Field buildingTypeField = townHall.getClass().getField(type);
            buildingTypeField.setAccessible(true);

            int maximumAmount = (int) buildingTypeField.get(townHall);
            if (playerInfo.getBuildingAmount().getOrDefault(type, 0) >= maximumAmount) {
                send(new ResponseBuyBuilding(ErrorConst.TOWNHALL_LEVEL_TOO_LOW), user);
                return;
            }

            //check position
            if (!BuildingUtils.checkBuildingPosition(playerInfo.getMap(), reqData.getPosition().x, reqData.getPosition().y, building.width, building.height)) {
                send(new ResponseBuyBuilding(ErrorConst.POSITION_INVALID), user);
                return;
            }

            int newId = playerInfo.getListBuildings().get(playerInfo.getListBuildings().size() - 1).getId() + 1;
            Building newBuilding = new Building(newId, type, 1, reqData.getPosition());
            buildNewBuilding(playerInfo, newBuilding, building);

            playerInfo.saveModel(user.getId());
            send(new ResponseBuyBuilding(ErrorConst.SUCCESS, newBuilding), user);

        } catch (Exception e) {
            System.out.println("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseBuyBuilding(ErrorConst.UNKNOWN), user);
        }

    }

    private void buildNewBuilding(PlayerInfo playerInfo, Building newBuilding, BaseBuildingConfig buildingDetail) {
        //update resource
        playerInfo.useResources(buildingDetail.gold, buildingDetail.elixir, buildingDetail.coin);
        if (buildingDetail.buildTime > 0) {
            playerInfo.useBuilder(1);
            newBuilding.startWorking(Common.currentTimeInSecond(), buildingDetail.buildTime);
        }

        //update mapInfo
        playerInfo.getListBuildings().add(newBuilding);
        playerInfo.getBuildingAmount().put(newBuilding.getType(), playerInfo.getBuildingAmount().getOrDefault(newBuilding.getType(), 0) + 1);

        int[][] map = playerInfo.getMap();

        for (int i = 0; i < buildingDetail.width; i++)
            for (int j = 0; j < buildingDetail.height; j++) {
                int posX = (int) (newBuilding.getPosition().getX() + i);
                int posY = (int) (newBuilding.getPosition().getY() + j);
                if (posX < GameConfig.MAP_WIDTH && posY < GameConfig.MAP_HEIGHT)
                    map[posY][posX] = newBuilding.getId();
            }
        playerInfo.setMap(map);
    }

    private void cancelBuild(User user, RequestCancelBuild reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseCancelBuild(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseCancelBuild(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            GameConfig gameConfig = GameConfig.getInstance();

            //get building by id
            int buildingId = reqData.getBuildingId();
            Building building = null;
            int buildingIdx = 0;
            for (buildingIdx = 0; buildingIdx < playerInfo.getListBuildings().size(); buildingIdx++) {
                Building tmpBuilding = playerInfo.getListBuildings().get(buildingIdx);
                if (tmpBuilding.getId() == buildingId) {
                    building = tmpBuilding;
                    break;
                }
            }

            if (building == null) {
                send(new ResponseCancelBuild(ErrorConst.BUILDING_NOT_EXIST), user);
                return;
            }

            //get building detail
            BaseBuildingConfig buildingDetail = BuildingUtils.getBuilding(building.getType(), building.getLevel());

            //check build done
            int currentTime = Common.currentTimeInSecond();
            if (building.getStatus() != Building.Status.ON_WORK || building.getEndTime() <= currentTime) {
                send(new ResponseCancelBuild(ErrorConst.BUILD_DONE), user);
                return;
            }

            //check resources
            double rewardRate = (double) 1 / 2;
            int rewardGold = (int) (buildingDetail.gold * rewardRate);
            int rewardElixir = (int) (buildingDetail.elixir * rewardRate);
            int rewardGem = (int) (buildingDetail.coin * rewardRate);

            if (playerInfo.getGold() + rewardGold > playerInfo.getGoldCapacity()
                    || playerInfo.getElixir() + rewardElixir > playerInfo.getElixirCapacity()) {
                send(new ResponseCancelBuild(ErrorConst.TOO_MUCH_RESOURCES), user);
                return;
            }

            //success
            cancelBuildSuccess(playerInfo, building, buildingIdx, buildingDetail, rewardGold, rewardElixir, rewardGem);

            playerInfo.saveModel(user.getId());
            send(new ResponseCancelBuild(ErrorConst.SUCCESS, building.getId()), user);

        } catch (Exception e) {
            System.out.println("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseBuyBuilding(ErrorConst.UNKNOWN), user);
        }
    }

    private void cancelBuildSuccess(PlayerInfo playerInfo, Building building, int buildingIdx, BaseBuildingConfig buildingDetail, int rewardGold, int rewardElixir, int rewardGem) {
        //update resources
        playerInfo.addResources(rewardGold, rewardElixir, rewardGem);
        playerInfo.freeBuilder(1);

        //update map
        int[][] map = playerInfo.getMap();

        for (int i = 0; i < buildingDetail.width; i++)
            for (int j = 0; j < buildingDetail.height; j++) {
                int posX = (int) (building.getPosition().getX() + i);
                int posY = (int) (building.getPosition().getY() + j);
                if (posX < GameConfig.MAP_WIDTH && posY < GameConfig.MAP_HEIGHT)
                    map[posY][posX] = 0;
            }
        playerInfo.setMap(map);

        //update buildingAmount
        int amount = playerInfo.getBuildingAmount().getOrDefault(building.getType(), 0);
        if (amount > 0)
            playerInfo.getBuildingAmount().put(building.getType(), amount - 1);

        //update listBuildings
        playerInfo.getListBuildings().remove(buildingIdx);
    }
}
