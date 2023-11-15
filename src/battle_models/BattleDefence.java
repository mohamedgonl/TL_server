package battle_models;

import util.BattleConst;
import util.GameConfig;
import util.config.DefenceBaseConfig;
import util.config.DefenceConfig;
import util.log.LogUtils;

import java.awt.*;

public class BattleDefence extends BattleBuilding {
    public BattleMatch match;
    public DefenceBaseConfig defBaseStats;
    public DefenceConfig defStats;
    public BattleTroop target;
    public double attackCd = 0;
    public double attackSpeed;
    public Point centerPoint;
    public double minRange;
    public double maxRange;
    public double attackRadius;
    public int attackArea;

    public BattleDefence(int id, String type, int level, int posX, int posY) {
        super(id, type, level, posX, posY);
        this.defStats = (DefenceConfig) GameConfig.getInstance().getBuildingConfig(type, level);
        this.defBaseStats = GameConfig.getInstance().getDefBaseConfig(type);

        minRange = this.defBaseStats.minRange * BattleConst.GRID_BATTLE_RATIO;
        maxRange = this.defBaseStats.maxRange * BattleConst.GRID_BATTLE_RATIO;
        attackRadius = this.defBaseStats.attackRadius * BattleConst.GRID_BATTLE_RATIO;
        attackSpeed = this.defBaseStats.attackSpeed;

        centerPoint = new Point(posX + (int) Math.floor(width / 2), posY + (int) Math.floor(height / 2));
    }

    public void gameLoop(double dt) {
        if (this.attackCd > 0) {
            this.attackCd -= dt;
            return;
        }
        if (!this.hasTarget())
            return;

        this.attackCd = this.attackSpeed;
        this.attack(target);
    }

    public BattleTroop getTarget() {
        return target;
    }

    public void setTarget(BattleTroop target) {
        this.target = target;

        LogUtils.writeLog("def " + this.id + " set new target " + target.type);

    }

    //check if troop can be added as new target
    public boolean checkTarget(BattleTroop target) {
        // target in air
        if (target.isOverhead() && this.attackArea == BattleConst.DEF_ATTACK_AREA_GROUND) {
            return false;
        }
        // target on ground
        if (!target.isOverhead() && this.attackArea == BattleConst.DEF_ATTACK_AREA_OVERHEAD) {
            return false;
        }
        return isTargetInRange(target);
    }

    public boolean hasTarget() {
        return this.target != null && this.target.isAlive();
    }

    //check if current target is still valid or not
    //set current target to null if not valid
    public void validateCurrentTarget() {
        if (!this.hasTarget()) {
            this.target = null;
            return;
        }
        if (!this.isTargetInRange(this.target)) {
            this.target = null;
        }
    }

    public boolean isTargetInRange(BattleTroop target) {
        double dist = Math.sqrt(Math.pow(centerPoint.x - target.posX, 2) + Math.pow(centerPoint.y - target.posY, 2));
        return dist > minRange && dist < maxRange;
    }

    public void attack(BattleTroop troop) {
        BattleBullet bullet = match.getOrCreateBullet(type, centerPoint, troop, defStats.damagePerShot, attackRadius);

        LogUtils.writeLog("def " + this.id + " fire to " + target.posX + " " + target.posY);

    }
}
