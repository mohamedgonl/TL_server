package util;

import model.Building;
import model.PlayerInfo;
import util.config.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerUtils {
    public static void initNewPlayerInfo(PlayerInfo playerInfo) throws Exception {
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

            Building building = new Building(id, type, 1, new Point(data.posX - 1, data.posY - 1));
            buildings.add(building);

            id++;
        }

        for (Map.Entry<Integer, InitGameConfig.ObsElement> entry : initGameConfig.obs.entrySet()) {
            InitGameConfig.ObsElement data = entry.getValue();

            Building building = new Building(id, data.type, 1, new Point(data.posX - 1, data.posY - 1));
            buildings.add(building);

            id++;
        }

        playerInfo.setListBuildings((ArrayList<Building>) buildings);
    }

    public static void initPlayerData(PlayerInfo playerInfo) {
        int[][] map = new int[GameConfig.MAP_HEIGHT][GameConfig.MAP_WIDTH];
        Map<String, Integer> buildingAmount = new HashMap<>();

        int totalBuilders = 0;
        int availableBuilders = 0;
        int goldCapacity = 0;
        int elixirCapacity = 0;

        for (Building building : playerInfo.getListBuildings()) {
            BaseBuildingConfig buildingDetail = BuildingUtils.getBuilding(building.getType(), building.getLevel());

            //update buildingAmount
            buildingAmount.put(building.getType(), buildingAmount.getOrDefault(building.getType(), 0) + 1);

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
            if (building.getType().startsWith("TOW")) {
                TownHallConfig townHall = (TownHallConfig) buildingDetail;
                playerInfo.setTownHallType(building.getType());
                playerInfo.setTownHallLv(building.getLevel());
                goldCapacity += townHall.capacityGold;
                elixirCapacity += townHall.capacityElixir;
            } else if (building.getType().startsWith("BDH")) {
                totalBuilders++;
                availableBuilders++;
            } else if (building.getType().startsWith("STO")) {
                StorageConfig storage = (StorageConfig) buildingDetail;
                switch (storage.type) {
                    case "gold":
                        goldCapacity += storage.capacity;
                    case "elixir":
                        elixirCapacity += storage.capacity;
                }
            }
            if (building.getStatus() == Building.Status.ON_BUILD || building.getStatus() == Building.Status.ON_UPGRADE) {
                if (building.getEndTime() <= Common.currentTimeInSecond()) {
                    if (building.getStatus() == Building.Status.ON_BUILD)
                        building.buildSuccess();
                    else building.upgradeSuccess();
                } else {
                    availableBuilders--;
                }
            }
        }
        playerInfo.setMap(map);
        playerInfo.setBuildingAmount(buildingAmount);
        playerInfo.setAvaiableBuilders(availableBuilders);
        playerInfo.setTotalBuilders(totalBuilders);
        playerInfo.setGoldCapacity(goldCapacity);
        playerInfo.setElixirCapacity(elixirCapacity);
    }
}
