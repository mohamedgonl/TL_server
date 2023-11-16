package battle_models;

import util.BattleConst;
import util.GameConfig;
import util.config.TroopBaseConfig;
import util.config.TroopConfig;
import util.log.LogUtils;

import java.awt.*;
import java.util.ArrayList;

public class BattleTroop {
    public int id;
    public String type;
    public int posX;
    public int posY;
    public int hp;
    public int level;
    public TroopBaseConfig baseStats;
    public TroopConfig stats;

    public BattleMatch match;

    private BattleBuilding _target;

    private ArrayList<BattleBuilding> listTarget;

    private int currentIndex;

    private BattleConst.TROOP_STATE state;

    private ArrayList<Point> path;

    private boolean _firstAttack;

    private float _attackCd;

    private boolean isOverhead;

    private int _currentIndexLeft = 0;

    public BattleTroop(String type, int level, int posX, int posY) {
        this.stats = GameConfig.getInstance().troopConfig.get(type).get(level);
        this.baseStats = GameConfig.getInstance().troopBaseConfig.get(type);
        this.hp = this.stats.hitpoints;
        this.level = level;
        this.posX = posX;
        this.posY = posY;
        this._attackCd = this.baseStats.attackSpeed;
        this.isOverhead = type.equals("ARM_6");
        this.type = type;
    }

    public void attack(BattleBuilding building) {

    }

    public void move() {

    }

    public boolean isOverhead() {
        return isOverhead;
    }

    public void setOverhead(boolean overhead) {
        isOverhead = overhead;
    }

    public void gameLoop(int dt) {
        if (this.state == BattleConst.TROOP_STATE.FIND_PATH) {
            this.findTargetandPath();
            return;
        }
        if (this.state == BattleConst.TROOP_STATE.MOVE) {
            this.moveToTarget(dt);
            return;
        }

        if (this.state == BattleConst.TROOP_STATE.ATTACK) {
            this.attackTarget(dt);
            return;
        }
    }

    public void setMatch(BattleMatch match) {
        this.match = match;
    }

    private void attackTarget() {
    }

    private ArrayList<Point> getPathToBuilding(BattleBuilding building) {
        // TODO:
//        let graph = BattleManager.getInstance().getBattleGraph();
//        let start = new  BattleGridNode(this._posX,this._posY,graph.getNode(this._posX,this._posY).weight);
//        let targetCenter = cc.pAdd(building.getGridPosition(),cc.p(building._width/2,building._height/2));
//        //floor
//        targetCenter.x = Math.floor(targetCenter.x);
//        targetCenter.y = Math.floor(targetCenter.y);
//
//        cc.log(JSON.stringify(targetCenter, null, 2));
//        let end = new BattleGridNode(targetCenter.x,targetCenter.y,graph.getNode(targetCenter.x,targetCenter.y).weight);
//        return BattleAStar.search(graph,start,end);
        return new ArrayList<>();
    }

    private void findTargetandPath() {

        listTarget = new ArrayList<>();

        switch (this.baseStats.favoriteTarget) {
            case "DEF":
                ArrayList<BattleBuilding> buildings = new ArrayList<>();
                for (BattleDefence battleDefence : this.match.getListDefences()) {
                    buildings.add(battleDefence);
                }
                listTarget = buildings;
                break;
            case "RES":
                listTarget = this.match.getListResources();
                break;
            case "NONE":

                for (BattleBuilding building : this.match.getBuildings()) {

                    if (building.type.startsWith("OBS")) continue;
                    if (building.type.startsWith("WAL")) continue;

                    listTarget.add(building);
                }
                break;
            default:
                System.out.println("Error::::: NOT FOUND FAVORITE TARGET");
        }

        if (listTarget.isEmpty()) {
            return;
        }

        this.currentIndex = 0;
        double minDistance = Double.MAX_VALUE;
        _target = null;

        for (BattleBuilding target : listTarget) {
            if (target.isDestroy()) continue;

            double distance = Math.sqrt(Math.pow(this.posX - target.posX, 2) + Math.pow(this.posY - target.posY, 2));

            if (minDistance == Double.MAX_VALUE || distance < minDistance) {
                minDistance = distance;
                this._target = target;
            }
        }

        if (this._target == null) {
            System.out.println("Error::::: NOT FOUND TARGET");
            this.state = BattleConst.TROOP_STATE.IDLE;
            return;
        }

        this.state = BattleConst.TROOP_STATE.MOVE;
        this.path = getPathToBuilding(_target);

        for (int i = 0; i < path.size(); i++) {
            Point node = path.get(i);
            BattleBuilding building = this.match.getBattleBuildingByPos(node.x, node.y);

            if (building != null && building.type.startsWith("WAL")) {
                _target = building;
                path = (ArrayList<Point>) path.subList(0, i);
                break;
            }
        }
    }

    public void moveToTarget(float dt) {
        // If target is destroyed, find a new target
        if (this._target.isDestroy()) {
            System.out.println("target destroy");
            this.state = BattleConst.TROOP_STATE.FIND_PATH;
            return;
        }

        // While moving on the path, the building is destroyed
        if (this.path.size() == 0) {
            // System.out.println("path length 0");
            return;
        }

        float distance = dt * this.baseStats.moveSpeed / BattleConst.GRID_BATTLE_RATIO;

        if (this._currentIndexLeft > distance) {
            this._currentIndexLeft -= distance;
        } else {
            this.currentIndex++;

            if (this.currentIndex >= this.path.size()) {
                // On end Path -> attack mode
                System.out.println("end path");
                this._firstAttack = true;
                this.state = BattleConst.TROOP_STATE.ATTACK;
                return;
            }

//            this.posX = this.path.get(this.currentIndex).getX();
//            this.posY = this.path.get(this.currentIndex).getY();
//
//            if (this.path.get(this.currentIndex).getX() != this.path.get(this.currentIndex - 1).getX()
//                    && this.path.get(this.currentIndex).getY() != this.path.get(this.currentIndex - 1).getY()) {
//                this.isCross = false;
//                this.currentIndexLeft = 1.414 - (distance - this.currentIndexLeft);
//                // System.out.println("cross::::, currentIndexLeft: " + this.currentIndexLeft);
//            } else {
//                this.isCross = true;
//                this.currentIndexLeft = 1 - (distance - this.currentIndexLeft);
//                // System.out.println("not cross::::, currentIndexLeft: " + this.currentIndexLeft);
//            }
        }

        // Set position
//        Point posIndexInMap = BattleLayer.getMapPosFromGridPos(this.path.get(this.currentIndex));
//        int prevIndex = (this.currentIndex - 1) > 0 ? (this.currentIndex - 1) : 0;
//        Point posPrevIndexInMap = BattleLayer.getMapPosFromGridPos(this.path.get(prevIndex));
//
//        Point pos;
//        if (this.isCross) {
//            pos = Point.interpolate(posIndexInMap, posPrevIndexInMap, this.currentIndexLeft);
//        } else {
//            pos = Point.interpolate(posIndexInMap, posPrevIndexInMap, this.currentIndexLeft / 1.414);
//        }
//        this.setPosition(pos);
//
//        // Set direction
//        int directX = this.path.get(this.currentIndex).getX() - this.path.get(this.currentIndex - 1).getX();
//        int directY = this.path.get(this.currentIndex).getY() - this.path.get(this.currentIndex - 1).getY();
//        this.setRunDirection(directX, directY);
    }

    private void attackTarget(int dt) {
        if (this._target.isDestroy()) {
            System.out.println("target destroy");
            this.state = BattleConst.TROOP_STATE.FIND_PATH;
            return;
        }
        //perform attack
        if (this._firstAttack && this._attackCd >= this.baseStats.attackSpeed / 2) {
            this._firstAttack = false;
        }
        if (this._attackCd == 0) {
            this._attackCd = this.baseStats.attackSpeed;
            this._target.onGainDamage(this.stats.damagePerAttack);
        } else {
            this._attackCd -= dt;
            if (this._attackCd < 0)
                this._attackCd = 0;
        }
    }

    public void onGainDamage(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            this.dead();
        }

        LogUtils.writeLog("troop " + this.type + " gain " + damage + " ~ " + this.hp);

    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public void dead() {
        this.match.removeTroop(this);

        LogUtils.writeLog("troop " + this.type + " dead");

    }

    @Override
    public String toString() {
        return "BattleTroop{" +
                ", type='" + type + '\'' +
                ", posX=" + posX +
                ", posY=" + posY +
                ", hp=" + hp +
                ", level=" + level +
                ", currentIndex=" + currentIndex +
//                ", state=" + state +
                ", _firstAttack=" + _firstAttack +
                ", _attackCd=" + _attackCd +
                ", isOverhead=" + isOverhead +
//                ", _currentIndexLeft=" + _currentIndexLeft +
                '}';
    }
}
