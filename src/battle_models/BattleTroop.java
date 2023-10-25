package battle_models;

import util.GameConfig;
import util.config.TroopBaseConfig;
import util.config.TroopConfig;

public class BattleTroop {
    public String type;
    public int posX;
    public int posY;
    public int hp;
    public int level;
    public TroopBaseConfig baseStats;
    public TroopConfig stats;

    public void attack(BattleBuilding building) {

    }

    public void move(){

    }

    public BattleTroop(String type, int level, int posX, int posY){
        this.stats = GameConfig.getInstance().troopConfig.get(type).get(level) ;
        this.baseStats  = GameConfig.getInstance().troopBaseConfig.get(type);
        this.hp = this.stats.hitpoints;
        this.level = level;
        this.posX = posX;
        this.posY = posY;
    }

}
