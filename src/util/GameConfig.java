package util;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import util.config.*;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameConfig {
    public static final int MAP_WIDTH = 40;
    public static final int MAP_HEIGHT = 40;
    private static GameConfig instance;

    public InitGameConfig initGameConfig;

    public Map<String, Map<Integer, BaseBuildingConfig>> buildingConfig;

    public Map<String, List<ShopResourceItemConfig>> shopResItemConfig;


    public Map<String, Map<Integer, TroopConfig>> troopConfig;
    public Map<String, TroopBaseConfig> troopBaseConfig;

    private GameConfig() {
    }

    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }

    public void init() {
        Gson gson = new Gson();
        try {
            FileReader reader = new FileReader("./gameConfig/InitGame.json");
            initGameConfig = gson.fromJson(reader, InitGameConfig.class);

            //building config
            buildingConfig = new HashMap<>();

            reader = new FileReader("./gameConfig/ArmyCamp.json");
            Map<String, Map<Integer, BaseBuildingConfig>> armyCampConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ArmyCampConfig>>>() {
            }.getType());
            buildingConfig.putAll(armyCampConfig);

            reader = new FileReader("./gameConfig/TownHall.json");
            Map<String, Map<Integer, BaseBuildingConfig>> townHallConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, TownHallConfig>>>() {
            }.getType());
            buildingConfig.putAll(townHallConfig);

            reader = new FileReader("./gameConfig/BuilderHut.json");
            Map<String, Map<Integer, BaseBuildingConfig>> builderHutConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, BuilderHutConfig>>>() {
            }.getType());
            buildingConfig.putAll(builderHutConfig);

            reader = new FileReader("./gameConfig/ClanCastle.json");
            Map<String, Map<Integer, BaseBuildingConfig>> clanCastleConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ClanCastleConfig>>>() {
            }.getType());
            buildingConfig.putAll(clanCastleConfig);

            reader = new FileReader("./gameConfig/Obstacle.json");
            Map<String, Map<Integer, BaseBuildingConfig>> obstacleConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ObstacleConfig>>>() {
            }.getType());
            buildingConfig.putAll(obstacleConfig);

            reader = new FileReader("./gameConfig/Resource.json");
            Map<String, Map<Integer, BaseBuildingConfig>> resourceConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ResourceConfig>>>() {
            }.getType());
            buildingConfig.putAll(resourceConfig);

            reader = new FileReader("./gameConfig/Storage.json");
            Map<String, Map<Integer, BaseBuildingConfig>> storageConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, StorageConfig>>>() {
            }.getType());
            buildingConfig.putAll(storageConfig);

            reader = new FileReader("./gameConfig/Barrack.json");
            Map<String, Map<Integer, BaseBuildingConfig>> barrackConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, BarrackConfig>>>() {
            }.getType());
            buildingConfig.putAll(barrackConfig);

            reader = new FileReader("./gameConfig/Defence.json");
            Map<String, Map<Integer, BaseBuildingConfig>> defenceConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, DefenceConfig>>>() {
            }.getType());
            buildingConfig.putAll(defenceConfig);

            reader = new FileReader("./gameConfig/Laboratory.json");
            Map<String, Map<Integer, BaseBuildingConfig>> laboratoryConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, LaboratoryConfig>>>() {
            }.getType());
            buildingConfig.putAll(laboratoryConfig);

            reader = new FileReader("./gameConfig/Wall.json");
            Map<String, Map<Integer, BaseBuildingConfig>> wallConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, WallConfig>>>() {
            }.getType());
            buildingConfig.putAll(wallConfig);

            //
            reader = new FileReader("./gameConfig/ShopResItem.json");
            shopResItemConfig = gson.fromJson(reader, new TypeToken<Map<String, List<ShopResourceItemConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/Troop.json");
            troopConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, TroopConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/TroopBase.json");
            troopBaseConfig = gson.fromJson(reader, new TypeToken<Map<String, TroopBaseConfig>>() {
            }.getType());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BaseBuildingConfig getBuildingConfig(String type, int level) {
        try {
            return instance.buildingConfig.get(type).get(level);
        } catch (Exception e) {
            return null;
        }
    }
}
