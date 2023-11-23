package battle_models;

import util.BattleConst;
import util.Common;
import util.algorithms.BattleGraph;
import util.database.DataModel;
import util.log.LogUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BattleMatch extends DataModel {

    private boolean isSaved = false;
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
    public Map<String, Integer> usedArmy = new HashMap<>();
    public int maxGold;
    public int maxElixir;
    public boolean isWin;
    public int winPercentage = 0;
    public int stars;
    public ArrayList<BattleGameObject> listGameObjects;
    public transient ArrayList<BattleObstacle> listObstacles = new ArrayList<>();
    public transient ArrayList<BattleBuilding> buildings = new ArrayList<>();
    public transient ArrayList<BattleDefence> listDefences = new ArrayList<>();
    public transient ArrayList<BattleBuilding> listResources = new ArrayList<>();
    public transient ArrayList<BattleBuilding> listWalls = new ArrayList<>();
    public transient ArrayList<BattleBullet> bullets = new ArrayList<>();
    public transient int[][] findPathGrid;
    public transient ArrayList<TroopBullet> listTroopBullet = new ArrayList<>();
    public transient BattleGraph battleGraph;
    private transient BattleBuilding townHall;
    private int goldGot = 0; // vàng chiếm dc
    private int elixirGot = 0; // dầu chiếm dc
    private transient int buildingDestroyedPoint = 0;
    private transient int totalBuildingPoint = 0;
    private transient boolean isDestroyedHalf = false;
    private int idTroop;


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
        this.idTroop = 0;

        this.winTrophy = this.getWinTrophy(enemyRank, userRank);
        this.loseTrophy = this.getLoseTrophy(enemyRank, userRank);

    }

    // must be call when sync
    public void initData() {
        this.initBattleBuildings();
        this.initFindPathGrid();
        this.setResourceToBuilding();
        this.initBattleMap();
        this.initThrowTroopMap();

        //init battleGraph
        this.battleGraph = new BattleGraph(this.findPathGrid, this.battleMap);
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
            if (!gameObject.type.startsWith("OBS") && !gameObject.type.startsWith("WAL")) {
                this.totalBuildingPoint += ((BattleBuilding) gameObject).maxHp;
            }
            for (int i = 0; i < gameObject.width; i++) {
                for (int j = 0; j < gameObject.height; j++) {
                    this.battleMap[i + gameObject.posX][j + gameObject.posY] = gameObject.id;
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

    public void initFindPathGrid() {
        this.findPathGrid = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];
        //set all to 0
        for (int i = 0; i < BattleConst.BATTLE_MAP_SIZE; i++) {
            for (int j = 0; j < BattleConst.BATTLE_MAP_SIZE; j++) {
                this.findPathGrid[i][j] = 0;
            }
        }

        //
        for (BattleGameObject gameObject : this.listGameObjects) {
            if (gameObject.type.startsWith("OBS")) continue;
            if (gameObject.type.startsWith("WAL")) {
                for (int i = gameObject.posX; i < gameObject.posX + gameObject.width; i++) {
                    for (int j = gameObject.posY; j < gameObject.posY + gameObject.height; j++) {
                        this.findPathGrid[i][j] = 9;
                    }
                }
                continue;
            }

            //normal building, not inclue obs and wall
            for (int i = gameObject.posX + 1; i < gameObject.posX + gameObject.width - 1; i++) {
                for (int j = gameObject.posY + 1; j < gameObject.posY + gameObject.height - 1; j++) {
                    this.findPathGrid[i][j] = 99999;
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

        if (!building.type.startsWith("WAL")) {
            this.buildingDestroyedPoint += building.maxHp;

            if (!this.isDestroyedHalf && this.buildingDestroyedPoint * 2 >= this.totalBuildingPoint) {
                this.isDestroyedHalf = true;
                this.stars++;
            }
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

        //update mapgrid
        for (int column = building.posX; column < building.posX + building.width; column++) {
            for (int row = building.posY; row < building.posY + building.height; row++) {
                this.battleMap[column][row] = 0;
            }
        }

        // Update troopMap
        for (int column = building.posX; column < building.posX + building.width; column++) {
            for (int row = building.posY; row < building.posY + building.height; row++) {
                this.troopMap[column][row] = 0;
            }
        }

        //update find path grid
        if (building.type.startsWith("WAL")) {
            for (int i = building.posX; i < building.posX + building.width; i++) {
                for (int j = building.posY; j < building.posY + building.height; j++) {
                    this.battleGraph.changeNodeWeight(i, j, 0);
                }
            }
        } else {
            for (int i = building.posX + 1; i < building.posX + building.width - 1; i++) {
                for (int j = building.posY + 1; j < building.posY + building.height - 1; j++) {
                    this.battleGraph.changeNodeWeight(i, j, 0);
                }
            }
        }

    }

    public BattleGraph getBattleGraph() {
        return this.battleGraph;
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
            } else {
                this.buildings.add((BattleBuilding) gameObject);

                if (gameObject.type.startsWith("RES") || gameObject.type.startsWith("STO")) {
//            if (gameObject.type.startsWith("RES") || gameObject.type.startsWith("STO") || gameObject.type.startsWith("TOW")) {
                    this.listResources.add((BattleBuilding) gameObject);
                } else if (gameObject.type.startsWith("WAL")) {
                    this.listWalls.add((BattleBuilding) gameObject);
                } else if (gameObject.type.startsWith("DEF")) {
                    this.listDefences.add((BattleDefence) gameObject);
                } else if (gameObject.type.startsWith("TOW")) {
                    this.townHall = (BattleBuilding) gameObject;
                }
            }
        }
    }

    public void addTroopBullet(TroopBullet bullet) {
        this.listTroopBullet.add(bullet);
    }

    public void setResourceToBuilding() {
        if (this.listResources.size() == 0)
            return;

        int goldBuildingAmount = 0;
        int elixirBuildingAmount = 0;

        for (BattleBuilding building : this.listResources) {
            BattleStorage storage = (BattleStorage) building;
            if (storage.getResourceType() == BattleConst.ResourceType.GOLD) {
                goldBuildingAmount++;
            } else if (storage.getResourceType() == BattleConst.ResourceType.ELIXIR)
                elixirBuildingAmount++;
        }

        int goldCapacity = (int) Math.floor((double) this.maxGold / goldBuildingAmount);
        int elixirCapacity = (int) Math.floor((double) this.maxElixir / elixirBuildingAmount);

        for (BattleBuilding building : this.listResources) {
            BattleStorage storage = (BattleStorage) building;
            if (storage.getResourceType() == BattleConst.ResourceType.GOLD) {
                storage.setCapacity(goldCapacity);
            } else if (storage.getResourceType() == BattleConst.ResourceType.ELIXIR)
                storage.setCapacity(elixirCapacity);
        }

        for (int i = this.listResources.size() - 1; i >= 0; i--) {
            BattleStorage latStorage = (BattleStorage) listResources.get(i);
            if (latStorage.getResourceType() == BattleConst.ResourceType.GOLD) {
                latStorage.setCapacity(this.maxGold - goldCapacity * (goldBuildingAmount - 1));
                break;
            }
        }
        for (int i = this.listResources.size() - 1; i >= 0; i--) {
            BattleStorage latStorage = (BattleStorage) listResources.get(i);
            if (latStorage.getResourceType() == BattleConst.ResourceType.ELIXIR) {
                latStorage.setCapacity(this.maxElixir - elixirCapacity * (elixirBuildingAmount - 1));
                break;
            }
        }
    }

    public void removeTroop(BattleTroop troop) {
//        for (int i = 0; i < this.troops.size(); i++) {
//            if (this.troops.get(i).id == id) {
//                this.troops.remove(i);
//            }
//        }
//        this.troops.remove(troop);
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

    public BattleBullet getOrCreateBullet(String type, Point startPoint, BattleTroop target, int damagePerShot, double attackRadius, int attackArea) {
        for (BattleBullet bullet : bullets)
            if (!bullet.isActive() && bullet.getType().equals(type)) {
                bullet.init(startPoint, target, damagePerShot);
                return bullet;
            }

        BattleBullet newBullet = new BattleBullet(type, startPoint, target, damagePerShot, attackRadius, attackArea);
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

    public ArrayList<BattleTroop> getTroops() {
        return troops;
    }


    public void sync() {
        this.initData();

        int actionIndex = 0;
        // ignore action start
        if (this.actionsList.get(actionIndex).type == BattleConst.ACTION_START) {
            actionIndex = 1;
        }

        int tick = 0;
        LogUtils.reset();

        while (tick < BattleConst.MAX_TICK_PER_GAME) {
            if (actionIndex < this.actionsList.size()) {
                if (this.actionsList.get(actionIndex).tick == tick) {
                    //TODO: do action
                    if (this.actionsList.get(actionIndex).type == BattleConst.ACTION_THROW_TROOP) {
                        BattleTroop troop = new BattleTroop(this.actionsList.get(actionIndex).troopType, 1, this.actionsList.get(actionIndex).posX, this.actionsList.get(actionIndex).posY);
                        troop.setMatch(this);
                        troop.setId(this.idTroop);
                        this.idTroop++;
                        troops.add(troop);
                        LogUtils.writeLog("create troop : " + troop.type + " " + troop.posX + " " + troop.posY);
                    }
                    if (this.actionsList.get(actionIndex).type == BattleConst.ACTION_END) {
                        this.updateData();
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

            for (BattleTroop troop : this.troops) {
                troop.gameLoop(secPerTick);
            }

            for (TroopBullet bullet : this.listTroopBullet) {
                bullet.gameLoop(secPerTick);
            }

            tick++;
            LogUtils.tick = tick;
        }

    }

    //return battlemap
    public int[][] getBattleMap() {
        return this.battleMap;
    }

    private void printEndLog() {
        LogUtils.writeLog("------------------------------------------ BATTLE ENDED ------------------------------------------");
        LogUtils.writeLog("LIST BUILDING");
        for (BattleBuilding building :
                this.buildings) {
            if (!building.type.startsWith("OBS")) LogUtils.writeLog(building.toString());
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
        LogUtils.writeLog("STAR: " + this.stars);
        LogUtils.writeLog("PERCENT : " + this.winPercentage);
        LogUtils.writeLog("RESOURCE GOT");
        LogUtils.writeLog("GOLD : " + this.goldGot + " ELIXIR : " + this.elixirGot);
        LogUtils.writeLog("TROOPS : ");
        for (Map.Entry<String, Integer> entry : this.usedArmy.entrySet()) {
            LogUtils.writeLog("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

    }


    private void updateData() {
        this.winPercentage = this.buildingDestroyedPoint * 100 / this.totalBuildingPoint;
        System.out.println(this.winPercentage);

        this.isWin = this.stars > 0;

        this.trophy = this.isWin ? this.winTrophy : -this.loseTrophy;
//        this.trophy = this.winPercentage * (this.isWin ? this.winTrophy : -this.loseTrophy) / 100;

        for (BattleAction action : this.actionsList) {
            if (action.type == BattleConst.ACTION_THROW_TROOP) {
                if (this.usedArmy.containsKey(action.troopType)) {
                    this.usedArmy.put(action.troopType, this.usedArmy.get(action.troopType) + 1);
                } else {
                    this.usedArmy.put(action.troopType, 1);
                }
            }
        }
    }


    public boolean isSaved() {
        return this.isSaved;
    }

    public void setSaved(boolean isSynced) {
        this.isSaved = isSynced;
    }
}
