package battle_models;

import util.BattleConst;
import util.Common;
import util.GameConfig;
import util.algorithms.BattleAStar;
import util.algorithms.BattleGraph;
import util.algorithms.BattleGridNode;
import util.config.TroopBaseConfig;
import util.config.TroopConfig;
import util.log.LogUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

import static util.BattleConst.TROOP_SPEED_RATIO;

public class BattleTroop
{
    //Battle Manager
    public BattleMatch match;

    public String type;
    public int posX;
    public int posY;
    public String favoriteTarget;
    public int moveSpeed;
    public double attackSpeed;
    public int damage;
    public int hitpoints;
    public double attackRange;
    public int damageScale;
    public boolean isOverhead;
    public int currentHitpoints;
    public BattleBuilding target;
    public TROOP_STATE state;
    public ArrayList<Point> path;
    public double attackCd;
    public boolean firstAttack;
    public TroopConfig stats;
    public TroopBaseConfig baseStats;
    private boolean isFirstMove;
    private int nextIndex;
    private double nextIndexDistanceLeft = 0;
    private double dtCount;
    private double timeAttackAnimationHit;

    public int id;

    public BattleTroop(String type, int level, int posX, int posY)
    {

        this.stats = GameConfig.getInstance().troopConfig.get(type).get(level);
        this.baseStats = GameConfig.getInstance().troopBaseConfig.get(type);
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.favoriteTarget = this.baseStats.favoriteTarget;
        this.moveSpeed = this.baseStats.moveSpeed * BattleConst.BATTLE_MAP_SCALE;
        this.attackSpeed = this.baseStats.attackSpeed;
        this.damage = this.stats.damagePerAttack;
        this.hitpoints = this.stats.hitpoints;
        this.attackRange = this.baseStats.attackRange * BattleConst.BATTLE_MAP_SCALE;
        this.damageScale = (int) this.baseStats.dmgScale;
        this.isOverhead = type.equals("ARM_6");
        this.currentHitpoints = this.hitpoints;
        this.target = null;
        this.state = TROOP_STATE.FIND;
        this.path = new ArrayList<>();
        this.attackCd = this.attackSpeed;
        this.firstAttack = true;
        this.dtCount = 0;
        switch (type)
        {
            case "ARM_1":
                this.timeAttackAnimationHit = 1;
                break;
            case "ARM_2":
                this.timeAttackAnimationHit = 0.7;
                break;
            case "ARM_3":
                this.timeAttackAnimationHit = 0.5;
                break;
            case "ARM_4":
                this.timeAttackAnimationHit = 0.6;
                break;
            case "ARM_6":
                this.timeAttackAnimationHit = 0.83;
                break;
        }

//        for(int i = 0; i<132;i++)
//            for(int j = 0; j<132;j++){
//                int id = this.match.getBattleMap()[i][j];
//                if(id == 0) continue;
//                LogUtils.writeLog("map: " + i + " " + j + " " + id);
//            }


    }
    public void setId(int id)
    {
        this.id = id;
    }

    public void setMatch(BattleMatch match)
    {
        this.match = match;
    }

    public void gameLoop(double dt)
    {
        this.dtCount += dt;

        if (this.state == TROOP_STATE.FIND)
        {
            this.findTarget();

            if (this.target == null) return;
            LogUtils.writeLog("troop ID:" +this.id);
            LogUtils.writeLog("1 :troop " + this.type + " find target " + this.target.type);
            this.findPath();
            LogUtils.writeLog("2 :troop " + this.type + " find target " + this.target.type);
            for (Point point : this.path)
            {
                LogUtils.writeLog("path: " + point.x + " " + point.y);
            }
            this.checkPath();
            LogUtils.writeLog("3 :troop " + this.type + " find target " + this.target.type);


            //if not found target, return in Java
            if (this.target == null)
            {
                return;
            }

            //attack case
            if (this.isInAttackRange(this.target))
            {
                this.state = TROOP_STATE.ATTACK;
                this.firstAttack = true;
            }

            //move case
            else
            {
                this.state = TROOP_STATE.MOVE;
                this.isFirstMove = true;
            }

            LogUtils.writeLog("troop " + this.type +
                    " find target " + this.target.type +
                    "length path: " + this.path.size());
            LogUtils.writeLog("target pos: " + this.target.posX + " " + this.target.posY +
                    "width: " + this.target.width + " height: " + this.target.height);
            for (Point point : this.path)
            {
                LogUtils.writeLog("path: " + point.x + " " + point.y);
            }
            return;
        }

        if (this.state == TROOP_STATE.MOVE)
        {

            this.moveLoop(dt);
            return;
        }

        if (this.state == TROOP_STATE.ATTACK)
        {
            this.attackLoop(dt);
        }

    }

    private boolean isInAttackRange(BattleBuilding target)
    {
        return this.isInAttackRange(target, null, null);
    }

    private ArrayList<Point> getPathToBuilding(BattleBuilding building)
    {
        try
        {
            //get path
            BattleGraph graph = this.match.getBattleGraph();
            BattleGridNode start = new BattleGridNode(this.posX, this.posY, graph.getNode(this.posX, this.posY).weight, null);


            Point nearestPoint = building.getNearestPoint(this.posX, this.posY,this.id,true);

            BattleGridNode end = new BattleGridNode(
                    nearestPoint.x, nearestPoint.y,
                    graph.getNode(nearestPoint.x, nearestPoint.y).weight, building.id);
            ArrayList<BattleGridNode> path = BattleAStar.search(graph, start, end);

            //change to Point
            ArrayList<Point> ret = new ArrayList<>();
            if (path == null)
                return ret;
            for (BattleGridNode node : path)
            {
                ret.add(new Point(node.x, node.y));
            }
            return ret;
        } catch (Exception exception)
        {
            exception.printStackTrace();
            System.out.println(exception.getMessage());
        }
        return null;
    }

    private boolean isInAttackRange(BattleBuilding building, Integer tempX, Integer tempY)
    {
        ArrayList<Point> corners = building.getCorners();

        int xStart = corners.get(0).x;
        int xEnd = corners.get(1).x;
        int yStart = corners.get(0).y;
        int yEnd = corners.get(2).y;


        int x = tempX == null ? this.posX : tempX;
        int y = tempY == null ? this.posY : tempY;


        //if X and Y in range of building
        if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd)
        {
            return true;
        }

        //if X or Y in range of building
        if (x >= xStart && x <= xEnd)
        {

            return (Math.abs(y - yStart) <= this.attackRange || Math.abs(yEnd - y) <= this.attackRange);
        }
        if (y >= yStart && y <= yEnd)
        {

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
        distance = Common.roundFloat(distance, 4);

        return distance <= this.attackRange;
    }

    //check if troop in attack range of building,
    // normal case : troop.posX, troop.posY,
    // else : tempX, tempY

    private void findTarget()
    {
        ArrayList<BattleBuilding> listTarget = new ArrayList<>();
        switch (this.favoriteTarget)
        {
            case "DEF":
                ArrayList<BattleDefence> list = this.match.getListDefences();
                //change to BattleBuilding
                listTarget.addAll(list);
                break;
            case "RES":
                listTarget = this.match.getListResources();
                break;
            case "NONE":
                ArrayList<BattleBuilding> mapListBuilding = this.match.getBuildings();
                for (BattleBuilding building : mapListBuilding)
                {
                    if (building.isDestroy()) continue;
                    if (building.type.startsWith("WAL")) continue;
                    if (building.type.startsWith("OBS")) continue;
                    listTarget.add(building);
                }
                break;
        }

        //if not have favourite target, change to NONE and find again
        if (listTarget.isEmpty())
        {
            if (!Objects.equals(this.favoriteTarget, "NONE"))
            {
                this.favoriteTarget = "NONE";
                this.findTarget();
            } else
            {
                this.state = TROOP_STATE.IDLE;
            }
            return;
        }

        //get min distance target
        Integer minDistanceSquare = null;
        this.target = null;
        for (BattleBuilding target : listTarget)
        {

            //if destroy, continue
            if (target.isDestroy()) continue;
            //get min distance
            Point nearestPoint = target.getNearestPoint(this.posX, this.posY,null,false);
            int distanceSquare = (nearestPoint.x - this.posX) * (nearestPoint.x - this.posX) +
                    (nearestPoint.y - this.posY) * (nearestPoint.y - this.posY);

            if (minDistanceSquare == null || distanceSquare < minDistanceSquare)
            {
                minDistanceSquare = distanceSquare;
                this.target = target;
            }
        }

        if (this.target == null)
        {
            //if no building left, change to idle
            if (Objects.equals(this.favoriteTarget, "NONE"))
            {
                this.state = TROOP_STATE.IDLE;
            }
            //if no favorite target, change to find all building and find again
            else
            {
                this.favoriteTarget = "NONE";
                this.state = TROOP_STATE.FIND;
                this.findTarget();
            }
            return;
        }
    }

    private void findPath()
    {
        this.path = this.getPathToBuilding(this.target);
    }

    private void checkPath()
    {
        for (int i = 0; i < this.path.size(); i++)
        {
            int x = this.path.get(i).x;
            int y = this.path.get(i).y;

            // if path go through WAL, this.target = WAL
            BattleBuilding building = this.match.getBattleBuildingByPos(x, y);
            if (building != null)
            {
                LogUtils.writeLog("building: " + building.type);
                LogUtils.writeLog("x , y" + x + " " + y);
                LogUtils.writeLog("building pos: " + building.posX + " " + building.posY);
                LogUtils.writeLog("building width: " + building.width + " height: " + building.height);
            }
            if (building != null && building.type.startsWith("WAL"))
            {
                LogUtils.writeLog("troop " + this.type + " change target to " + building.type);
                this.target = building;
                //update this.path = path from 0 to i
                this.path = new ArrayList<>(this.path.subList(0, i));
                return;
            }

            //if (x,y) is in range attack, path is valid, return;
            if (this.isInAttackRange(this.target, x, y))
            {
                return;
            }
        }
    }

    //check that path is valid or not
    //not valid when path go through WAL before in attack range -> change target to WAL and update path

    private void moveLoop(double dt)
    {

        //if target destroy, find new target
        if (this.target.isDestroy())
        {
            this.state = TROOP_STATE.FIND;
            return;
        }
        if (this.path.isEmpty())
        {
            this.state = TROOP_STATE.ATTACK;
            return;
        }

        //perform run animation by direction
        if (this.isFirstMove)
        {
            this.nextIndex = 0;

            //current index distance left = 1 if not cross, 1.414 if cross
            if (this.path.get(this.nextIndex).x != this.posX &&
                    this.path.get(this.nextIndex).y != this.posY)
            {
                this.nextIndexDistanceLeft = 1.414;
            } else
            {
                this.nextIndexDistanceLeft = 1;
            }
            this.isFirstMove = false;
        }


        //distance moved each dt
        double distance = Common.roundFloat(dt * this.moveSpeed * TROOP_SPEED_RATIO, 4);

        //if move in this grid, not ++ currentIndex
        if (this.nextIndexDistanceLeft > distance)
        {
            this.nextIndexDistanceLeft = Common.roundFloat(this.nextIndexDistanceLeft - distance, 4);
        }

        //if move to next index of path
        else
        {
            this.nextIndex++;
            if (this.nextIndex >= this.path.size())
            {
                this.state = TROOP_STATE.ATTACK;
                this.firstAttack = true;
                return;
            }
            if (this.isInAttackRange(this.target))
            {
                //on end Path -> attack mode
                this.state = TROOP_STATE.ATTACK;
                this.firstAttack = true;
                return;
            }


            Point nextPos = this.path.get(this.nextIndex);

            //nếu chéo, = 1.414, else this.nextIndexDistanceLeft = 1
            if (nextPos.x != this.posX && nextPos.y != this.posY)
            {
                this.nextIndexDistanceLeft = 1.414 - (distance - this.nextIndexDistanceLeft);
            } else
            {

                this.nextIndexDistanceLeft = 1 - (distance - this.nextIndexDistanceLeft);
            }
            this.nextIndexDistanceLeft = Common.roundFloat(this.nextIndexDistanceLeft, 4);

            // set posX, y is currentPos
            this.posX = this.path.get(this.nextIndex - 1).x;
            this.posY = this.path.get(this.nextIndex - 1).y;
            LogUtils.writeLog("troop " + this.type + " move to next index" + this.posX + " " + this.posY + " dt:" + this.dtCount);
        }

    }

    private void attackLoop(double dt)
    {

        if (this.target.isDestroy())
        {
            this.state = TROOP_STATE.FIND;
            return;
        }

        if (this.firstAttack)
        {
            this.attackCd = this.timeAttackAnimationHit;
            this.attackCd = Common.roundFloat(this.attackCd, 4);
            this.firstAttack = false;
        }

        //loop attack cd
        if (this.attackCd == 0)
        {
            this.attackCd = this.attackSpeed;
            this.attackCd = Common.roundFloat(this.attackCd, 4);
            LogUtils.writeLog("troop" + this.type + " attack" + this.target.type + " dtCount" + this.dtCount);
            this.attack();
            LogUtils.writeLog("troop" + this.type + " attacked" + this.target.type + " dtCount" + this.dtCount);
        } else
        {
            this.attackCd -= dt;
            this.attackCd = Common.roundFloat(this.attackCd, 4);
            if (this.attackCd < 0)
            {
                this.attackCd = 0;
            }
        }
    }

    public void attack()
    {
        if (Objects.equals(this.type, "ARM_2"))
        {
            TroopBullet.createBullet(this.match, "ARM_2", this.target, new Point(this.posX, this.posY), this.damage);
            return;
        }
        int damage = this.damage;

        //if target is favorite target, damage *= damageScale
        if (this.target.type.startsWith(this.favoriteTarget))
        {
            damage = damage * this.damageScale;
        }
        this.target.onGainDamage(damage);
    }

    //call when troop gain damage, update hp bar, if hp = 0, call dead
    public void onGainDamage(int damage)
    {
        this.currentHitpoints -= damage;
        if (this.currentHitpoints <= 0)
        {
            this.dead();
        }

        LogUtils.writeLog("troop " + this.type + " gain " + damage + " ~ " + this.currentHitpoints);

    }

    public boolean isAlive()
    {
        return this.currentHitpoints > 0;
    }

    //create sprite of troop with shadow, body, hp bar
    public void refindTarget()
    {
        this.state = TROOP_STATE.FIND;
    }

    public boolean isOverhead()
    {
        return isOverhead;
    }

    public void dead()
    {
        this.match.removeTroop(this);
        this.state = TROOP_STATE.DEAD;
        LogUtils.writeLog("troop " + this.type + " dead");
    }

    @Override
    public String toString()
    {
        return "BattleTroop{" +
                "type='" + type + '\'' +
                ", posX=" + posX +
                ", posY=" + posY +
                ", favoriteTarget='" + favoriteTarget + '\'' +
                ", currentHitpoints=" + currentHitpoints +
                '}';
    }

    //state 5 type: FIND, MOVE, ATTACK, IDLE, DEAD
    public enum TROOP_STATE
    {
        FIND, MOVE, ATTACK, IDLE, DEAD
    }
}


