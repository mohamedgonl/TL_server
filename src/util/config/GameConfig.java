package util.config;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.util.Map;

public class GameConfig {
    private static GameConfig instance;

    public InitGameConfig initGameConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> armyCampConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> townHallConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> builderHutConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> clanCastleConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> obstacleConfig;
    public Map<String, Map<Integer, BaseBuildingConfig>> resourceConfig;

    private GameConfig() {
    }

    public static GameConfig getInstance() {
        if (instance == null) {
            System.out.println("new game config");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
