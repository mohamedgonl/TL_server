package service;

import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;
import cmd.CmdDefine;
import cmd.ErrorConst;
import cmd.receive.building.*;
import cmd.send.building.*;
import model.Building;
import model.CollectorBuilding;
import model.PlayerInfo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.BuildingUtils;
import util.Common;
import util.config.BaseBuildingConfig;
import util.config.GameConfig;
import util.config.ResourceConfig;
import util.config.TownHallConfig;
import util.server.ServerConstant;

import java.lang.reflect.Field;

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
                case CmdDefine.BUILD_SUCCESS:
                    RequestBuildSuccess reqBuildSuccess = new RequestBuildSuccess(dataCmd);
                    buildSuccess(user, reqBuildSuccess);
                    break;
                case CmdDefine.UPGRADE_BUILDING:
                    RequestUpgradeBuilding reqUpgradeBuilding = new RequestUpgradeBuilding(dataCmd);
                    upgradeBuilding(user, reqUpgradeBuilding);
                    break;
                case CmdDefine.CANCEL_UPGRADE:
                    RequestCancelUpgrade reqCancelUpgrade = new RequestCancelUpgrade(dataCmd);
                    cancelUpgrade(user, reqCancelUpgrade);
                    break;
                case CmdDefine.UPGRADE_SUCCESS:
                    RequestUpgradeSuccess reqUpgradeSuccess = new RequestUpgradeSuccess(dataCmd);
                    upgradeSuccess(user, reqUpgradeSuccess);
                    break;
                case CmdDefine.COLLECT_RESOURCE:
                    RequestCollectResource reqCollectResource = new RequestCollectResource(dataCmd);
                    collectResource(user, reqCollectResource);
                    break;
                case CmdDefine.MOVE_BUILDING:
                    RequestMoveBuilding reqMoveBuilding = new RequestMoveBuilding(dataCmd);
                    moveBuilding(user, reqMoveBuilding);
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

            String type = reqData.getType();

            //get building detail
            BaseBuildingConfig building = BuildingUtils.getBuildingConfig(type, 1);

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
            TownHallConfig townHall = (TownHallConfig) BuildingUtils.getBuildingConfig(playerInfo.getTownHallType(), playerInfo.getTownHallLv());

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
            Building newBuilding = BuildingUtils.getBuilding(newId,type,  1, reqData.getPosition());

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
        newBuilding.startBuilding(Common.currentTimeInSecond(), buildingDetail.buildTime);
        if (newBuilding.getStatus() == Building.Status.ON_BUILD) {
            playerInfo.useBuilder(1);
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

            //get building by id
            int buildingId = reqData.getBuildingId();
            Building building = BuildingUtils.getBuildingInListById(playerInfo.getListBuildings(), buildingId);

            if (building == null) {
                send(new ResponseCancelBuild(ErrorConst.BUILDING_NOT_EXIST), user);
                return;
            }

            //get building detail
            BaseBuildingConfig buildingDetail = BuildingUtils.getBuildingConfig(building.getType(), building.getLevel());

            //check build done
            int currentTime = Common.currentTimeInSecond();
            if (building.getStatus() != Building.Status.ON_BUILD || building.getEndTime() <= currentTime) {
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
            cancelBuildSuccess(playerInfo, building, buildingDetail, rewardGold, rewardElixir, rewardGem);

            playerInfo.saveModel(user.getId());
            send(new ResponseCancelBuild(ErrorConst.SUCCESS, building.getId()), user);

        } catch (Exception e) {
            System.out.println("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseCancelBuild(ErrorConst.UNKNOWN), user);
        }
    }

    private void cancelBuildSuccess(PlayerInfo playerInfo, Building building, BaseBuildingConfig buildingDetail, int rewardGold, int rewardElixir, int rewardGem) {
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
        playerInfo.getListBuildings().remove(building);
    }

    private void buildSuccess(User user, RequestBuildSuccess reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseBuildSuccess(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseBuildSuccess(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            //get building by id
            int buildingId = reqData.getBuildingId();
            Building building = BuildingUtils.getBuildingInListById(playerInfo.getListBuildings(), buildingId);

            if (building == null) {
                send(new ResponseBuildSuccess(ErrorConst.BUILDING_NOT_EXIST), user);
                return;
            }

            //check if build already done
            if (building.getStatus() != Building.Status.ON_BUILD) {
                send(new ResponseBuildSuccess(ErrorConst.BUILD_DONE), user);
                return;
            }

            //check if build not done
            if (building.getEndTime() > Common.currentTimeInSecond()) {
                send(new ResponseBuildSuccess(ErrorConst.BUILD_NOT_DONE), user);
                return;
            }

            //success
            building.buildSuccess();
            playerInfo.freeBuilder(1);

            playerInfo.saveModel(user.getId());
            send(new ResponseBuildSuccess(ErrorConst.SUCCESS, building.getId()), user);

        } catch (Exception e) {
            System.out.println("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseBuildSuccess(ErrorConst.UNKNOWN), user);
        }
    }

    private void upgradeBuilding(User user, RequestUpgradeBuilding reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseUpgradeBuilding(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseUpgradeBuilding(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            //get building by id
            int buildingId = reqData.getBuildingId();
            Building building = BuildingUtils.getBuildingInListById(playerInfo.getListBuildings(), buildingId);


            if (building == null) {
                send(new ResponseUpgradeBuilding(ErrorConst.BUILDING_NOT_EXIST), user);
                return;
            }

            if (building.getStatus() != Building.Status.DONE) {
                send(new ResponseUpgradeBuilding(ErrorConst.BUILDING_ON_WORKING), user);
                return;
            }

            String type = building.getType();
            BaseBuildingConfig buildingDetail = BuildingUtils.getBuildingConfig(type, building.getLevel() + 1);

            //check resources
            if (playerInfo.getGold() < buildingDetail.gold || playerInfo.getElixir() < buildingDetail.elixir || playerInfo.getGem() < buildingDetail.coin) {
                send(new ResponseUpgradeBuilding(ErrorConst.NOT_ENOUGH_RESOURCES), user);
                return;
            }

            //check builder
            if (buildingDetail.buildTime > 0 && playerInfo.getAvaiableBuilders() == 0) {
                send(new ResponseUpgradeBuilding(ErrorConst.NOT_ENOUGH_BUILDER), user);
                return;
            }

            //check town hall lv
            if (playerInfo.getTownHallLv() < buildingDetail.townHallLevelRequired) {
                send(new ResponseUpgradeBuilding(ErrorConst.TOWNHALL_LEVEL_TOO_LOW), user);
                return;
            }

            //success
            playerInfo.useResources(buildingDetail.gold, buildingDetail.elixir, buildingDetail.coin);
            building.startUpgrading(Common.currentTimeInSecond(), buildingDetail.buildTime);
            if (building.getStatus() == Building.Status.ON_UPGRADE)
                playerInfo.useBuilder(1);

            playerInfo.saveModel(user.getId());
            send(new ResponseUpgradeBuilding(ErrorConst.SUCCESS, building), user);
        } catch (Exception e) {
            System.out.println("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseUpgradeBuilding(ErrorConst.UNKNOWN), user);
        }
    }

    private void cancelUpgrade(User user, RequestCancelUpgrade reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseCancelUpgrade(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseCancelUpgrade(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            //get building by id
            int buildingId = reqData.getBuildingId();
            Building building = BuildingUtils.getBuildingInListById(playerInfo.getListBuildings(), buildingId);

            if (building == null) {
                send(new ResponseCancelUpgrade(ErrorConst.BUILDING_NOT_EXIST), user);
                return;
            }

            //get building detail
            BaseBuildingConfig buildingDetail = BuildingUtils.getBuildingConfig(building.getType(), building.getLevel() + 1);

            //check upgrade done
            int currentTime = Common.currentTimeInSecond();
            if (building.getStatus() != Building.Status.ON_UPGRADE || building.getEndTime() <= currentTime) {
                send(new ResponseCancelUpgrade(ErrorConst.BUILD_DONE), user);
                return;
            }

            //check resources
            double rewardRate = (double) 1 / 2;
            int rewardGold = (int) (buildingDetail.gold * rewardRate);
            int rewardElixir = (int) (buildingDetail.elixir * rewardRate);
            int rewardGem = (int) (buildingDetail.coin * rewardRate);

            if (playerInfo.getGold() + rewardGold > playerInfo.getGoldCapacity()
                    || playerInfo.getElixir() + rewardElixir > playerInfo.getElixirCapacity()) {
                send(new ResponseCancelUpgrade(ErrorConst.TOO_MUCH_RESOURCES), user);
                return;
            }

            //success
            playerInfo.addResources(rewardGold, rewardElixir, rewardGem);
            playerInfo.freeBuilder(1);

            building.cancelUpgradeSuccess();

            playerInfo.saveModel(user.getId());
            send(new ResponseCancelUpgrade(ErrorConst.SUCCESS, building.getId()), user);
        } catch (Exception e) {
            System.out.println("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseCancelUpgrade(ErrorConst.UNKNOWN), user);
        }
    }

    private void upgradeSuccess(User user, RequestUpgradeSuccess reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseUpgradeSuccess(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseUpgradeSuccess(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            //get building by id
            int buildingId = reqData.getBuildingId();
            Building building = BuildingUtils.getBuildingInListById(playerInfo.getListBuildings(), buildingId);

            if (building == null) {
                send(new ResponseUpgradeSuccess(ErrorConst.BUILDING_NOT_EXIST), user);
                return;
            }

            //check if upgrade already done
            if (building.getStatus() != Building.Status.ON_UPGRADE) {
                send(new ResponseUpgradeSuccess(ErrorConst.BUILD_DONE), user);
                return;
            }

            //check if upgrade not done
            if (building.getEndTime() > Common.currentTimeInSecond()) {
                send(new ResponseUpgradeSuccess(ErrorConst.BUILD_NOT_DONE), user);
                return;
            }

            //success
            building.upgradeSuccess();
            playerInfo.freeBuilder(1);

            playerInfo.saveModel(user.getId());
            send(new ResponseUpgradeSuccess(ErrorConst.SUCCESS, building.getId()), user);

        } catch (Exception e) {
            System.out.println("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseUpgradeSuccess(ErrorConst.UNKNOWN), user);
        }
    }

    private void collectResource(User user, RequestCollectResource reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseCollectResource(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseCollectResource(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            //get building by id
            int buildingId = reqData.getBuildingId();
            Building building = BuildingUtils.getBuildingInListById(playerInfo.getListBuildings(), buildingId);

            if (building == null) {
                send(new ResponseCollectResource(ErrorConst.BUILDING_NOT_EXIST), user);
                return;
            }

            if (!BuildingUtils.isResourceBuilding(building.getType())) {
                send(new ResponseCollectResource(ErrorConst.UNEXPECTED_BUILDING), user);
                return;
            }

            if (building.getStatus() != Building.Status.DONE) {
                send(new ResponseCollectResource(ErrorConst.BUILDING_ON_WORKING), user);
                return;
            }

            ResourceConfig collectorConfig = (ResourceConfig) BuildingUtils.getBuildingConfig(building.getType(), building.getLevel());
            CollectorBuilding collector = (CollectorBuilding) building;

            //check resources
            int currentTime = Common.currentTimeInSecond();

            int quantity = collectorConfig.productivity * (currentTime - collector.getLastCollectTime()) / 3600;
            if (quantity > collectorConfig.capacity)
                quantity = collectorConfig.capacity;

            //success
            collector.collect();

            if (collectorConfig.type.equals("gold")) {
                playerInfo.addResources(quantity, 0, 0);
            } else if (collectorConfig.type.equals("elixir")) {
                playerInfo.addResources(0, quantity, 0);
            }

            playerInfo.saveModel(user.getId());
            send(new ResponseCollectResource(ErrorConst.SUCCESS, collector, playerInfo.getGold(), playerInfo.getElixir()), user);
        } catch (Exception e) {
            System.out.println("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseCollectResource(ErrorConst.UNKNOWN), user);
        }
    }

    private void moveBuilding(User user, RequestMoveBuilding reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseMoveBuilding(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseMoveBuilding(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            //get building by id
            int buildingId = reqData.getBuildingId();
            Building building = BuildingUtils.getBuildingInListById(playerInfo.getListBuildings(), buildingId);

            if (building == null) {
                send(new ResponseMoveBuilding(ErrorConst.BUILDING_NOT_EXIST), user);
                return;
            }

            if (BuildingUtils.isObstacle(building.getType())) {
                send(new ResponseMoveBuilding(ErrorConst.BUILDING_CANT_BE_MOVED), user);
                return;
            }

            BaseBuildingConfig buildingDetail = BuildingUtils.getBuildingConfig(building.getType(), building.getLevel());

            //check position
            if (!BuildingUtils.checkBuildingPosition(playerInfo.getMap(), reqData.getPosition().x, reqData.getPosition().y,
                    buildingDetail.width, buildingDetail.height, buildingId)) {
                send(new ResponseMoveBuilding(ErrorConst.POSITION_INVALID), user);
                return;
            }

            //success
            building.setPosition(reqData.getPosition());
            int[][] map = playerInfo.getMap();

            for (int i = 0; i < buildingDetail.width; i++)
                for (int j = 0; j < buildingDetail.height; j++) {
                    int posX = (int) (building.getPosition().getX() + i);
                    int posY = (int) (building.getPosition().getY() + j);
                    if (posX < GameConfig.MAP_WIDTH && posY < GameConfig.MAP_HEIGHT)
                        map[posY][posX] = 0;

                    int newPosX = (int) (reqData.getPosition().x+ i);
                    int newPosY = (int) (reqData.getPosition().y + j);
                    if (newPosX < GameConfig.MAP_WIDTH && newPosY < GameConfig.MAP_HEIGHT)
                        map[newPosY][newPosX] = buildingId;
                }
            playerInfo.setMap(map);

            playerInfo.saveModel(user.getId());
            send(new ResponseMoveBuilding(ErrorConst.SUCCESS), user);
        } catch (Exception e) {
            System.out.println("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseMoveBuilding(ErrorConst.UNKNOWN), user);
        }

    }
}
