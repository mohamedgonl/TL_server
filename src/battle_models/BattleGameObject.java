package battle_models;

import model.Building;
import util.BattleConst;
import util.GameConfig;
import util.config.BaseBuildingConfig;

public class BattleGameObject {
    public int id;
    public int posX;
    public int posY;
    public String type; // config id
    public int level;
    public int width;
    public int height;
    public transient BattleMatch match;

    public BattleGameObject(int id, String type, int level, int posX, int posY) {
        BaseBuildingConfig baseBuildingStats = GameConfig.getInstance().getBuildingConfig(type, level);
        this.posX = posX;
        this.posY = posY;
        this.type = type;
        this.level = level;
        this.id = id;
        this.width = baseBuildingStats.width * BattleConst.BATTLE_MAP_SCALE;
        this.height = baseBuildingStats.height * BattleConst.BATTLE_MAP_SCALE;
    }

    public BattleMatch getMatch() {
        return match;
    }

    public void setMatch(BattleMatch match) {
        this.match = match;
    }

    public static BattleGameObject convertFromCityBuilding(Building building) {
        int id = building.getId();
        String type = building.getType();
        int level = building.getLevel();
        int posX = (building.getPosition().x + BattleConst.BATTLE_MAP_BORDER) * BattleConst.BATTLE_MAP_SCALE;
        int posY = (building.getPosition().y + BattleConst.BATTLE_MAP_BORDER) * BattleConst.BATTLE_MAP_SCALE;

        if (type.startsWith("OBS"))
            return new BattleObstacle(id, type, 1, posX, posY);

        if (type.startsWith("DEF"))
            return new BattleDefence(id, type, level, posX, posY);

        if (type.equals("RES_1") || type.equals("STO_1"))
            return new BattleStorage(id, type, level, posX, posY, BattleConst.ResourceType.GOLD);

        if (type.equals("RES_2") || type.equals("STO_2"))
            return new BattleStorage(id, type, level, posX, posY, BattleConst.ResourceType.ELIXIR);

        return new BattleBuilding(id, type, level, posX, posY);
    }
}
