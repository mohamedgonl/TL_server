package battle_models;

import util.BattleConst;
import util.GameConfig;
import util.algorithms.BattleAStar;
import util.algorithms.BattleGraph;
import util.algorithms.BattleGridNode;
import util.config.TroopBaseConfig;
import util.config.TroopConfig;
import util.log.LogUtils;

import java.awt.*;
import java.util.ArrayList;

import static util.BattleConst.TROOP_SPEED_RATIO;

public class BattleTroop {
    //Battle Manager
    public BattleMatch match;

    public int id;
    public String type;
    public int posX;
    public int posY;
    public String favoriteTarget;
    public int moveSpeed;
    public float attackSpeed;
    public int damage;
    public int hitpoints;
    public double attackRange;
    public double damageScale;
    public boolean isOverhead;
    public int currentHitpoints;
    public BattleBuilding target;

    //state 5 type: FIND, MOVE, ATTACK, IDLE, DEAD
    public enum TROOP_STATE {
        FIND, MOVE, ATTACK, IDLE, DEAD
    }

    public TROOP_STATE state;
    public ArrayList<Point> path;

    public float attackCd;
    public boolean firstAttack;

    public TroopConfig stats;
    public TroopBaseConfig baseStats;

    private boolean isFirstMove;

    private int nextIndex;

    private double nextIndexDistanceLeft = 0;


    public BattleTroop(String type, int level, int posX, int posY) {

        this.stats = GameConfig.getInstance().troopConfig.get(type).get(level);
        this.baseStats = GameConfig.getInstance().troopBaseConfig.get(type);

        this.posX = posX;
        this.posY = posY;
        this.favoriteTarget = this.baseStats.favoriteTarget;
        this.moveSpeed = this.baseStats.moveSpeed;
        this.attackSpeed = this.baseStats.attackSpeed;
        this.damage = this.stats.damagePerAttack;
        this.hitpoints = this.stats.hitpoints;
        this.attackRange = this.baseStats.attackRange;
        this.damageScale = this.baseStats.dmgScale;
        this.isOverhead = type.equals("ARM_6");
        this.currentHitpoints = this.hitpoints;
        this.target = null;
        this.state = TROOP_STATE.FIND;
        this.path = new ArrayList<>();
        this.attackCd = this.attackSpeed;
        this.firstAttack = true;
    }

    public void setMatch(BattleMatch match) {
        this.match = match;
    }

    public void gameLoop(int dt) {
        if (this.state == TROOP_STATE.FIND) {
            this.findTarget();
            this.findPath();
            this.checkPath();


            //change weight of grid in path +1 for various path each troop
            //TODO : battle manager get battle graph
            BattleGraph graph = this.match.getBattleGraph();
            for (Point point : this.path) {
                int x = (int) point.getX();
                int y = (int) point.getY();
                graph.changeNodeWeight(x, y, graph.getNode(x, y).weight + 1);
            }

            //if not found target, return in Java
            if (this.target == null) {
                return;
            }

            //attack case
            if (this.isInAttackRange(this.target)) {
                this.state = TROOP_STATE.ATTACK;
                this.firstAttack = true;
            }

            //move case
            else {
                this.state = TROOP_STATE.MOVE;
                this.isFirstMove = true;
            }
            return;
        }

        if (this.state == TROOP_STATE.MOVE) {
            this.moveLoop(dt);
            return;
        }

        if (this.state == TROOP_STATE.ATTACK) {
            this.attackLoop(dt);
        }

    }

    private boolean isInAttackRange(BattleBuilding target) {
        return this.isInAttackRange(target, null, null);
    }

    private ArrayList<Point> getPathToBuilding(BattleBuilding building) {
        //get path
        BattleGraph graph = this.match.getBattleGraph();
        BattleGridNode start = new BattleGridNode(this.posX, this.posY, graph.getNode(this.posX, this.posY).weight, null);

        //get center of building
        int targetCenterX = building.posX + (building.width / 2);
        int targetCenterY = building.posY + (building.height / 2);

        BattleGridNode end = new BattleGridNode(targetCenterX, targetCenterY, graph.getNode(targetCenterX, targetCenterY).weight, building.id);
        ArrayList<BattleGridNode> path = BattleAStar.search(graph, start, end);

        //change to Point
        ArrayList<Point> ret = new ArrayList<>();
        for (BattleGridNode node : path) {
            ret.add(new Point(node.x, node.y));
        }
        return ret;
    }

    //check if troop in attack range of building,
    // normal case : troop.posX, troop.posY,
    // else : tempX, tempY

    private boolean isInAttackRange(BattleBuilding building, Integer tempX, Integer tempY) {
        ArrayList<Point> corners = building.getCorners();

        int xStart = corners.get(0).x;
        int xEnd = corners.get(1).x;
        int yStart = corners.get(0).y;
        int yEnd = corners.get(2).y;

        int x = tempX;
        int y = tempY;

        //if X and Y in range of building
        if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) {
            return true;
        }

        //if X or Y in range of building
        if (x >= xStart && x <= xEnd) {

            return (Math.abs(y - yStart) <= this.attackRange || Math.abs(yEnd - y) <= this.attackRange);
        }
        if (y >= yStart && y <= yEnd) {

            return (Math.abs(x - xStart) <= this.attackRange || Math.abs(xEnd - x) <= this.attackRange);
        }

        //if X and Y not in range of building, get nearest corner by if else
        int xNearest = 0;
        int yNearest = 0;

        if (x < xStart) xNearest = xStart;
        else if (x > xEnd) xNearest = xEnd;
        else xNearest = x;

        if (y < yStart) yNearest = yStart;
        else if (y > yEnd) yNearest = yEnd;
        else yNearest = y;

        //if distance from nearest corner to troop < attack range
        double distance = Math.sqrt(Math.pow(xNearest - x, 2) + Math.pow(yNearest - y, 2));

        return distance <= this.attackRange;
    }

    private void findTarget() {
        ArrayList<BattleBuilding> listTarget = new ArrayList<>();
        switch (this.favoriteTarget) {
            case "DEF":
                listTarget = this.match.getListDefences();
                break;
            case "RES":
                listTarget = this.match.getListResources();
                break;
            case "NONE":
                ArrayList<BattleBuilding> mapListBuilding = this.match.getAllBuilding();
                for (BattleBuilding building : mapListBuilding) {
                    if (building.isDestroy()) continue;
                    if (building.type.startsWith("WAL")) continue;
                    if (building.type.startsWith("RES")) continue;
                    listTarget.add(building);
                }
                break;
        }

        //if not have favourite target, change to NONE and find again
        if (listTarget.isEmpty()) {
            this.favoriteTarget = "NONE";
            this.findTarget();
            return;
        }

        //get min distance target
        Double minDistance = null;
        this.target = null;
        for (int i = 0; i < listTarget.size(); i++) {

            BattleBuilding target = listTarget.get(i);

            //if destroy, continue
            if (target.isDestroy()) continue;
            //get min distance
            double distance = Math.sqrt(Math.pow(this.posX - target.posX, 2) + Math.pow(this.posY - target.posY, 2));
            if (minDistance == null || distance < minDistance) {
                minDistance = distance;
                this.target = target;
            }
        }

        if (this.target == null) {
            //if no building left, change to idle
            if (this.favoriteTarget == "NONE") {
                this.state = TROOP_STATE.IDLE;
                return;
            }
            //if no favorite target, change to find all building and find again
            else {
                this.favoriteTarget = "NONE";
                this.state = TROOP_STATE.FIND;
                this.findTarget();
                return;
            }
        }
    }

    private void findPath() {
        this.path = this.getPathToBuilding(this.target);
    }

    //check that path is valid or not
    //not valid when path go through WAL before in attack range -> change target to WAL and update path

    private void checkPath() {
        for (int i = 0; i < this.path.size(); i++) {
            int x = this.path.get(i).x;
            int y = this.path.get(i).y;

            // if path go through WAL, this.target = WAL
            BattleBuilding building = this.match.getBattleBuildingByPos(x, y);
            if (building != null && building.type.startsWith("WAL")) {
                this.target = building;
                //update this.path = path from 0 to i
                this.path = new ArrayList<>(this.path.subList(0, i + 1));
                return;
            }

            //if (x,y) is in range attack, path is valid, return;
            if (this.isInAttackRange(this.target, x, y)) {
                return;
            }
        }
    }

    private void moveLoop(double dt) {

        //if target destroy, find new target
        if (this.target.isDestroy()) {
            this.state = TROOP_STATE.FIND;
            return;
        }
        if (this.path.isEmpty()) {
            this.state = TROOP_STATE.ATTACK;
            return;
        }

        //perform run animation by direction
        boolean isCross = false;
        if (this.isFirstMove) {
            this.nextIndex = 0;

            //current index distance left = 1 if not cross, 1.414 if cross
            if (this.path.get(this.nextIndex).x != this.posX && this.path.get(this.nextIndex).y != this.posY) {
                this.nextIndexDistanceLeft = 1.414;
            } else {
                isCross = true;
                this.nextIndexDistanceLeft = 1;
            }
            this.isFirstMove = false;
        }


        //distance moved each dt
        double distance = dt * this.moveSpeed * TROOP_SPEED_RATIO;

        //if move in this grid, not ++ currentIndex
        if (this.nextIndexDistanceLeft > distance) {
            this.nextIndexDistanceLeft -= distance;
        }

        //if move to next index of path
        else {
            this.nextIndex++;
            if (this.nextIndex >= this.path.size()) {
                this.state = TROOP_STATE.ATTACK;
                this.firstAttack = true;
                return;
            }
            if (this.isInAttackRange(this.target)) {
                //on end Path -> attack mode
                this.state = TROOP_STATE.ATTACK;
                this.firstAttack = true;
                return;
            }


            Point nextPos = this.path.get(this.nextIndex);

            //nếu chéo, = 1.414, else this.nextIndexDistanceLeft = 1
            if (nextPos.x != this.posX && nextPos.y != this.posY) {
                isCross = false;
                this.nextIndexDistanceLeft = 1.414 - (distance - this.nextIndexDistanceLeft);
            } else {
                isCross = true;
                this.nextIndexDistanceLeft = 1 - (distance - this.nextIndexDistanceLeft);
            }

            // set posX, y is currentPos
            this.posX = this.path.get(this.nextIndex - 1).x;
            this.posY = this.path.get(this.nextIndex - 1).y;
        }

    }

    private void attackLoop(double dt) {

        if (this.target.isDestroy()) {
            this.state = TROOP_STATE.FIND;
            return;
        }

        if (this.firstAttack) {
            if (this.target.type.startsWith("WAL")) {
                this.target.addTroopAttack(this);
            }
            this.firstAttack = false;
        }
        if (this.attackCd == 0) {
            this.attackCd = this.attackSpeed;
            this.attack();
        } else {
            this.attackCd -= (float) dt;
            if (this.attackCd < 0) {
                this.attackCd = 0;
            }
        }
    }

    private void attack() {
        int damage = this.damage;

        //if target is favorite target, damage *= damageScale
        if (this.target.type.startsWith(this.favoriteTarget)) {
            damage = (int) ((double) damage * this.damageScale);
        }
        this.target.onGainDamage(damage);
    }

    //call when troop gain damage, update hp bar, if hp = 0, call dead
    private void onGainDamage(int damage) {
        this.currentHitpoints -= damage;
        if (this.currentHitpoints <= 0) {
            this.dead();
        }

        LogUtils.writeLog("troop " + this.type + " gain " + damage + " ~ " + this.currentHitpoints);

    }

    public boolean isAlive() {
        return this.currentHitpoints > 0;
    }
    //create sprite of troop with shadow, body, hp bar
    public void refindTarget() {
        this.state = TROOP_STATE.FIND;
    }
    public boolean isOverhead() {
        return isOverhead;
    }

    public void dead() {
        this.match.removeTroop(this);
        this.state = TROOP_STATE.DEAD;
        LogUtils.writeLog("troop " + this.type + " dead");
    }

}


