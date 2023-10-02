package util.config;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    public Map<String, Map<Integer, BaseBuildingConfig>> barrackConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> defenceConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> laboratoryConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> wallConfig;
    public Map<String, List<ShopResourceItemConfig>>  shopResItemConfig;

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



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
