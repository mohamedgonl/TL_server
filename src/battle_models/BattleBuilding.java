package battle_models;

import model.Building;
import util.BattleConst;
import util.GameConfig;
import util.config.BaseBuildingConfig;

public class BattleBuilding {
    public int id;
    public int posX;
    public int posY;
    public transient int hp; // hp hien tai
    public String type; // config id
    public int level;
    public int width;
    public int height;
    public int maxHp;
    public transient BattleMatch match;

    public BattleBuilding(int id, String type, int level, int posX, int posY) {
        BaseBuildingConfig baseBuildingStats = GameConfig.getInstance().getBuildingConfig(type, level);
        this.posX = posX;
        this.posY = posY;
        this.type = type;
        this.level = level;
        this.id = id;
        this.width = baseBuildingStats.width * BattleConst.BATTLE_MAP_SCALE;
        this.height = baseBuildingStats.height * BattleConst.BATTLE_MAP_SCALE;
        this.hp = baseBuildingStats.hitpoints;
        this.maxHp = baseBuildingStats.hitpoints;

    }

    public static BattleBuilding convertFromCityBuilding(Building building) {
        int id = building.getId();
        String type = building.getType();
        int level = building.getLevel();
        int posX = (building.getPosition().x + BattleConst.BATTLE_MAP_BORDER) * BattleConst.BATTLE_MAP_SCALE;
        int posY = (building.getPosition().y + BattleConst.BATTLE_MAP_BORDER) * BattleConst.BATTLE_MAP_SCALE;

        if (type.startsWith("DEF"))
            return new BattleDefence(id, type, level, posX, posY);

        if (type.equals("RES_1") || type.equals("STO_1"))
            return new BattleStorage(id, type, level, posX, posY, BattleConst.ResourceType.GOLD);

        if (type.equals("RES_2") || type.equals("STO_2"))
            return new BattleStorage(id, type, level, posX, posY, BattleConst.ResourceType.ELIXIR);

        return new BattleBuilding(id, type, level, posX, posY);
    }

    public boolean isDestroy() {
        return this.hp <= 0;
    }

    public void onGainDamage(int damage) {
        if (damage <= 0 || this.hp <= 0) {
            return;
        }
        this.hp = Math.max(this.hp - damage, 0);
        if (this.hp <= 0) {
            this.onDestroy();
        }
    }

    public void onDestroy() {
        this.match.onDestroyBuilding(this.id);
    }

}
