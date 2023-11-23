package util;

import model.Barrack;
import model.Building;
import model.CollectorBuilding;
import model.Obstacle;

import java.awt.*;

public class BuildingFactory {
    public static class BuildingType {
        public static final String TOWN_HALL = "TOW_1";
        public static final String GOLD_MINE = "RES_1";
        public static final String ELIXIR_MINE = "RES_2";
        public static final String GOLD_STORAGE = "STO_1";
        public static final String ELIXIR_STORAGE = "STO_2";
        public static final String WALL = "WAL_1";
        public static final String CANNON = "DEF_1";
        public static final String ARCHER_TOWER = "DEF_2";
        public static final String MORTAR = "DEF_3";
        public static final String AIR_DEFENSE = "DEF_4";
        public static final String BUILDER_HUT = "BDH_1";
        public static final String BARRACK = "BAR_1";
        public static final String ARMY_CAMP = "AMC_1";
    }

    public static class GameObjectPrefix {
        public static final String TOWN_HALL = "TOW";
        public static final String RESOURCE = "RES";
        public static final String STORAGE = "STO";
        public static final String WALL = "WAL";
        public static final String DEFENCE = "DEF";
        public static final String BUILDER_HUT = "BDH";
        public static final String BARRACK = "BAR";
        public static final String ARMY_CAMP = "AMC";
        public static final String OBSTACLE = "OBS";
    }

    public static Building getBuilding(int id, String type, int level, Point position) {
        if (isResourceBuilding(type))
            return new CollectorBuilding(id, type, level, position);
        if (isBarrack(type))
            return new Barrack(id, type, level, position);
        if (isObstacle(type))
            return new Obstacle(id, type, level, position);
        return new Building(id, type, level, position);
    }

    public static boolean isResourceBuilding(String type) {
        return type.startsWith(GameObjectPrefix.RESOURCE);
    }

    public static boolean isTownHallBuilding(String type) {
        return type.startsWith(GameObjectPrefix.TOWN_HALL);
    }

    public static boolean isBuilderHutBuilding(String type) {
        return type.startsWith(GameObjectPrefix.BUILDER_HUT);
    }

    public static boolean isStorageBuilding(String type) {
        return type.startsWith(GameObjectPrefix.STORAGE);
    }

    public static boolean isObstacle(String type) {
        return type.startsWith(GameObjectPrefix.OBSTACLE);
    }

    public static boolean isWall(String type) {
        return type.startsWith(GameObjectPrefix.WALL);
    }

    public static boolean isBarrack(String type) {
        return type.startsWith(GameObjectPrefix.BARRACK);
    }
}
