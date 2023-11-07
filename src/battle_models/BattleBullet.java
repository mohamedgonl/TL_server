package battle_models;

import java.awt.*;

public class BattleBullet {
    private final String type;
    private final Point startPoint;
    private boolean active;
    private BattleTroop target;
    private double time; // time logic to reach destination
    private double gridSpeed; // time logic to reach destination
    private int damagePerShot; // time logic to reach destination

    public BattleBullet(Point startPoint, BattleTroop target, int damagePerShot, String type, double gridSpeed) {
        this.active = true;
        this.startPoint = startPoint;
        this.damagePerShot = damagePerShot;
        this.type = type;
        this.gridSpeed = gridSpeed;

        double gridDist = Math.sqrt(Math.pow(startPoint.x - target.posX, 2) + Math.pow(startPoint.y - target.posY, 2));
        this.time = gridDist / this.gridSpeed;
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

    public void reset(BattleTroop target) {
        this.target = target;
        double gridDist = Math.sqrt(Math.pow(startPoint.x - target.posX, 2) + Math.pow(startPoint.y - target.posY, 2));
        this.time = gridDist / this.gridSpeed;
        this.active = true;
    }

    public void gameLoop(double dt) {
        // cc.log(JSON.stringify({active: this.active, destination: this.destination, x: this.x, y: this.y}))
        if (!this.active || this.target == null)
            return;
        this.time -= dt;
        if (this.time <= 0) {
            this.onReachDestination();
        }
    }

    public void onReachDestination() {

    }

    public void destroyBullet() {
        this.active = false;
    }
}
