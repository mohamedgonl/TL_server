package util;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Building;
import model.PlayerInfo;

import java.awt.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameConfig {
    public GameConfig() {
        super();
    }

    public static void initPlayerInfo(PlayerInfo playerInfo) throws Exception {
//        if (playerInfo == null) {
//            playerInfo = new PlayerInfo();
        System.out.println("init player info: " + playerInfo.getId());
        JsonParser parser = new JsonParser();
        Object obj = null;

        try {
            obj = parser.parse(new FileReader("./gameConfig/InitGame.json"));
        } catch (Exception e) {
            throw e;
        }

        JsonObject dataInit = (JsonObject) obj;

        JsonObject player = dataInit.getAsJsonObject("player");
        playerInfo.setGold(player.get("gold").getAsInt());
        playerInfo.setGem(player.get("coin").getAsInt());
        playerInfo.setElixir(player.get("elixir").getAsInt());

        List buildings = new ArrayList<Building>();
        int id = 1;

        //parse list init building
        JsonObject mapJson = dataInit.getAsJsonObject("map");
        Set<Map.Entry<String, JsonElement>> buildingSet = mapJson.entrySet();
        for (Map.Entry<String, JsonElement> entry : buildingSet) {
            String type = entry.getKey();
            JsonObject position = mapJson.getAsJsonObject(type);
            int posX = position.get("posX").getAsInt();
            int posY = position.get("posY").getAsInt();
            Building building = new Building(id, type, 1, new Point(posX, posY));
            buildings.add(building);
            id++;
        }

        //parser list obstacles
        JsonObject obs = dataInit.getAsJsonObject("obs");
        Set<Map.Entry<String, JsonElement>> obsSet = obs.entrySet();
        for (Map.Entry<String, JsonElement> entry : obsSet) {
            JsonObject obsItem = obs.getAsJsonObject(entry.getKey());
            String type = obsItem.get("type").getAsString();
            int posX = obsItem.get("posX").getAsInt();
            int posY = obsItem.get("posY").getAsInt();
            Building building = new Building(id, type, new Point(posX, posY));
            buildings.add(building);
            id++;
        }

        playerInfo.setListBuildings((ArrayList<Building>) buildings);
//        updatePlayerMap(playerInfo, parser);
        //}
    }

    public static boolean initPlayerMap(PlayerInfo playerInfo) {
        JsonParser parser = new JsonParser();

        final int MAP_WIDTH = 40;
        final int MAP_HEIGHT = 40;
        int[][] map = new int[MAP_HEIGHT][MAP_WIDTH];

        for (Building building : playerInfo.getListBuildings()) {
            Object obj = null;
            String fileConfigName = getFileConfigNameFromType(building.getType());
            if (fileConfigName.length() == 0)
                continue;
            try {
                obj = parser.parse(new FileReader("./gameConfig/" + fileConfigName));
            } catch (Exception e) {
                return false;
            }

            JsonObject dataInit = (JsonObject) obj;

            JsonObject buildingDetail = dataInit.getAsJsonObject(building.getType()).getAsJsonObject(String.valueOf(building.getLevel()));

            int width = buildingDetail.get("width").getAsInt();
            int height = buildingDetail.get("height").getAsInt();

            if (width <= 0 || height <= 0)
                return false;

            for (int i = 0; i < width; i++)
                for (int j = 0; j < height; j++){
                    int posX = (int) (building.getPosition().getX() + i);
                    int posY = (int) (building.getPosition().getY() + j);
                    if (posX < MAP_HEIGHT && posY < MAP_HEIGHT)
                    map[posX][posY] = building.getId();
                }
        }
        playerInfo.setMap(map);
        return true;
    }

    public static String getFileConfigNameFromType(String type){
        if (type.startsWith("AMC"))
            return "ArmyCamp.json";
        if (type.startsWith("BAR"))
            return "Barrack.json";
        if (type.startsWith("BDH"))
            return "BuilderHut.json";
        if (type.startsWith("CLC"))
            return "ClanCastle.json";
        if (type.startsWith("DEF"))
            return "Defence.json";
        if (type.startsWith("LAB"))
            return "Laboratory.json";
        if (type.startsWith("OBS"))
            return "Obstacle.json";
        if (type.startsWith("STO"))
            return "Storage.json";
        if (type.startsWith("TOW"))
            return "TownHall.json";
        if (type.startsWith("WAL"))
            return "Wall.json";
        return "";
    }
}
