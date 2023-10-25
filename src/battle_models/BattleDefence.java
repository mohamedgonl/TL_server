package battle_models;

import util.GameConfig;
import util.config.DefenceConfig;

public class BattleDefence extends BattleBuilding{

    public DefenceConfig defBase;

    public void attack(BattleTroop troop) {

    }

    public BattleDefence(String type, int level, int posX, int posY){
        super(type, level, posX, posY);
        this.defBase = (DefenceConfig) GameConfig.getInstance().getBuildingConfig(type,level);

    }

}
