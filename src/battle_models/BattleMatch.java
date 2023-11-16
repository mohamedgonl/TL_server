package battle_models;

import org.apache.commons.logging.Log;
import util.BattleConst;
import util.Common;
import util.database.DataModel;
import util.log.LogUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class BattleMatch extends DataModel {
    private final ArrayList<BattleAction> actionsList = new ArrayList<>();
    private final transient int[][] battleMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];
    private final transient double secPerTick = Math.round(1.0 / BattleConst.TICK_PER_SECOND * 1e6) / 1e6;
    private final transient int[][] troopMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];
    private final transient int[][] throwTroopMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];
    private final transient ArrayList<BattleTroop> troops = new ArrayList<>(); // Lưu thông tin từng con lính trong trận
    public int id;
    public int enemyId;
    public String enemyName;
    public int state;
    public int trophy;
    public int winTrophy;
    public int loseTrophy;
    public int createTime; // thời điểm tạo trận
    public int startTime;
    public Map<String, Integer> army; // Lượng lính mang đi đánh
    public Map<String, Integer> usedArmy;
    public int maxGold;
    public int maxElixir;
    public boolean isWin;
    public int winPercentage = 0;
    public int stars;
    public ArrayList<BattleGameObject> listGameObjects;
    public ArrayList<BattleObstacle> listObstacles = new ArrayList<>();
    public ArrayList<BattleBuilding> buildings = new ArrayList<>();
    public transient ArrayList<BattleDefence> listDefences = new ArrayList<>();
    public transient ArrayList<BattleBuilding> listResources = new ArrayList<>();
    public transient ArrayList<BattleBuilding> listWalls = new ArrayList<>();
    public transient ArrayList<BattleBullet> bullets = new ArrayList<>();
    private int goldGot = 0; // vàng chiếm dc
    private int elixirGot = 0; // dầu chiếm dc
    private transient int buildingDestroyedPoint = 0;
    private transient int totalBuildingPoint = 0;

    private transient boolean isDestroyedHalf = false;


    public BattleMatch(int id, int enemyId, String enemyName, ArrayList<BattleGameObject> gameObjects, Map<String, Integer> army, int maxGold, int maxElixir, int enemyRank, int userRank) {
        this.id = id;
        this.enemyId = enemyId;
        this.enemyName = enemyName;
        this.maxGold = maxGold;
        this.maxElixir = maxElixir;
        this.army = army;
        this.listGameObjects = gameObjects;
        this.state = BattleConst.MATCH_NEW;
        this.createTime = Common.currentTimeInSecond();

        this.winTrophy = this.getWinTrophy(enemyRank, userRank);
        this.loseTrophy = this.getLoseTrophy(enemyRank, userRank);

    }

    // must be call when sync
    public void initData() {
        this.initBattleBuildings();
        this.setResourceToBuilding();
        this.initBattleMap();
        this.initThrowTroopMap();
    }

    public int getWinTrophy(int userRank, int enemyRank) {
        return (int) Math.floor(-0.63599 + (59.43467 + 0.63599) / (1 + 0.991798 * Math.exp(0.00576 * (userRank - enemyRank))));
    }

    public int getLoseTrophy(int userRank, int enemyRank) {
        return (int) Math.floor(39.0907 - (39.0619) / (1 + 0.993 * Math.exp(0.00595 * (userRank - enemyRank))));
    }

    public int getGoldGot() {
        return this.goldGot;
    }

    public void setGoldGot(int goldGot) {
        this.goldGot = goldGot;
    }

    public int getElixirGot() {
        return this.elixirGot;
    }

    public void setElixirGot(int elixirGot) {
        this.elixirGot = elixirGot;
    }

    @Override
    public String toString() {
        return "BattleMatch{" +
                "id=" + id +
                ", enemyId=" + enemyId +
                ", enemyName='" + enemyName + '\'' +
                ", state=" + state +
                ", trophy=" + trophy +
                ", winTrophy=" + winTrophy +
                ", loseTrophy=" + loseTrophy +
                ", createTime=" + createTime +
                ", startTime=" + startTime +
                ", army=" + army +
                ", usedArmy=" + usedArmy +
                ", maxGold=" + maxGold +
                ", maxElixir=" + maxElixir +
                ", goldGot=" + goldGot +
                ", elixirGot=" + elixirGot +
                ", isWin=" + isWin +
                ", winPercentage=" + winPercentage +
                ", stars=" + stars +
                ", battleMap=" + Arrays.toString(battleMap) +
                ", troopMap=" + Arrays.toString(troopMap) +
                ", troops=" + troops +
                ", buildings=" + listGameObjects +
                ", actionsList=" + actionsList +
                '}';
    }

    public void initBattleMap() {
        for (BattleGameObject gameObject : this.listGameObjects) {
            for (int i = 0; i < gameObject.width; i++) {
                for (int j = 0; j < gameObject.height; j++) {
                    this.battleMap[i + gameObject.posX][j + gameObject.posY] = gameObject.id;
                    if (!gameObject.type.startsWith("OBS") && !gameObject.type.startsWith("WAL")) {
                        this.totalBuildingPoint += ((BattleBuilding) gameObject).maxHp;
                    }
                }
            }
        }
    }

    public void initThrowTroopMap() {
        for (BattleGameObject building : this.listGameObjects) {
            int width = building.width + BattleConst.BATTLE_MAP_BORDER,
                    height = building.height + BattleConst.BATTLE_MAP_BORDER,
                    posX = Math.max(building.posX - BattleConst.BATTLE_MAP_BORDER / 2 * BattleConst.BATTLE_MAP_SCALE, 0), posY = Math.max(building.posY - BattleConst.BATTLE_MAP_BORDER / 2 * BattleConst.BATTLE_MAP_SCALE, 0);
            if (building.type.startsWith("OBS")) {
                width = building.width;
                height = building.height;
                posX = posX == 0 ? 0 : posX + BattleConst.BATTLE_MAP_BORDER / 2 * BattleConst.BATTLE_MAP_SCALE;
                posY = posY == 0 ? 0 : posY + BattleConst.BATTLE_MAP_BORDER / 2 * BattleConst.BATTLE_MAP_SCALE;
            } else {

            }

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    this.throwTroopMap[i + posX][j + posY] = building.id;
                }
            }
        }
    }


    public void printGridMap(int[][] map) {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                String cellValue = String.format("%3d", map[row][col]);
                System.out.print(cellValue);
            }
            System.out.println();
        }
    }

    public void pushAction(BattleAction action) {
        this.actionsList.add(action);
    }

    public ArrayList<BattleAction> getActionsList() {
        return this.actionsList;
    }

    public boolean checkValidTroopCount(BattleAction action) {
        if (action.type == BattleConst.ACTION_THROW_TROOP) {
            return this.army.get(action.troopType) > this.getTroopCount(action.troopType);
        }
        return false;
    }

    public void onDestroyBuilding(int id) {
        BattleBuilding building = this.getBattleBuildingById(id);
        this.buildingDestroyedPoint += building.maxHp;


        if (!this.isDestroyedHalf && this.buildingDestroyedPoint * 2 >= this.totalBuildingPoint) {
            this.isDestroyedHalf = true;
            this.stars++;
        }

        if (building.type.startsWith("TOW")) {
            this.stars++;
        }

        if (this.buildingDestroyedPoint >= this.totalBuildingPoint) {
            this.stars++;
            return;
        }

        // remove from building count
//        this.buildingAmount[building._type] = Math.max(this.buildingAmount[building._type] - 1, 0);

        // Update troopMap
        for (int column = building.posX; column < building.posX + building.width; column++) {
            for (int row = building.posY; row < building.posY + building.height; row++) {
                this.troopMap[column][row] = 0;
            }
        }

// Update findPathGrid
        if (building.type.startsWith("WAL")) {
            for (int column = building.posX; column < building.posX + building.width; column++) {
                for (int row = building.posY; row < building.posY + building.height; row++) {
                    // this.findPathGrid[column][row] = 0;
// TODO:                   this.battleGraph.changeNodeWeight(column, row, 0);
                }
            }
        } else {
            for (int column = building.posX + 1; column < building.posX + building.width - 1; column++) {
                for (int row = building.posY + 1; row < building.posY + building.height - 1; row++) {
                    // this.findPathGrid[column][row] = 0;
// TODO:                   this.battleGraph.changeNodeWeight(column, row, 0);
                }
            }
        }

        //update battle graph
//TODO:        this._battleGraph = new BattleGraph(this.findPathGrid);

        System.out.println("get battle graph");


    }

    public void updateResourceGot(int addition, BattleConst.ResourceType type) {

        if (type == BattleConst.ResourceType.GOLD) {
            if (this.goldGot + addition <= this.maxGold) {
                this.goldGot += addition;
            }
        }

        if (type == BattleConst.ResourceType.ELIXIR) {
            if (this.elixirGot + addition <= this.maxElixir) {
                this.elixirGot += addition;
            }
        }
    }

    public void throwTroop(BattleTroop troop) {

    }

    public BattleBuilding getBattleBuildingByPos(int posX, int posY) {
        return this.getBattleBuildingById(this.battleMap[posX][posY]);
    }

    public BattleBuilding getBattleBuildingById(int id) {
        for (BattleBuilding building :
                this.buildings) {
            if (building.id == id) {
                return building;
            }
        }
        return null;
    }


    public boolean checkValidThrowTroopPos(int posX, int posY) {
        return this.throwTroopMap[posX][posY] == 0;
    }


    public int getTroopCount(String type) {
        int count = 0;
        for (int i = 0; i < this.actionsList.size(); i++) {
            if (actionsList.get(i).type == BattleConst.ACTION_THROW_TROOP && actionsList.get(i).troopType.equals(type)) {
                count++;
            }
        }
        return count;
    }

    public void initBattleBuildings() {
        for (BattleGameObject gameObject : this.listGameObjects) {
            gameObject.setMatch(this);
            if (gameObject.type.startsWith("OBS")) {
                this.listObstacles.add((BattleObstacle) gameObject);
            } else if (gameObject.type.startsWith("RES") || gameObject.type.startsWith("STO")) {
//            if (gameObject.type.startsWith("RES") || gameObject.type.startsWith("STO") || gameObject.type.startsWith("TOW")) {
                this.listResources.add((BattleBuilding) gameObject);
            } else if (gameObject.type.startsWith("WAL")) {
                this.listWalls.add((BattleBuilding) gameObject);
            } else if (gameObject.type.startsWith("DEF")) {
                this.listDefences.add((BattleDefence) gameObject);
            } else {
                this.buildings.add((BattleBuilding) gameObject);
            }
        }
    }

    public void setResourceToBuilding() {
        if (this.listResources.size() == 0)
            return;
        int goldCapacity = (int) Math.floor(this.maxGold / this.listResources.size());
        int elixirCapacity = (int) Math.floor(this.maxElixir / this.listResources.size());

        for (BattleBuilding building : this.listResources) {
            BattleStorage storage = (BattleStorage) building;
            if (storage.getResourceType() == BattleConst.ResourceType.GOLD) {
                storage.setCapacity(goldCapacity);
            } else if (storage.getResourceType() == BattleConst.ResourceType.ELIXIR)
                storage.setCapacity(elixirCapacity);
        }

        BattleStorage lastBuilding = (BattleStorage) this.listResources.get(this.listResources.size() - 1);
        if (lastBuilding.getResourceType() == BattleConst.ResourceType.GOLD)
            lastBuilding.setCapacity(this.maxGold - goldCapacity * (this.listResources.size() - 1));
        else if (lastBuilding.getResourceType() == BattleConst.ResourceType.ELIXIR)
            lastBuilding.setCapacity(this.maxElixir - elixirCapacity * (this.listResources.size() - 1));
    }

    public void removeTroop(BattleTroop troop) {
//        for (int i = 0; i < this.troops.size(); i++) {
//            if (this.troops.get(i).id == id) {
//                this.troops.remove(i);
//            }
//        }
        this.troops.remove(troop);
    }

    //get list troops in a circle
    public ArrayList<BattleTroop> getListTroopsInRange(Point centerPoint, double range) {
        //todo: change troops to listCurrentTroops
        ArrayList<BattleTroop> listTroopsInRange = new ArrayList<>();
        for (BattleTroop troop : troops) {
            if (!troop.isAlive())
                continue;
            if (Math.sqrt(Math.pow(centerPoint.x - troop.posX, 2) + Math.pow(centerPoint.y - troop.posY, 2)) <= range)
                listTroopsInRange.add(troop);
        }
        return listTroopsInRange;
    }

    public BattleBullet getOrCreateBullet(String type, Point startPoint, BattleTroop target, int damagePerShot, double attackRadius) {
        for (BattleBullet bullet : bullets)
            if (!bullet.isActive() && bullet.getType().equals(type)) {
                bullet.init(startPoint, target);
                return bullet;
            }

        BattleBullet newBullet = new BattleBullet(type, startPoint, target, damagePerShot, attackRadius);
        newBullet.setMatch(this);
        bullets.add(newBullet);
        return newBullet;
    }

    public ArrayList<BattleDefence> getListDefences() {
        return listDefences;
    }

    public ArrayList<BattleBuilding> getListResources() {
        return listResources;
    }

    public ArrayList<BattleBuilding> getBuildings() {
        return buildings;
    }


    public void sync() {
        this.initData();

        // ignore action start
        int actionIndex = 1;
        int tick = 0;
        LogUtils.reset();

        while (tick < BattleConst.MAX_TICK_PER_GAME) {

            if (actionIndex < this.actionsList.size()) {
                if (this.actionsList.get(actionIndex).tick == tick) {
                    //TODO: do action
                    if (this.actionsList.get(actionIndex).type == BattleConst.ACTION_THROW_TROOP) {
                        BattleTroop troop = new BattleTroop(this.actionsList.get(actionIndex).troopType, 1, this.actionsList.get(actionIndex).posX, this.actionsList.get(actionIndex).posY);
                        troop.setMatch(this);
                        troops.add(troop);
                        LogUtils.writeLog("create troop : " + troop.type + " " + troop.posX + " " + troop.posY);
                    }
                    if (this.actionsList.get(actionIndex).type == BattleConst.ACTION_END) {
                        this.printEndLog();
                        return;
                    }

                    actionIndex++;
                }
            }
            //TODO: update state
            //check defences targets
            for (BattleDefence defence : this.listDefences) {
                if (defence.isDestroy()) {
                    continue;
                }
                defence.validateCurrentTarget();
                if (defence.hasTarget())
                    continue;
                for (BattleTroop troop : this.troops) {//loop list current troops
                    if (!troop.isAlive()) continue;
                    if (defence.checkTarget(troop)) {
                        LogUtils.writeLog("check target :" + tick);
                        defence.setTarget(troop);
                        break;
                    }
                }
            }

            for (BattleDefence defence : this.listDefences)
                if (!defence.isDestroy()) {
                    defence.gameLoop(secPerTick);
                }

            for (BattleBullet bullet : this.bullets)
                if (bullet.isActive()) {
                    bullet.gameLoop(secPerTick);
                }

//            for (BattleTroop troop : this.troops) {
//                troop.gameLoop(0);
//            }

            tick++;
            LogUtils.tick = tick;
        }

    }

    private void printEndLog() {
        LogUtils.writeLog("------------------------------------------ BATTLE ENDED ------------------------------------------");
        LogUtils.writeLog("LIST BUILDING");
        for (BattleBuilding building :
                this.buildings) {
            if(!building.type.startsWith("OBS"))  LogUtils.writeLog(building.toString());
        }
        LogUtils.writeLog("LIST TROOP");
        for (BattleTroop e : this.troops) {
                LogUtils.writeLog(e.toString());
        }
        LogUtils.writeLog("LIST BULLET");
        for (BattleBullet e :
                this.bullets) {
                LogUtils.writeLog(e.toString());
        }

    }


}
