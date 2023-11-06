package battle_models;

import util.GameConfig;
import util.config.DefenceBaseConfig;
import util.config.DefenceConfig;

public class BattleDefence extends BattleBuilding{
    public DefenceBaseConfig defBaseStats;
    public DefenceConfig defStats;

    public void attack(BattleTroop troop) {

    }

    public BattleDefence(int id, String type, int level, int posX, int posY){
        super(id ,type, level, posX, posY);
//        this.defBaseStats =
        this.defStats = (DefenceConfig) GameConfig.getInstance().getBuildingConfig(type,level);
        this.defBaseStats = (DefenceBaseConfig) GameConfig.getInstance().defenceBaseConfig;

    }

}
