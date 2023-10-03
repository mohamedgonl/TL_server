package util;

import model.Building;
import model.CollectorBuilding;

import java.awt.*;

public class BuildingFactory {
    public static Building getBuilding(int id, String type, int level, Point position) {
        if (isResourceBuilding(type))
            return new CollectorBuilding(id, type, level, position);
        return new Building(id, type, level, position);
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
