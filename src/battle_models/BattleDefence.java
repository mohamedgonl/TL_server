package battle_models;

import util.GameConfig;
import util.config.DefenceBaseConfig;
import util.config.DefenceConfig;

import java.awt.*;
import java.util.ArrayList;

public class BattleDefence extends BattleBuilding {
    public DefenceBaseConfig defBaseStats;
    public DefenceConfig defStats;
    public ArrayList<BattleTroop> targetQueue;
    public double attackCd = 0;
    public double attackSpeed = 0;
    public Point centerPoint;

    public BattleDefence(int id, String type, int level, int posX, int posY) {
        super(id, type, level, posX, posY);
        this.defStats = (DefenceConfig) GameConfig.getInstance().getBuildingConfig(type, level);
        this.defBaseStats = (DefenceBaseConfig) GameConfig.getInstance().defenceBaseConfig;
        this.centerPoint = new Point(posX + (int) Math.floor(defStats.width / 2), posY + (int) Math.floor(defStats.height / 2));
    }

    public void gameLoop(double dt) {
        if (this.attackCd > 0) {
            this.attackCd -= dt;
            return;
        }
        if (this.targetQueue.size() == 0)
            return;

        BattleTroop target = targetQueue.get(0);
        this.attackCd = this.attackSpeed;
        this.attack(target);
    }

    public boolean checkTargetInRange(BattleTroop target) {
        double dist = Math.sqrt(Math.pow(centerPoint.x - target.posX, 2) + Math.pow(centerPoint.y - target.posY, 2));
        return dist > defBaseStats.minRange && dist < defBaseStats.maxRange;
    }

    public void attack(BattleTroop troop) {
        if (type.equals("DEF_1")){// Cannon
            BattleBullet bullet = new BattleCannonBullet(new Point(posX, posY), troop, defStats.damagePerShot, type);
            //todo: add bullet to list bullets
        }
    }
}
