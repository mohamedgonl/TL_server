package battle_models;

import util.BattleConst;
import util.Common;
import util.GameConfig;
import util.config.DefenceBaseConfig;
import util.config.DefenceConfig;
import util.log.LogUtils;

import java.awt.*;
import java.util.ArrayList;

public class BattleDefence extends BattleBuilding {
    public transient DefenceBaseConfig defBaseStats;
    public transient DefenceConfig defStats;
    public transient BattleTroop target;
    public transient double attackCd = 0;
    public transient double attackSpeed;
    public transient Point centerPoint;
    public transient double minRange;
    public transient double maxRange;
    public transient double minRangeSquare;
    public transient double maxRangeSquare;
    public transient double attackRadius;
    public transient int attackArea;

    public BattleDefence(int id, String type, int level, int posX, int posY) {
        super(id, type, level, posX, posY);
        this.defStats = (DefenceConfig) GameConfig.getInstance().getBuildingConfig(type, level);
        this.defBaseStats = GameConfig.getInstance().getDefBaseConfig(type);

        minRange = this.defBaseStats.minRange * BattleConst.GRID_BATTLE_RATIO;
        maxRange = this.defBaseStats.maxRange * BattleConst.GRID_BATTLE_RATIO;
        attackRadius = this.defBaseStats.attackRadius * BattleConst.GRID_BATTLE_RATIO;
        attackSpeed = this.defBaseStats.attackSpeed;
        this.attackArea = this.defBaseStats.attackArea;

        minRangeSquare = minRange * minRange;
        maxRangeSquare = maxRange * maxRange;

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

    public void findTarget(ArrayList<BattleTroop> troops) {
        //remove target if dead or out of range
        if (this.target != null && (!this.target.isAlive() || !this.isTargetInRange(this.target))) {
            this.target = null;
        }

        if (this.target != null)
            return;

        //find new target
        ArrayList<BattleTroop> listTroopsAttackThis = new ArrayList<>();//troops attacking this def
        ArrayList<BattleTroop> listAttackingTroops = new ArrayList<>();
        ArrayList<BattleTroop> listSatisfyTroops = new ArrayList<>();

        for (BattleTroop troop : troops) {
            if (troop == null || !troop.isAlive()
                    || !this.checkTargetType(troop) || !this.isTargetInRange(troop)) continue;

            listSatisfyTroops.add(troop);

            if (troop.state == BattleTroop.TROOP_STATE.ATTACK) {
                if (troop.target != null && troop.target.id == this.id) {
                    listTroopsAttackThis.add(troop);
                }
                listAttackingTroops.add(troop);
            }
        }

        this.findNearestTarget(listTroopsAttackThis);
        if (this.target != null) {
            return;
        }
        this.findNearestTarget(listAttackingTroops);
        if (this.target != null) {
            return;
        }
        this.findNearestTarget(listSatisfyTroops);
    }

    public void findNearestTarget(ArrayList<BattleTroop> troops) {
        int minimumDist = Integer.MAX_VALUE;
        BattleTroop newTarget = null;

        for (BattleTroop troop : troops) {
            int dx = Math.abs(centerPoint.x - troop.posX);
            int dy = Math.abs(centerPoint.y - troop.posY);

            int distSquare = dx * dx + dy * dy;
            if (distSquare < minimumDist) {
                minimumDist = distSquare;
                newTarget = troop;
            }
        }
        if (newTarget != null) {
            this.setTarget(newTarget);
        }
    }

    //check attack area of troop
    public boolean checkTargetType(BattleTroop target) {
        // target in air
        if (target.isOverhead() && this.attackArea == BattleConst.DEF_ATTACK_AREA_GROUND) {
            return false;
        }
        // target on ground
        return target.isOverhead() || this.attackArea != BattleConst.DEF_ATTACK_AREA_OVERHEAD;
    }

    public boolean hasTarget() {
        return this.target != null && this.target.isAlive();
    }


    public boolean isTargetInRange(BattleTroop target) {
        int dx = Math.abs(centerPoint.x - target.posX);
        int dy = Math.abs(centerPoint.y - target.posY);

        if (dx > maxRange || dy > maxRange)
            return false;

        int distSquare = dx * dx + dy * dy;
        return distSquare >= minRangeSquare && distSquare <= maxRangeSquare;
    }

    public void attack(BattleTroop troop) {
        BattleBullet bullet = match.getOrCreateBullet(type, centerPoint, troop, defStats.damagePerShot, attackRadius, attackArea);

        LogUtils.writeLog("def " + this.id + " fire to " + target.posX + " " + target.posY);
    }
}
