package battle_models;

import util.BattleConst;
import util.Common;
import util.database.DataModel;

import java.util.*;

public class BattleMatch extends DataModel {
    public static int idGenerate = 1;
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

    private int goldGot = 0; // vàng chiếm dc
    private int elixirGot = 0; // dầu chiếm dc

    public boolean isWin;
    public int winPercentage = 0;

    public int stars;
    private ArrayList<BattleAction> actionsList = new ArrayList<>();
    public ArrayList<BattleBuilding> buildings;

    private transient int[][] battleMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];
    private transient int[][] troopMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];
    private transient int[][] throwTroopMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];
    private transient ArrayList<BattleTroop> troops = new ArrayList<>(); // Lưu thông tin từng con lính trong trận
    public transient ArrayList<BattleDefence> defBuildings = new ArrayList<>();
    public transient ArrayList<BattleBuilding> resBuildings = new ArrayList<>();
    public transient ArrayList<BattleBullet> bullets = new ArrayList<>();
    private transient int buildingDestroyedPoint = 0;
    private transient int totalBuildingPoint = 0;

    private transient boolean isDestroyedHalf = false;


    public BattleMatch(int id, int enemyId, String enemyName, ArrayList<BattleBuilding> buildings, Map<String, Integer> army, int maxGold, int maxElixir, int enemyRank, int userRank) {
        this.id = id;
        this.enemyId = enemyId;
        this.enemyName = enemyName;
        this.maxGold = maxGold;
        this.maxElixir = maxElixir;
        this.army = army;
        this.buildings = buildings;
        this.state = BattleConst.MATCH_NEW;
        this.createTime = Common.currentTimeInSecond();

        this.winTrophy = this.getWinTrophy(enemyRank, userRank);
        this.loseTrophy = this.getLoseTrophy(enemyRank, userRank);

//        this.initData();
    }

    public void setId(int id) {
        this.id = id;
    }


    // must be call when sync
    public void initData() {

        this.initBattleMap();
        this.initThrowTroopMap();
        this.initBattleBuildings();

    }

    public int getWinTrophy(int userRank, int enemyRank) {
        return (int) Math.floor(-0.63599 + (59.43467 + 0.63599) / (1 + 0.991798 * Math.exp(0.00576 * (userRank - enemyRank))));
    }

    public int getLoseTrophy(int userRank, int enemyRank) {
        return (int) Math.floor(39.0907 - (39.0619) / (1 + 0.993 * Math.exp(0.00595 * (userRank - enemyRank))));
    }

    public void setGoldGot(int goldGot) {
        this.goldGot = goldGot;
    }

    public void setElixirGot(int elixirGot) {
        this.elixirGot = elixirGot;
    }

    public int getGoldGot() {
        return this.goldGot;
    }

    public int getElixirGot() {
        return this.elixirGot;
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
                ", buildings=" + buildings +
                ", actionsList=" + actionsList +
                '}';
    }

    public void initBattleMap() {
        for (BattleBuilding building : this.buildings) {
            for (int i = 0; i < building.baseBuildingStats.width * BattleConst.BATTLE_MAP_SCALE; i++) {
                for (int j = 0; j < building.baseBuildingStats.height * BattleConst.BATTLE_MAP_SCALE; j++) {
                    this.battleMap[i + building.posX][j + building.posY] = building.id;
                    if (!building.type.startsWith("OBS") && !building.type.startsWith("WAL")) {
                        this.totalBuildingPoint += building.baseBuildingStats.hitpoints;
                    }
                }
            }
        }
    }

    public void initThrowTroopMap() {
        for (BattleBuilding building : this.buildings) {
            int width = building.baseBuildingStats.width + BattleConst.BATTLE_MAP_BORDER,
                    height = building.baseBuildingStats.height + BattleConst.BATTLE_MAP_BORDER,
                    posX = Math.max(building.posX - BattleConst.BATTLE_MAP_BORDER / 2 * BattleConst.BATTLE_MAP_SCALE, 0), posY = Math.max(building.posY - BattleConst.BATTLE_MAP_BORDER / 2 * BattleConst.BATTLE_MAP_SCALE, 0);
            if (building.type.startsWith("OBS")) {
                width = building.baseBuildingStats.width;
                height = building.baseBuildingStats.height;
                posX = posX == 0 ? 0 : posX + BattleConst.BATTLE_MAP_BORDER / 2 * BattleConst.BATTLE_MAP_SCALE;
                posY = posY == 0 ? 0 : posY + BattleConst.BATTLE_MAP_BORDER / 2 * BattleConst.BATTLE_MAP_SCALE;
            } else {

            }

            for (int i = 0; i < width * BattleConst.BATTLE_MAP_SCALE; i++) {
                for (int j = 0; j < height * BattleConst.BATTLE_MAP_SCALE; j++) {
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
        this.buildingDestroyedPoint += building.baseBuildingStats.hitpoints;


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
        for (int column = building.posX; column < building.posX + building.baseBuildingStats.width; column++) {
            for (int row = building.posY; row < building.posY + building.baseBuildingStats.height; row++) {
                this.troopMap[column][row] = 0;
            }
        }

// Update findPathGrid
        if (building.type.startsWith("WAL")) {
            for (int column = building.posX; column < building.posX + building.baseBuildingStats.width; column++) {
                for (int row = building.posY; row < building.posY + building.baseBuildingStats.height; row++) {
                    // this.findPathGrid[column][row] = 0;
// TODO:                   this.battleGraph.changeNodeWeight(column, row, 0);
                }
            }
        } else {
            for (int column = building.posX + 1; column < building.posX + building.baseBuildingStats.width - 1; column++) {
                for (int row = building.posY + 1; row < building.posY + building.baseBuildingStats.height - 1; row++) {
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
            if (actionsList.get(i).troopType.equals(type)) {
                count++;
            }
        }
        return count;
    }

    public void initBattleBuildings() {
        for (BattleBuilding building : this.buildings) {
            if (building.type.startsWith("RES") || building.type.startsWith("STO") || building.type.startsWith("TOW")) {
                this.resBuildings.add(building);
            }
            if (building.type.startsWith("DEF")) {
                this.defBuildings.add(new BattleDefence(building.id, building.type, building.level, building.posX, building.posY));
            }
        }
    }

    public void removeTroop(int id) {
        for (int i = 0; i < this.troops.size(); i++) {
            if (this.troops.get(i).id == id) {
                this.troops.remove(i);
            }
        }
    }

    public ArrayList<BattleDefence> getDefBuildings() {
        return defBuildings;
    }

    public ArrayList<BattleBuilding> getResBuildings() {
        return resBuildings;
    }

    public ArrayList<BattleBuilding> getBuildings() {
        return buildings;
    }

    public void startGameLoop() {

        // ignore action start
        int actionIndex = 1;
        int tick = 0;

        while (tick < BattleConst.MAX_TICK_PER_GAME || this.actionsList.get(actionIndex).type != BattleConst.ACTION_END) {

            if (this.actionsList.get(actionIndex).tick == tick && actionIndex < this.actionsList.size()) {
                //TODO: do action

                actionIndex++;
            }
            //TODO: update state

            // defence buildings
            for (BattleDefence defence : this.defBuildings) {
                defence.gameLoop(0);
            }

            for (BattleBullet bullet : this.bullets) {
                bullet.gameLoop(0);
            }


            for (BattleTroop troop: this.troops) {
                troop.gameLoop(0);
            }

            tick++;
        }

    }


}
