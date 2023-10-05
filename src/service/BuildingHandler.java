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
import model.Obstacle;
import model.PlayerInfo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.BuildingFactory;
import util.Common;
import util.GameConfig;
import util.config.*;
import util.server.ServerConstant;

import java.lang.reflect.Field;
import java.util.ArrayList;

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
                case CmdDefine.REMOVE_OBSTACLE:
                    RequestRemoveObstacle reqRemoveObstacle = new RequestRemoveObstacle(dataCmd);
                    removeObstacle(user, reqRemoveObstacle);
                    break;
                case CmdDefine.REMOVE_OBSTACLE_SUCCESS:
                    RequestRemoveObstacleSuccess reqRemoveObstacleSuccess = new RequestRemoveObstacleSuccess(dataCmd);
                    removeObstacleSuccess(user, reqRemoveObstacleSuccess);
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

            if (BuildingFactory.isObstacle(type)) {
                send(new ResponseBuyBuilding(ErrorConst.UNEXPECTED_BUILDING), user);
                return;
            }

            //get building detail
            BaseBuildingConfig building = GameConfig.getInstance().getBuildingConfig(type, 1);

            Building newBuilding;
            synchronized (playerInfo) {
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
                TownHallConfig townHall = (TownHallConfig) GameConfig.getInstance().getBuildingConfig(playerInfo.getTownHallType(), playerInfo.getTownHallLv());

                Field buildingTypeField = townHall.getClass().getField(type);
                buildingTypeField.setAccessible(true);

                int maximumAmount = (int) buildingTypeField.get(townHall);
                if (playerInfo.getBuildingAmount().getOrDefault(type, 0) >= maximumAmount) {
                    send(new ResponseBuyBuilding(ErrorConst.TOWNHALL_LEVEL_TOO_LOW), user);
                    return;
                }

                //check position
                if (!checkBuildingPosition(playerInfo.getMap(), reqData.getPosition().x, reqData.getPosition().y, building.width, building.height)) {
                    send(new ResponseBuyBuilding(ErrorConst.POSITION_INVALID), user);
                    return;
                }

                int newId = playerInfo.getListBuildings().get(playerInfo.getListBuildings().size() - 1).getId() + 1;
                newBuilding = BuildingFactory.getBuilding(newId, type, 1, reqData.getPosition());

                buildNewBuilding(playerInfo, newBuilding, building);
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseBuyBuilding(ErrorConst.SUCCESS, newBuilding), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseBuyBuilding(ErrorConst.UNKNOWN), user);
        }

    }

    private void buildNewBuilding(PlayerInfo playerInfo, Building newBuilding, BaseBuildingConfig buildingDetail) {
        //update resource
        playerInfo.useResources(buildingDetail.gold, buildingDetail.elixir, buildingDetail.coin);
        newBuilding.startBuilding(Common.currentTimeInSecond(), buildingDetail.buildTime);
        if (newBuilding.getStatus() == Building.Status.ON_BUILD) {
            playerInfo.useBuilder(1);
        } else if (newBuilding.getStatus() == Building.Status.DONE) {
            if (BuildingFactory.isBuilderHutBuilding(newBuilding.getType())) {
                playerInfo.setTotalBuilders(playerInfo.getTotalBuilders() + 1);
            }
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

            Building building;

            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

                if (building == null) {
                    send(new ResponseCancelBuild(ErrorConst.BUILDING_NOT_EXIST), user);
                    return;
                }

                //get building detail
                BaseBuildingConfig buildingDetail = GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());

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
                //update resources
                playerInfo.addResources(rewardGold, rewardElixir, rewardGem);
                playerInfo.freeBuilder(1);

                //update buildingAmount
                int amount = playerInfo.getBuildingAmount().getOrDefault(building.getType(), 0);
                if (amount > 0)
                    playerInfo.getBuildingAmount().put(building.getType(), amount - 1);

                removeBuilding(playerInfo, building, buildingDetail);
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseCancelBuild(ErrorConst.SUCCESS, building.getId()), user);

        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseCancelBuild(ErrorConst.UNKNOWN), user);
        }
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

            Building building;
            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

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

                if (BuildingFactory.isStorageBuilding(building.getType())) {
                    StorageConfig storage = (StorageConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());
                    switch (storage.type) {
                        case "gold":
                            playerInfo.setGoldCapacity(playerInfo.getGoldCapacity() + storage.capacity);
                        case "elixir":
                            playerInfo.setElixirCapacity(playerInfo.getElixir() + storage.capacity);
                    }
                }
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseBuildSuccess(ErrorConst.SUCCESS, building.getId()), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
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

            Building building;
            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

                if (building == null) {
                    send(new ResponseUpgradeBuilding(ErrorConst.BUILDING_NOT_EXIST), user);
                    return;
                }

                if (building.getStatus() != Building.Status.DONE) {
                    send(new ResponseUpgradeBuilding(ErrorConst.BUILDING_ON_WORKING), user);
                    return;
                }

                String type = building.getType();

                if (BuildingFactory.isObstacle(type)) {
                    send(new ResponseUpgradeBuilding(ErrorConst.UNEXPECTED_BUILDING), user);
                    return;
                }

                BaseBuildingConfig buildingDetail = GameConfig.getInstance().getBuildingConfig(type, building.getLevel() + 1);

                if (buildingDetail == null) {
                    send(new ResponseUpgradeBuilding(ErrorConst.UNEXPECTED_BUILDING), user);
                    return;
                }

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
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseUpgradeBuilding(ErrorConst.SUCCESS, building), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
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

            Building building;
            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

                if (building == null) {
                    send(new ResponseCancelUpgrade(ErrorConst.BUILDING_NOT_EXIST), user);
                    return;
                }

                //check upgrade done
                int currentTime = Common.currentTimeInSecond();
                if (building.getStatus() != Building.Status.ON_UPGRADE || building.getEndTime() <= currentTime) {
                    send(new ResponseCancelUpgrade(ErrorConst.BUILD_DONE), user);
                    return;
                }

                //get building detail
                BaseBuildingConfig buildingDetail = GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel() + 1);

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
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseCancelUpgrade(ErrorConst.SUCCESS, building.getId()), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
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

            Building building;
            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

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

                //update resource capacity
                if (BuildingFactory.isStorageBuilding(building.getType())) {
                    StorageConfig storage = (StorageConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel() - 1);
                    StorageConfig storageNextLevel = (StorageConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());
                    int capacity = storageNextLevel.capacity - storage.capacity;
                    switch (storage.type) {
                        case "gold":
                            playerInfo.setGoldCapacity(playerInfo.getGoldCapacity() + capacity);
                        case "elixir":
                            playerInfo.setElixirCapacity(playerInfo.getElixirCapacity() + capacity);
                    }
                } else if (BuildingFactory.isTownHallBuilding(building.getType())) {
                    TownHallConfig townHall = (TownHallConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel() - 1);
                    TownHallConfig townHallNextLv = (TownHallConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());

                    playerInfo.setGoldCapacity(playerInfo.getGoldCapacity() + townHallNextLv.capacityGold - townHall.capacityGold);
                    playerInfo.setElixirCapacity(playerInfo.getElixirCapacity() + townHallNextLv.capacityElixir - townHall.capacityElixir);
                }
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseUpgradeSuccess(ErrorConst.SUCCESS, building.getId()), user);

        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
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

            Building building;
            CollectorBuilding collector;
            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

                if (building == null) {
                    send(new ResponseCollectResource(ErrorConst.BUILDING_NOT_EXIST), user);
                    return;
                }

                if (!BuildingFactory.isResourceBuilding(building.getType())) {
                    send(new ResponseCollectResource(ErrorConst.UNEXPECTED_BUILDING), user);
                    return;
                }

                if (building.getStatus() != Building.Status.DONE) {
                    send(new ResponseCollectResource(ErrorConst.BUILDING_ON_WORKING), user);
                    return;
                }

                ResourceConfig collectorConfig = (ResourceConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());
                collector = (CollectorBuilding) building;

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
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseCollectResource(ErrorConst.SUCCESS, collector, playerInfo.getGold(), playerInfo.getElixir()), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
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

            Building building;
            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

                if (building == null) {
                    send(new ResponseMoveBuilding(ErrorConst.BUILDING_NOT_EXIST), user);
                    return;
                }

                if (BuildingFactory.isObstacle(building.getType())) {
                    send(new ResponseMoveBuilding(ErrorConst.BUILDING_CANT_BE_MOVED), user);
                    return;
                }

                BaseBuildingConfig buildingDetail = GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());

                //check position
                if (!checkBuildingPosition(playerInfo.getMap(), reqData.getPosition().x, reqData.getPosition().y,
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

                        int newPosX = reqData.getPosition().x + i;
                        int newPosY = reqData.getPosition().y + j;
                        if (newPosX < GameConfig.MAP_WIDTH && newPosY < GameConfig.MAP_HEIGHT)
                            map[newPosY][newPosX] = buildingId;
                    }
                playerInfo.setMap(map);
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseMoveBuilding(ErrorConst.SUCCESS), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseMoveBuilding(ErrorConst.UNKNOWN), user);
        }
    }

    private void removeObstacle(User user, RequestRemoveObstacle reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseRemoveObstacle(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseRemoveObstacle(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            Building building;
            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

                if (building == null) {
                    send(new ResponseRemoveObstacle(ErrorConst.BUILDING_NOT_EXIST), user);
                    return;
                }

                if (!BuildingFactory.isObstacle(building.getType())) {
                    send(new ResponseRemoveObstacle(ErrorConst.UNEXPECTED_BUILDING), user);
                    return;
                }

                Obstacle obstacle = (Obstacle) building;

                //get building detail
                ObstacleConfig buildingDetail = (ObstacleConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());

                //check resources
                if (playerInfo.getGold() < buildingDetail.gold || playerInfo.getElixir() < buildingDetail.elixir || playerInfo.getGem() < buildingDetail.coin) {
                    send(new ResponseRemoveObstacle(ErrorConst.NOT_ENOUGH_RESOURCES), user);
                    return;
                }

                //check builder
                if (buildingDetail.buildTime > 0 && playerInfo.getAvaiableBuilders() == 0) {
                    send(new ResponseRemoveObstacle(ErrorConst.NOT_ENOUGH_BUILDER), user);
                    return;
                }

                //success
                playerInfo.useResources(buildingDetail.gold, buildingDetail.elixir, buildingDetail.coin);

                if (buildingDetail.buildTime > 0) {
                    playerInfo.useBuilder(1);
                    obstacle.startBuilding(Common.currentTimeInSecond(), buildingDetail.buildTime);
                } else {
                    //remove obstacle from map
                    removeBuilding(playerInfo, obstacle, buildingDetail);
                    obstacle.buildSuccess();
                }
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseRemoveObstacle(ErrorConst.SUCCESS, building), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseRemoveObstacle(ErrorConst.UNKNOWN), user);
        }
    }

    private void removeObstacleSuccess(User user, RequestRemoveObstacleSuccess reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseRemoveObstacleSuccess(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseRemoveObstacleSuccess(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            Building building;
            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

                if (building == null) {
                    send(new ResponseRemoveObstacleSuccess(ErrorConst.BUILDING_NOT_EXIST), user);
                    return;
                }

                if (!BuildingFactory.isObstacle(building.getType())) {
                    send(new ResponseRemoveObstacleSuccess(ErrorConst.UNEXPECTED_BUILDING), user);
                    return;
                }

                //check if build already done
                if (building.getStatus() != Building.Status.ON_BUILD) {
                    send(new ResponseRemoveObstacleSuccess(ErrorConst.BUILD_DONE), user);
                    return;
                }

                //check if build not done
                if (building.getEndTime() > Common.currentTimeInSecond()) {
                    send(new ResponseRemoveObstacleSuccess(ErrorConst.BUILD_NOT_DONE), user);
                    return;
                }

                //success
                ObstacleConfig buildingDetail = (ObstacleConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());

                building.buildSuccess();
                playerInfo.freeBuilder(1);
                playerInfo.addResources(0, buildingDetail.rewardElixir, 0);
                removeBuilding(playerInfo, building, buildingDetail);
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseRemoveObstacleSuccess(ErrorConst.SUCCESS, building.getId()), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseRemoveObstacleSuccess(ErrorConst.UNKNOWN), user);
        }
    }

    private void removeBuilding(PlayerInfo playerInfo, Building building, BaseBuildingConfig buildingDetail) {
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

        //update listBuildings
        playerInfo.getListBuildings().remove(building);
    }

    public boolean checkBuildingPosition(int[][] map, int posX, int posY, int width, int height) {
        if (map.length == 0 || posY < 0 || posX < 0 || posY + width >= map.length || posX + height >= map[0].length)
            return false;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (map[posY + j][posX + i] > 0)
                    return false;
        return true;
    }

    public boolean checkBuildingPosition(int[][] map, int posX, int posY, int width, int height, int buildingId) {
        if (map.length == 0 || posY < 0 || posX < 0 || posY + width >= map.length || posX + height >= map[0].length)
            return false;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (map[posY + j][posX + i] > 0 && map[posY + j][posX + i] != buildingId)
                    return false;
        return true;
    }

    public Building getBuildingInListById(ArrayList<Building> list, int buildingId) {
        for (Building building : list) {
            if (building.getId() == buildingId) {
                return building;
            }
        }
        return null;
    }

}
