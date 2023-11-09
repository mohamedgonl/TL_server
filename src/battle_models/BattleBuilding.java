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

        if (this.hp == 0) {
            this.onDestroy();
        }
    }

    public void onDestroy() {

    }

}
