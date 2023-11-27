package battle_models;


import util.Common;

import java.awt.*;

public class TroopBullet {

    public BattleBuilding target;

    public BattleMatch match;
    public int damage;
    public Point startPoint;
    public Point endPoint;

    public double startTime;
    public double endTime;
    public double currentTime;
    public boolean active;
    public int speedPerSec = 20;
    public BattleTroop troop;


    TroopBullet(BattleBuilding target, Point startPoint, int damage, int speedPerSec, BattleTroop troop) {
        this.target = target;
        this.damage = damage;
        this.startPoint = startPoint;
        this.active = true;
        this.startTime = 0;
        this.currentTime = 0;
        this.troop= troop;
        this.endPoint = new Point (target.posX+ (int) (target.width / 2),
                target.posY + (int) (target.height/2));

        //calculate distance from start to end point
        double distance = Math.sqrt(Math.pow(startPoint.x - endPoint.x, 2)
                                    + Math.pow(startPoint.y - endPoint.y, 2));
        distance = Common.roundFloat(distance, 4);
        //calculate time to reach end point
        this.endTime = (float) (distance /this.speedPerSec);
        this.endTime = Common.roundFloat(this.endTime, 4);
    }
    private void onReachTarget(){
        this.active = false;
        this.target.onGainDamage(this.damage, this.troop);
    }
    public void setMatch(BattleMatch match) {
        this.match = match;
    }

    public void gameLoop(double dt){
        if (!this.active) return;
        this.currentTime += dt;
        if (this.currentTime >= this.endTime){
            this.onReachTarget();
        }
    }
    public static void createBullet(BattleMatch match,String type, BattleBuilding target, Point startPoint, int damage,BattleTroop troop){
        TroopBullet bullet = null;
        switch (type) {
            case "ARM_2":
                bullet = new ArcherBullet(target, startPoint, damage, troop);
                break;
        }
        //add bullet to battle manager
        if(bullet != null)
            match.addTroopBullet(bullet);
    }

}