package battle_models;

import model.Building;
import util.GameConfig;
import util.config.BaseBuildingConfig;

public class BattleBuilding {
    public int id;
    public int posX;
    public int posY;
    public int hp; // hp hien tai
    public String type; // config id
    public int level;
    public int gold = 0;
    public int elixir = 0;
    public BaseBuildingConfig baseBuildingStats;

    public BattleBuilding(int id, String type, int level, int posX, int posY){
        this.baseBuildingStats = GameConfig.getInstance().getBuildingConfig(type, level);
        this.posX = posX;
        this.posY = posY;
        this.type = type;
        this.level = level;
        this.id = id;
        this.hp = this.baseBuildingStats.hitpoints;
    }

    public static BattleBuilding convertFromCityBuilding(Building building) {
        return new BattleBuilding(building.getId(), building.getType(), building.getLevel(), building.getPosition().x * 3 + 3, building.getPosition().y * 3 + 3);
    }

}
