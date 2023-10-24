package event.handler;

import bitzero.server.core.BZEventParam;
import bitzero.server.core.IBZEvent;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseServerEventHandler;
import bitzero.server.extensions.ExtensionLogLevel;
import bitzero.util.ExtensionUtility;
import event.eventType.DemoEventParam;
import model.Building;
import model.Obstacle;
import model.PlayerInfo;
import util.BuildingFactory;
import util.Common;
import util.GameConfig;
import util.config.BaseBuildingConfig;
import util.config.InitGameConfig;
import util.config.StorageConfig;
import util.config.TownHallConfig;
import util.server.ServerConstant;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginSuccessHandler extends BaseServerEventHandler {
    public LoginSuccessHandler() {
        super();
    }

    public void handleServerEvent(IBZEvent iBZEvent) {
        this.onLoginSuccess((User) iBZEvent.getParameter(BZEventParam.USER));
    }

    /**
     * @param user description: after login successful to server, core framework will dispatch this event
     */
    private void onLoginSuccess(User user) {
        trace(ExtensionLogLevel.DEBUG, "On Login Success ", user.getName());
        PlayerInfo pInfo = null;
        try {
            //get from db
            pInfo = (PlayerInfo) PlayerInfo.getModel(user.getId(), PlayerInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pInfo == null) {
            pInfo = new PlayerInfo(user.getId(), "username_" + user.getId());
            try {
                initNewPlayerInfo(pInfo);
                //save to db
                pInfo.saveModel(user.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //init map & capacity from buildings
        initPlayerData(pInfo);

        /**
         * cache playerinfo in RAM
         */
        user.setProperty(ServerConstant.PLAYER_INFO, pInfo);

        /**
         * send login success to client
         * after receive this message, client begin to send game logic packet to server
         */
        ExtensionUtility.instance().sendLoginOK(user);

        /**
         * dispatch event here
         */
        Map evtParams = new HashMap();
        evtParams.put(DemoEventParam.USER, user);
        evtParams.put(DemoEventParam.NAME, user.getName());
//        ExtensionUtility.dispatchEvent(new BZEvent(DemoEventType.LOGIN_SUCCESS, evtParams));
    }

    private void initNewPlayerInfo(PlayerInfo playerInfo) throws Exception {
        GameConfig gameConfig = GameConfig.getInstance();
        System.out.println("init new player info: " + playerInfo.getId());

        InitGameConfig initGameConfig = gameConfig.initGameConfig;

        playerInfo.setGold(initGameConfig.player.gold);
        playerInfo.setGem(initGameConfig.player.coin);
        playerInfo.setElixir(initGameConfig.player.elixir);

        List buildings = new ArrayList<Building>();
        int id = 1;

        for (Map.Entry<String, InitGameConfig.MapElement> entry : initGameConfig.map.entrySet()) {
            String type = entry.getKey();
            InitGameConfig.MapElement data = entry.getValue();

            Building building = BuildingFactory.getBuilding(id, type, 1, new Point(data.posX - 1, data.posY - 1));

            buildings.add(building);
            id++;
        }

        for (Map.Entry<Integer, InitGameConfig.ObsElement> entry : initGameConfig.obs.entrySet()) {
            InitGameConfig.ObsElement data = entry.getValue();

            Building building = BuildingFactory.getBuilding(id, data.type, 1, new Point(data.posX - 1, data.posY - 1));
            buildings.add(building);
            id++;
        }

        playerInfo.setListBuildings((ArrayList<Building>) buildings);
    }

    private void initPlayerData(PlayerInfo playerInfo) {
        int[][] map = new int[GameConfig.MAP_HEIGHT][GameConfig.MAP_WIDTH];
        Map<String, Integer> buildingAmount = new HashMap<>();

        int totalBuilders = 0;
        int availableBuilders = 0;
        int goldCapacity = 0;
        int elixirCapacity = 0;

        List<Building> listBuildingToRemove = new ArrayList<>();

        for (Building building : playerInfo.getListBuildings()) {
            //update building status
            if (building.getStatus() == Building.Status.ON_BUILD || building.getStatus() == Building.Status.ON_UPGRADE) {
                if (building.getEndTime() <= Common.currentTimeInSecond()) {
                    if (building.getStatus() == Building.Status.ON_BUILD)
                        building.buildSuccess();
                    else building.upgradeSuccess();
                } else {
                    availableBuilders--;
                }
            }

            BaseBuildingConfig buildingDetail = GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());

            if (building instanceof Obstacle) {
                if (((Obstacle) building).isRemove()) {
                    listBuildingToRemove.add(building);
                    continue;
                }
            } else {
                //update buildingAmount
                buildingAmount.put(building.getType(), buildingAmount.getOrDefault(building.getType(), 0) + 1);
            }

            //update map
            if (buildingDetail.height <= 0 || buildingDetail.height <= 0)
                return;

            for (int i = 0; i < buildingDetail.width; i++)
                for (int j = 0; j < buildingDetail.height; j++) {
                    int posX = (int) (building.getPosition().getX() + i);
                    int posY = (int) (building.getPosition().getY() + j);
                    if (posX < GameConfig.MAP_WIDTH && posY < GameConfig.MAP_HEIGHT)
                        map[posY][posX] = building.getId();
                }

            //update resources capacity
            if (BuildingFactory.isTownHallBuilding(building.getType())) {
                TownHallConfig townHall = (TownHallConfig) buildingDetail;
                playerInfo.setTownHallType(building.getType());
                playerInfo.setTownHallLv(building.getLevel());
                goldCapacity += townHall.capacityGold;
                elixirCapacity += townHall.capacityElixir;
            } else if (BuildingFactory.isBuilderHutBuilding(building.getType())) {
                totalBuilders++;
                availableBuilders++;
            } else if (BuildingFactory.isStorageBuilding(building.getType())) {
                StorageConfig storage = (StorageConfig) buildingDetail;
                switch (storage.type) {
                    case "gold":
                        goldCapacity += storage.capacity;
                        break;
                    case "elixir":
                        elixirCapacity += storage.capacity;
                        break;
                }
            }
        }

        for (Building building : listBuildingToRemove) {
            playerInfo.getListBuildings().remove(building);
        }

        playerInfo.setMap(map);
        playerInfo.setBuildingAmount(buildingAmount);
        playerInfo.setAvaiableBuilders(availableBuilders);
        playerInfo.setTotalBuilders(totalBuilders);
        playerInfo.setGoldCapacity(goldCapacity);
        playerInfo.setElixirCapacity(elixirCapacity);
    }
}
