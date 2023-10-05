package util;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import util.config.*;

import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class GameConfig {
    private static GameConfig instance;

    public static final int MAP_WIDTH = 40;
    public static final int MAP_HEIGHT = 40;

    public InitGameConfig initGameConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> armyCampConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> townHallConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> builderHutConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> clanCastleConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> obstacleConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> resourceConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> storageConfig;
    public Map<String, Map<Integer, BarrackConfig>> barrackConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> defenceConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> laboratoryConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> wallConfig;
    public Map<String, List<ShopResourceItemConfig>>  shopResItemConfig;

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

            reader = new FileReader("./gameConfig/ArmyCamp.json");
            armyCampConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ArmyCampConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/TownHall.json");
            townHallConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, TownHallConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/BuilderHut.json");
            builderHutConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, BuilderHutConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/ClanCastle.json");
            clanCastleConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ClanCastleConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/Obstacle.json");
            obstacleConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ObstacleConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/Resource.json");
            resourceConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, ResourceConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/Storage.json");
            storageConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, StorageConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/Barrack.json");
            barrackConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, BarrackConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/Defence.json");
            defenceConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, DefenceConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/Laboratory.json");
            laboratoryConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, LaboratoryConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/Wall.json");
            wallConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, WallConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/ShopResItem.json");
            shopResItemConfig = gson.fromJson(reader, new TypeToken<Map<String, List<ShopResourceItemConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/Troop.json");
            troopConfig = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, TroopConfig>>>() {
            }.getType());

            reader = new FileReader("./gameConfig/TroopBase.json");
            troopBaseConfig = gson.fromJson(reader, new TypeToken<Map<String,  TroopBaseConfig>>() {
            }.getType());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BaseBuildingConfig getBuildingConfig(String type, int level) {
        if (type.startsWith("AMC"))
            return instance.armyCampConfig.get(type).get(level);
        if (type.startsWith("BDH"))
            return instance.builderHutConfig.get(type).get(level);
        if (type.startsWith("CLC"))
            return instance.clanCastleConfig.get(type).get(level);
        if (type.startsWith("RES"))
            return instance.resourceConfig.get(type).get(level);
        if (type.startsWith("OBS"))
            return instance.obstacleConfig.get(type).get(level);
        if (type.startsWith("TOW"))
            return instance.townHallConfig.get(type).get(level);
        if (type.startsWith("STO"))
            return instance.storageConfig.get(type).get(level);
        if (type.startsWith("BAR"))
            return instance.barrackConfig.get(type).get(level);
        if (type.startsWith("DEF"))
            return instance.defenceConfig.get(type).get(level);
        if (type.startsWith("LAB"))
            return instance.laboratoryConfig.get(type).get(level);
        if (type.startsWith("WAL"))
            return instance.wallConfig.get(type).get(level);

        return null;
    }
}
