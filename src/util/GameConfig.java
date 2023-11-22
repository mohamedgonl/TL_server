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
    public InitGameConfig fakeInitGameConfig;

    public Map<String, Map<Integer, BaseBuildingConfig>> buildingConfig;

    public Map<String, List<ShopResourceItemConfig>> shopResItemConfig;


    public Map<String, Map<Integer, TroopConfig>> troopConfig;
    public Map<String, TroopBaseConfig> troopBaseConfig;
    public Map<String, DefenceBaseConfig> defenceBaseConfig;

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
            FileReader reader = new FileReader("./conf/InitGame.json");
            initGameConfig = gson.fromJson(reader, InitGameConfig.class);

            //building config
            buildingConfig = new HashMap<>();

            reader = new FileReader("./conf/ArmyCamp.json");
            Map<String, Map<Integer, BaseBuildingConfig>> armyCampConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ArmyCampConfig>>>() {
            }.getType());
            buildingConfig.putAll(armyCampConfig);

            reader = new FileReader("./conf/TownHall.json");
            Map<String, Map<Integer, BaseBuildingConfig>> townHallConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, TownHallConfig>>>() {
            }.getType());
            buildingConfig.putAll(townHallConfig);

            reader = new FileReader("./conf/BuilderHut.json");
            Map<String, Map<Integer, BaseBuildingConfig>> builderHutConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, BuilderHutConfig>>>() {
            }.getType());
            buildingConfig.putAll(builderHutConfig);

            reader = new FileReader("./conf/ClanCastle.json");
            Map<String, Map<Integer, BaseBuildingConfig>> clanCastleConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ClanCastleConfig>>>() {
            }.getType());
            buildingConfig.putAll(clanCastleConfig);

            reader = new FileReader("./conf/Obstacle.json");
            Map<String, Map<Integer, BaseBuildingConfig>> obstacleConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ObstacleConfig>>>() {
            }.getType());
            buildingConfig.putAll(obstacleConfig);

            reader = new FileReader("./conf/Resource.json");
            Map<String, Map<Integer, BaseBuildingConfig>> resourceConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ResourceConfig>>>() {
            }.getType());
            buildingConfig.putAll(resourceConfig);

            reader = new FileReader("./conf/Storage.json");
            Map<String, Map<Integer, BaseBuildingConfig>> storageConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, StorageConfig>>>() {
            }.getType());
            buildingConfig.putAll(storageConfig);

            reader = new FileReader("./conf/Barrack.json");
            Map<String, Map<Integer, BaseBuildingConfig>> barrackConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, BarrackConfig>>>() {
            }.getType());
            buildingConfig.putAll(barrackConfig);

            reader = new FileReader("./conf/Defence.json");
            Map<String, Map<Integer, BaseBuildingConfig>> defenceConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, DefenceConfig>>>() {
            }.getType());
            buildingConfig.putAll(defenceConfig);

            reader = new FileReader("./conf/Laboratory.json");
            Map<String, Map<Integer, BaseBuildingConfig>> laboratoryConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, LaboratoryConfig>>>() {
            }.getType());
            buildingConfig.putAll(laboratoryConfig);

            reader = new FileReader("./conf/Wall.json");
            Map<String, Map<Integer, BaseBuildingConfig>> wallConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, WallConfig>>>() {
            }.getType());
            buildingConfig.putAll(wallConfig);

            //
            reader = new FileReader("./conf/ShopResItem.json");
            shopResItemConfig = gson.fromJson(reader, new TypeToken<Map<String, List<ShopResourceItemConfig>>>() {
            }.getType());

            reader = new FileReader("./conf/Troop.json");
            troopConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, TroopConfig>>>() {
            }.getType());

            reader = new FileReader("./conf/TroopBase.json");
            troopBaseConfig = gson.fromJson(reader, new TypeToken<Map<String, TroopBaseConfig>>() {
            }.getType());

            reader = new FileReader("./conf/DefenceBase.json");
            defenceBaseConfig = gson.fromJson(reader, new TypeToken<Map<String, DefenceBaseConfig>>() {
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
    public DefenceBaseConfig getDefBaseConfig(String type) {
        try {
            return instance.defenceBaseConfig.get(type);
        } catch (Exception e) {
            return null;
        }
    }
}
