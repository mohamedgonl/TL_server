package util;

import model.Building;
import model.PlayerInfo;
import util.config.BaseBuildingConfig;
import util.config.GameConfig;
import util.config.InitGameConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerUtils {
    public static void initPlayerInfo(PlayerInfo playerInfo) throws Exception {
        GameConfig gameConfig = GameConfig.getInstance();
        System.out.println("init player info: " + playerInfo.getId());

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
        genaratePlayerMap(playerInfo);
    }

    public static void genaratePlayerMap(PlayerInfo playerInfo) {
        final int MAP_WIDTH = 40;
        final int MAP_HEIGHT = 40;
        int[][] map = new int[MAP_HEIGHT][MAP_WIDTH];

        for (Building building : playerInfo.getListBuildings()) {
            BaseBuildingConfig buildingDetail = getBuilding(building.getType(), building.getLevel());

            int width = buildingDetail.width;
            int height = buildingDetail.height;

            if (width <= 0 || height <= 0)
                return;

            for (int i = 0; i < width; i++)
                for (int j = 0; j < height; j++) {
                    int posX = (int) (building.getPosition().getX() + i);
                    int posY = (int) (building.getPosition().getY() + j);
                    if (posX < MAP_HEIGHT && posY < MAP_HEIGHT)
                        map[posX][posY] = building.getId();
                }
        }
        playerInfo.setMap(map);
    }

    private static BaseBuildingConfig getBuilding(String type, int level) {
        GameConfig gameConfig = GameConfig.getInstance();
        if (type.startsWith("AMC"))
            return gameConfig.armyCampConfig.get(type).get(level);
        if (type.startsWith("BDH"))
            return gameConfig.builderHutConfig.get(type).get(level);
        if (type.startsWith("CLC"))
            return gameConfig.clanCastleConfig.get(type).get(level);
        if (type.startsWith("RES"))
            return gameConfig.resourceConfig.get(type).get(level);
        if (type.startsWith("OBS"))
            return gameConfig.obstacleConfig.get(type).get(level);
        if (type.startsWith("TOW"))
            return gameConfig.townHallConfig.get(type).get(level);
        if (type.startsWith("STO"))
            return gameConfig.storageConfig.get(type).get(level);
        if (type.startsWith("BAR"))
            return gameConfig.barrackConfig.get(type).get(level);
        if (type.startsWith("DEF"))
            return gameConfig.defenceConfig.get(type).get(level);
        if (type.startsWith("LAB"))
            return gameConfig.laboratoryConfig.get(type).get(level);
        if (type.startsWith("WAL"))
            return gameConfig.wallConfig.get(type).get(level);

        return gameConfig.townHallConfig.get(type).get(level);
    }
}
