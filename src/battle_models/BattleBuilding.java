package battle_models;

import util.GameConfig;
import util.config.BaseBuildingConfig;
import util.log.LogUtils;

public class BattleBuilding extends BattleGameObject {
    public transient int hp; // hp hien tai
    public int maxHp;

    public BattleBuilding(int id, String type, int level, int posX, int posY) {
        super(id, type, posX, level, posY);
        BaseBuildingConfig baseBuildingStats = GameConfig.getInstance().getBuildingConfig(type, level);

        this.hp = baseBuildingStats.hitpoints;
        this.maxHp = baseBuildingStats.hitpoints;
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

        LogUtils.writeLog("building " + this.id + " gain " + damage + " ~ " + this.hp);
    }

    public void onDestroy() {
        this.match.onDestroyBuilding(this.id);

        LogUtils.writeLog("building " + this.id + " destroyed");
    }

    @Override
    public String toString() {
        return "BattleBuilding{" +
                "id=" + id +
                ", posX=" + posX +
                ", posY=" + posY +
                ", hp=" + hp +
                ", type='" + type + '\'' +
                ", level=" + level +
                ", width=" + width +
                ", height=" + height +
                ", maxHp=" + maxHp +
                '}';
    }
}
