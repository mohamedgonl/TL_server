package battle_models;

import model.Building;
import util.BattleConst;
import util.GameConfig;
import util.config.BaseBuildingConfig;

public class BattleBuilding {
    public int id;
    public int posX;
    public int posY;
    public int hp; // hp hien tai
    public String type; // config id
    public int level;
    private int capacity;
    private int resourceLeft;

    public int width;
    public int height;

    public int maxHp;

    public BattleMatch match;

    public BattleBuilding(int id, String type, int level, int posX, int posY){
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

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setResourceLeft(int resourceLeft) {
        this.resourceLeft = resourceLeft;
    }

    public static BattleBuilding convertFromCityBuilding(Building building) {
        return new BattleBuilding(building.getId(), building.getType(), building.getLevel(),
                (building.getPosition().x+ BattleConst.BATTLE_MAP_BORDER) * BattleConst.BATTLE_MAP_SCALE,
                (building.getPosition().y +BattleConst.BATTLE_MAP_BORDER)* BattleConst.BATTLE_MAP_SCALE );
    }

    public boolean isDestroy(){
        return this.hp <= 0;
    }

    public void onGainDamage(int damage) {
        if (damage <= 0 || this.hp <= 0) {
            return;
        }
        this.hp = Math.max(this.hp - damage, 0);

        if(this.type.startsWith("RES") || this.type.startsWith("STO")) {
            int resource = (int) Math.ceil((double) (damage * this.capacity) / this.maxHp);
            if (resource <= this.resourceLeft) {
                this.reduceResource(
                        resource,
                        this.type.substring(4).equals("1") ? BattleConst.ResourceType.GOLD
                                : BattleConst.ResourceType.ELIXIR);
            }
        }

        if (this.hp == 0) {
            this.onDestroy();
        }
    }

    public void reduceResource(int resource, BattleConst.ResourceType type) {
        this.resourceLeft -= resource;
        this.match.updateResourceGot(resource, type);

    }

    public void onDestroy() {
        this.match.onDestroyBuilding(this.id);
    }

}
