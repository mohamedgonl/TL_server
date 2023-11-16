package battle_models;

import java.awt.*;
import java.util.ArrayList;

public class BattleBullet {
    private final String type;
    private final double attackRadius; // cell
    public BattleMatch match;

    public void setMatch(BattleMatch match) {
        this.match = match;
    }

    private Point startPoint;
    private boolean active;
    private BattleTroop target;
    private Point destination;
    private double time; // time left to reach destination
    private double totalTime; //total time logic to reach destination
    private double gridSpeed; // cell/s
    private int damagePerShot; // time logic to reach destination
    private double minimumTime = 0; // minimum time to reach destination

    public BattleBullet(String type, Point startPoint, BattleTroop target, int damagePerShot, double attackRadius) {
        this.type = type;
        this.startPoint = startPoint;
        this.damagePerShot = damagePerShot;
        this.attackRadius = attackRadius;

        if (type.equals("DEF_1")) {
            this.gridSpeed = 40;
        } else if (type.equals("DEF_2")) {
            this.gridSpeed = 50;
            minimumTime = 15 / this.gridSpeed;
        } else if (type.equals("DEF_3")) {
            this.gridSpeed = 13;
        }
        init(startPoint, target);
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

    public void init(Point startPoint, BattleTroop target) {
        this.startPoint = startPoint;
        this.target = target;
        this.destination = new Point(target.posX, target.posY);

        double gridDist = Math.sqrt(Math.pow(startPoint.x - target.posX, 2) + Math.pow(startPoint.y - target.posY, 2));
        this.time = Math.max(gridDist / this.gridSpeed, this.minimumTime);
        this.totalTime = this.time;

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
                if (target != null && target.isAlive())
                    target.onGainDamage(this.damagePerShot);
            }
        } else {
            if (target != null && target.isAlive()) {
                target.onGainDamage(getDamagePerShot());
            }
        }
        destroyBullet();
    }

    public void destroyBullet() {
        this.active = false;
    }

    @Override
    public String toString() {
        return "BattleBullet{" +
                "active=" + active +
                ", target=" + target +
                ", destination=" + destination.x +" " + destination.y+
                '}';
    }
}
