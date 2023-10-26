package service.city;

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

import java.awt.*;
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
                case CmdDefine.UPGRADE_LIST_WALL:
                    RequestUpgradeListWall reqUpgradeListWall = new RequestUpgradeListWall(dataCmd);
                    upgradeListWall(user, reqUpgradeListWall);
                    break;
                case CmdDefine.MOVE_LIST_WALL:
                    RequestMoveListWall reqMoveListWall = new RequestMoveListWall(dataCmd);
                    moveListWall(user, reqMoveListWall);
                    break;
                case CmdDefine.FINISH_WORK_BY_GEM:
                    RequestFinishWorkByGem reqFinishWorkByGem = new RequestFinishWorkByGem(dataCmd);
                    finishWorkByGem(user, reqFinishWorkByGem);
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

            Building newBuilding;
            synchronized (playerInfo) {
                //get building detail
                BaseBuildingConfig building;
                boolean isBuilderHut = BuildingFactory.isBuilderHutBuilding(type);
                if (isBuilderHut) {
                    int totalBDH = playerInfo.getBuildingAmount().getOrDefault(type, 0);
                    building = GameConfig.getInstance().getBuildingConfig(type, totalBDH + 1);
                    if (building == null) {
                        send(new ResponseBuyBuilding(ErrorConst.UNEXPECTED_BUILDING), user);
                        return;
                    }
                } else {
                    building = GameConfig.getInstance().getBuildingConfig(type, 1);
                }

                //check resources
                if (playerInfo.getGold() < building.gold || playerInfo.getElixir() < building.elixir || playerInfo.getGem() < building.coin) {
                    send(new ResponseBuyBuilding(ErrorConst.NOT_ENOUGH_RESOURCES), user);
                    return;
                }

                //check builder
                if (playerInfo.getAvaiableBuilders() == 0 && (building.coin == 0 || building.buildTime > 0)) {
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
                try {
                    Field buildingTypeField = townHall.getClass().getField(type);
                    buildingTypeField.setAccessible(true);
                    int maximumAmount = (int) buildingTypeField.get(townHall);
                    if (playerInfo.getBuildingAmount().getOrDefault(type, 0) >= maximumAmount) {
                        send(new ResponseBuyBuilding(ErrorConst.TOWNHALL_LEVEL_TOO_LOW), user);
                        return;
                    }
                } catch (NoSuchFieldException fieldException) {
                    //no limit
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
                playerInfo.setAvaiableBuilders(playerInfo.getAvaiableBuilders() + 1);
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
                int timeLeft = building.getEndTime() - Common.currentTimeInSecond();
                if (timeLeft > 0) {
                    send(new ResponseBuildSuccess(ErrorConst.BUILD_NOT_DONE, buildingId, timeLeft), user);
                    return;
                }

                onBuildSucess(playerInfo, building);

            }
            playerInfo.saveModel(user.getId());
            send(new ResponseBuildSuccess(ErrorConst.SUCCESS, building.getId()), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseBuildSuccess(ErrorConst.UNKNOWN), user);
        }
    }

    private void onBuildSucess(PlayerInfo playerInfo, Building building) {
        building.buildSuccess();
        playerInfo.freeBuilder(1);

        if (BuildingFactory.isStorageBuilding(building.getType())) {
            StorageConfig storage = (StorageConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());
            switch (storage.type) {
                case "gold":
                    playerInfo.setGoldCapacity(playerInfo.getGoldCapacity() + storage.capacity);
                    break;
                case "elixir":
                    playerInfo.setElixirCapacity(playerInfo.getElixirCapacity() + storage.capacity);
                    break;
            }
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
                if (playerInfo.getAvaiableBuilders() == 0) {
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
                int timeLeft = building.getEndTime() - Common.currentTimeInSecond();
                if (timeLeft > 0) {
                    send(new ResponseUpgradeSuccess(ErrorConst.BUILD_NOT_DONE, buildingId, timeLeft), user);
                    return;
                }

                onUpgradeSuccess(playerInfo, building);
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseUpgradeSuccess(ErrorConst.SUCCESS, building.getId()), user);

        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseUpgradeSuccess(ErrorConst.UNKNOWN), user);
        }
    }

    private void onUpgradeSuccess(PlayerInfo playerInfo, Building building) {
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
            playerInfo.setTownHallLv(building.getLevel());
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

                int maxQuantity = collectorConfig.productivity * (currentTime - collector.getLastCollectTime()) / 3600;
                if (maxQuantity > collectorConfig.capacity)
                    maxQuantity = collectorConfig.capacity;

                //success
                int collectTime = currentTime;
                int quantity = maxQuantity;

                if (collectorConfig.type.equals("gold")) {
                    quantity = Math.min(playerInfo.getGoldCapacity() - playerInfo.getGold(), maxQuantity);
                    playerInfo.addResources(quantity, 0, 0);
                } else if (collectorConfig.type.equals("elixir")) {
                    quantity = Math.min(playerInfo.getElixirCapacity() - playerInfo.getElixir(), maxQuantity);
                    playerInfo.addResources(0, quantity, 0);
                }

                if (quantity < maxQuantity) {
                    collectTime = collector.getLastCollectTime() +
                            (int) Math.ceil((double) quantity * (currentTime - collector.getLastCollectTime()) / maxQuantity);
                }

                collector.collect(collectTime);
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

                building.setPosition(reqData.getPosition());
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
                if (playerInfo.getAvaiableBuilders() == 0) {
                    send(new ResponseRemoveObstacle(ErrorConst.NOT_ENOUGH_BUILDER), user);
                    return;
                }

                //success
                playerInfo.useResources(buildingDetail.gold, buildingDetail.elixir, buildingDetail.coin);
                obstacle.startBuilding(Common.currentTimeInSecond(), buildingDetail.buildTime);

                if (obstacle.getStatus() == Building.Status.ON_BUILD) {
                    playerInfo.useBuilder(1);
                } else {
                    //remove obstacle from map
                    removeBuilding(playerInfo, obstacle, buildingDetail);
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
                int timeLeft = building.getEndTime() - Common.currentTimeInSecond();
                if (timeLeft > 0) {
                    send(new ResponseRemoveObstacleSuccess(ErrorConst.BUILD_NOT_DONE, buildingId, timeLeft), user);
                    return;
                }

                onRemoveObstacleSuccess(playerInfo, building);
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseRemoveObstacleSuccess(ErrorConst.SUCCESS, building.getId()), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseRemoveObstacleSuccess(ErrorConst.UNKNOWN), user);
        }
    }

    private void onRemoveObstacleSuccess(PlayerInfo playerInfo, Building building) {
        ObstacleConfig buildingDetail = (ObstacleConfig) GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());

        building.buildSuccess();
        playerInfo.freeBuilder(1);
        playerInfo.addResources(0, buildingDetail.rewardElixir, 0);
        removeBuilding(playerInfo, building, buildingDetail);
    }

    private void upgradeListWall(User user, RequestUpgradeListWall reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseUpgradeListWall(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseUpgradeListWall(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            Building building;
            int goldCost = 0;
            int elixirCost = 0;
            int gemCost = 0;
            int maxTownHallLvRequire = 0;

            synchronized (playerInfo) {

                int[] buildingIds = reqData.getBuildingIds();
                ArrayList<Building> walls = new ArrayList<>();

                for (int buildingId : buildingIds) {
                    building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);
                    if (building == null) {
                        send(new ResponseUpgradeListWall(ErrorConst.BUILDING_NOT_EXIST), user);
                        return;
                    }
                    if (building.getStatus() != Building.Status.DONE) {
                        send(new ResponseUpgradeListWall(ErrorConst.BUILDING_ON_WORKING), user);
                        return;
                    }
                    String type = building.getType();

                    if (BuildingFactory.isObstacle(type)) {
                        send(new ResponseUpgradeListWall(ErrorConst.UNEXPECTED_BUILDING), user);
                        return;
                    }
                    BaseBuildingConfig buildingDetail = GameConfig.getInstance().getBuildingConfig(type, building.getLevel() + 1);
                    if (buildingDetail == null) {
                        send(new ResponseUpgradeListWall(ErrorConst.UNEXPECTED_BUILDING), user);
                        return;
                    }
                    walls.add(building);
                    goldCost += buildingDetail.gold;
                    elixirCost += buildingDetail.elixir;
                    gemCost += buildingDetail.coin;
                    maxTownHallLvRequire = Math.max(maxTownHallLvRequire, buildingDetail.townHallLevelRequired);
                }

                //check resources
                if (playerInfo.getGold() < goldCost || playerInfo.getElixir() < elixirCost || playerInfo.getGem() < gemCost) {
                    send(new ResponseUpgradeListWall(ErrorConst.NOT_ENOUGH_RESOURCES), user);
                    return;
                }

                //check town hall lv
                if (playerInfo.getTownHallLv() < maxTownHallLvRequire) {
                    send(new ResponseUpgradeListWall(ErrorConst.TOWNHALL_LEVEL_TOO_LOW), user);
                    return;
                }

                //success
                playerInfo.useResources(goldCost, elixirCost, gemCost);
                for (Building wall : walls) {
                    wall.upgradeSuccess();
                }
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseUpgradeListWall(ErrorConst.SUCCESS, reqData.getBuildingIds()), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseUpgradeListWall(ErrorConst.UNKNOWN), user);
        }
    }

    private void moveListWall(User user, RequestMoveListWall reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseMoveListWall(ErrorConst.PARAM_INVALID), user);
                return;
            }

            if (reqData.getFirstPos().x < 0 || reqData.getFirstPos().x >= GameConfig.MAP_WIDTH
                    || reqData.getFirstPos().y < 0 || reqData.getFirstPos().y >= GameConfig.MAP_HEIGHT
                    || reqData.getNextFirstPos().x < 0 || reqData.getNextFirstPos().x >= GameConfig.MAP_WIDTH
                    || reqData.getNextFirstPos().y < 0 || reqData.getNextFirstPos().y >= GameConfig.MAP_HEIGHT) {
                send(new ResponseMoveListWall(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseMoveListWall(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            synchronized (playerInfo) {
                int[][] map = playerInfo.getMap();
                int posX = reqData.getFirstPos().x;
                int posY = reqData.getFirstPos().y;
                int nextPosX = reqData.getNextFirstPos().x;
                int nextPosY = reqData.getNextFirstPos().y;
                ArrayList<Building> listWall = new ArrayList<>();
                ArrayList<Point> listNewPostion = new ArrayList<>();

                for (short i = 0; i < reqData.getAmount(); i++) {
                    int wallId = map[posY][posX];
                    if (wallId == 0) {
                        send(new ResponseMoveListWall(ErrorConst.BUILDING_NOT_EXIST), user);
                        return;
                    }

                    Building wall = getBuildingInListById(playerInfo.getListBuildings(), wallId);

                    if (wall == null) {
                        send(new ResponseMoveListWall(ErrorConst.BUILDING_NOT_EXIST), user);
                        return;
                    }

                    if (!BuildingFactory.isWall(wall.getType())) {
                        send(new ResponseMoveListWall(ErrorConst.UNEXPECTED_BUILDING), user);
                        return;
                    }
                    listWall.add(wall);
                    listNewPostion.add(new Point(nextPosX, nextPosY));
                    posX += reqData.getDx();
                    posY += reqData.getDy();
                    nextPosX += reqData.getNewDx();
                    nextPosY += reqData.getNewDy();
                }


                //check position
                int[][] tempMap = map.clone();
                for (Building wall : listWall) {
                    BaseBuildingConfig buildingDetail = GameConfig.getInstance().getBuildingConfig(wall.getType(), wall.getLevel());
                    for (int i = 0; i < buildingDetail.width; i++)
                        for (int j = 0; j < buildingDetail.height; j++) {
                            int tempPosX = (int) (wall.getPosition().getX() + i);
                            int tempPosY = (int) (wall.getPosition().getY() + j);
                            if (tempPosX < GameConfig.MAP_WIDTH && tempPosY < GameConfig.MAP_HEIGHT)
                                tempMap[tempPosY][tempPosX] = 0;
                        }
                }
                for (int i = 0; i < listWall.size(); i++) {
                    Building wall = listWall.get(i);
                    Point newPos = listNewPostion.get(i);
                    BaseBuildingConfig buildingDetail = GameConfig.getInstance().getBuildingConfig(wall.getType(), wall.getLevel());
                    for (int x = 0; x < buildingDetail.width; x++)
                        for (int y = 0; y < buildingDetail.height; y++) {
                            int tempPosX = (int) (newPos.getX() + x);
                            int tempPosY = (int) (newPos.getY() + y);
                            if (tempPosX >= GameConfig.MAP_WIDTH || tempPosY >= GameConfig.MAP_HEIGHT || tempMap[tempPosY][tempPosX] > 0) {
                                send(new ResponseMoveListWall(ErrorConst.BUILDING_CANT_BE_MOVED), user);
                                return;
                            }
                            tempMap[tempPosY][tempPosX] = wall.getId();
                        }
                }
                //success
                for (int i = 0; i < listWall.size(); i++) {
                    Building wall = listWall.get(i);
                    Point newPos = listNewPostion.get(i);
                    wall.setPosition(newPos);
                }
                playerInfo.setMap(tempMap);
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseMoveListWall(ErrorConst.SUCCESS), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseMoveListWall(ErrorConst.UNKNOWN), user);
        }
    }

    private void finishWorkByGem(User user, RequestFinishWorkByGem reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseFinishWorkByGem(ErrorConst.PARAM_INVALID), user);
                return;
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseFinishWorkByGem(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            Building building;
            synchronized (playerInfo) {
                //get building by id
                int buildingId = reqData.getBuildingId();
                building = getBuildingInListById(playerInfo.getListBuildings(), buildingId);

                if (building == null) {
                    send(new ResponseFinishWorkByGem(ErrorConst.BUILDING_NOT_EXIST), user);
                    return;
                }

                if (building.getStatus() == Building.Status.DONE) {
                    send(new ResponseFinishWorkByGem(ErrorConst.BUILD_DONE), user);
                    return;
                }

                int currentTime = Common.currentTimeInSecond();
                int gemCost = building.getEndTime() > currentTime ? getGemByTimeInSecond(building.getEndTime() - currentTime) : 0;

                if (playerInfo.getGem() < gemCost) {
                    send(new ResponseFinishWorkByGem(ErrorConst.NOT_ENOUGH_RESOURCES), user);
                    return;
                }

                //success
                playerInfo.useResources(0, 0, gemCost);
                if (building.getStatus() == Building.Status.ON_BUILD) {
                    if (BuildingFactory.isObstacle(building.getType()))
                        onRemoveObstacleSuccess(playerInfo, building);
                    else onBuildSucess(playerInfo, building);
                } else if (building.getStatus() == Building.Status.ON_UPGRADE) {
                    onUpgradeSuccess(playerInfo, building);
                }
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseFinishWorkByGem(ErrorConst.SUCCESS, building.getId(), playerInfo.getGem()), user);
        } catch (Exception e) {
            logger.warn("BUILDING HANDLER EXCEPTION " + e.getMessage());
            send(new ResponseFinishWorkByGem(ErrorConst.UNKNOWN), user);
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
        if (map.length == 0 || posY < 0 || posX < 0 || posY + width - 1 >= map.length || posX + height - 1 >= map[0].length)
            return false;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (map[posY + j][posX + i] > 0)
                    return false;
        return true;
    }

    public boolean checkBuildingPosition(int[][] map, int posX, int posY, int width, int height, int buildingId) {
        if (map.length == 0 || posY < 0 || posX < 0 || posY + width - 1 >= map.length || posX + height - 1 >= map[0].length)
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

    public int getGemByTimeInSecond(int time) {
        int secondPerHour = 3600;
        int gemPerHour = 15;
        return (int) Math.ceil((double) (gemPerHour * time) / secondPerHour);
    }

}
