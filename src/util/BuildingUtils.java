package util;

import model.Building;
import model.CollectorBuilding;
import util.config.BaseBuildingConfig;
import util.config.GameConfig;

import java.awt.*;
import java.util.ArrayList;

public class BuildingUtils {
    public static Building getBuilding(int id, String type, int level, Point position) {
        if (isResourceBuilding(type))
            return new CollectorBuilding(id, type, level, position);
        return new Building(id, type, level, position);
    }

    public static BaseBuildingConfig getBuildingConfig(String type, int level) {
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

    public static boolean checkBuildingPosition(int[][] map, int posX, int posY, int width, int height) {
        if (map.length == 0 || posY < 0 || posX < 0 || posY + width >= map.length || posX + height >= map[0].length)
            return false;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (map[posY + j][posX + i] > 0)
                    return false;
        return true;
    }

    public static boolean checkBuildingPosition(int[][] map, int posX, int posY, int width, int height, int buildingId) {
        if (map.length == 0 || posY < 0 || posX < 0 || posY + width >= map.length || posX + height >= map[0].length)
            return false;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (map[posY + j][posX + i] > 0 && map[posY + j][posX + i] != buildingId)
                    return false;
        return true;
    }

    public static Building getBuildingInListById(ArrayList<Building> list, int buildingId) {
        for (Building building : list) {
            if (building.getId() == buildingId) {
                return building;
            }
        }
        return null;
    }

    public static boolean isResourceBuilding(String type) {
        return type.startsWith("RES");
    }

    public static boolean isTownHallBuilding(String type) {
        return type.startsWith("TOW");
    }

    public static boolean isBuilderHutBuilding(String type) {
        return type.startsWith("BDH");
    }

    public static boolean isStorageBuilding(String type) {
        return type.startsWith("BDH");
    }

    public static boolean isObstacle(String type) {
        return type.startsWith("OBS");
    }
}
