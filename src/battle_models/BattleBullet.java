package battle_models;

import util.BattleConst;
import util.Common;
import util.log.LogUtils;

import java.awt.*;
import java.util.ArrayList;

public class BattleBullet {
    private final String type;
    private final double attackRadius; // cell
    private final int attackArea;
    public BattleMatch match;
    private Point startPoint;
    private boolean active;
    private BattleTroop target;
    private Point destination;
    private double time; // time left to reach destination
    private double totalTime; //total time logic to reach destination
    private double gridSpeed; // cell/s
    private int damagePerShot; // time logic to reach destination
    private double minimumTime = 0; // minimum time to reach destination

    public BattleBullet(String type, Point startPoint, BattleTroop target, int damagePerShot, double attackRadius, int attackArea) {
        this.type = type;
        this.startPoint = startPoint;
        this.damagePerShot = damagePerShot;
        this.attackRadius = attackRadius;
        this.attackArea = attackArea;
        this.gridSpeed = BattleConst.BULLET_GRID_SPEED.get(type);
        this.minimumTime = Common.roundFloat(BattleConst.BULLET_MINIMUM_TIME_REACH_DEST.get(type), 2);

        init(startPoint, target, damagePerShot);
    }

    public void setMatch(BattleMatch match) {
        this.match = match;
    }

    public String getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public BattleTroop getTarget() {
        return target;
    }

    public void setTarget(BattleTroop target) {
        this.target = target;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getGridSpeed() {
        return gridSpeed;
    }

    public void setGridSpeed(double gridSpeed) {
        this.gridSpeed = gridSpeed;
    }

    public int getDamagePerShot() {
        return damagePerShot;
    }

    public void setDamagePerShot(int damagePerShot) {
        this.damagePerShot = damagePerShot;
    }

    public void init(Point startPoint, BattleTroop target, int damagePerShot) {
        this.startPoint = startPoint;
        this.target = target;
        this.damagePerShot = damagePerShot;
        this.destination = new Point(target.posX, target.posY);

        double gridDist = Math.sqrt(Math.pow(startPoint.x - target.posX, 2) + Math.pow(startPoint.y - target.posY, 2));
        gridDist = Common.roundFloat(gridDist, 2);
        this.time = Math.max(Common.roundFloat(gridDist / this.gridSpeed, 2), this.minimumTime);
        this.totalTime = this.time;

        LogUtils.writeLog("def " + this.type + " bullet: time to reach " + this.destination.x + ' ' + this.destination.y + " : " + this.totalTime);

        this.active = true;
    }

    public void gameLoop(double dt) {
        if (!this.active || this.target == null)
            return;
        this.time -= dt;
        if (this.time <= 0) {
            this.onReachDestination();
        }
    }

    public void onReachDestination() {
        if (attackRadius > 0) {
            ArrayList<BattleTroop> listTargets = match.getListTroopsInRange(destination, attackRadius);
            for (BattleTroop target : listTargets) {
                if (checkTarget(target))
                    target.onGainDamage(this.damagePerShot);
            }
        } else {
            if (target != null && target.isAlive()) {
                target.onGainDamage(getDamagePerShot());
            }
        }
        destroyBullet();
    }

    //check if troop can be attakced
    public boolean checkTarget(BattleTroop target) {
        // target in air
        if (target.isOverhead() && this.attackArea == BattleConst.DEF_ATTACK_AREA_GROUND) {
            return false;
        }
        // target on ground
        if (!target.isOverhead() && this.attackArea == BattleConst.DEF_ATTACK_AREA_OVERHEAD) {
            return false;
        }
        return target != null && target.isAlive();
    }

    public void destroyBullet() {
        this.active = false;
    }

    @Override
    public String toString() {
        return "BattleBullet{" +
                "active=" + active +
                ", target=" + target +
                ", destination=" + destination.x + " " + destination.y +
                '}';
    }
}
