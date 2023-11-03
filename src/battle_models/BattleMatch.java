package battle_models;

import util.BattleConst;
import util.Common;
import util.database.DataModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

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

    public int maxGold;

    public int maxElixir;

    private int goldGot = 0; // vàng chiếm dc
    private int elixirGot = 0; // dầu chiếm dc

    public boolean isWin;
    public float winPercentage = 0;

    public int stars;

    private transient int[][] battleMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];
    private transient int[][] troopMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];

    private transient int[][] throwTroopMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];


    private transient ArrayList<BattleTroop> troops = new ArrayList<>(); // Lưu thông tin từng con lính trong trận
    public ArrayList<BattleBuilding> buildings;
    private ArrayList<BattleAction> actionsList = new ArrayList<>();


    public BattleMatch(int id, int enemyId, String enemyName, ArrayList<BattleBuilding> buildings, Map<String, Integer> army, int maxGold, int maxElixir) {
        this.id = id;
        this.enemyId = enemyId;
        this.enemyName = enemyName;
        this.maxGold = maxGold;
        this.maxElixir = maxElixir;
        this.army = army;
        this.buildings = buildings;
        this.initData();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void initData() {
        this.state = BattleConst.MATCH_NEW;
        this.createTime = Common.currentTimeInSecond();

        Random random = new Random();
        this.winTrophy = random.nextInt(BattleConst.MAX_POINT - BattleConst.MIN_POINT + 1) + BattleConst.MIN_POINT;
        this.loseTrophy = random.nextInt(BattleConst.MAX_POINT - BattleConst.MIN_POINT + 1) + BattleConst.MIN_POINT;

        this.initBattleMap();
        this.initThrowTroopMap();
//        this.printGridMap(this.battleMap);

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
                }
            }
        }
    }

    public void initThrowTroopMap() {
        for (BattleBuilding building : this.buildings) {
            int width = building.baseBuildingStats.width + BattleConst.BATTLE_MAP_BORDER,
                    height = building.baseBuildingStats.height + BattleConst.BATTLE_MAP_BORDER,
            posX = Math.max(building.posX - BattleConst.BATTLE_MAP_BORDER/2 * BattleConst.BATTLE_MAP_SCALE, 0), posY = Math.max(building.posY - BattleConst.BATTLE_MAP_BORDER/2 * BattleConst.BATTLE_MAP_SCALE, 0);
            if (building.type.startsWith("OBS")) {
                width = building.baseBuildingStats.width;
                height = building.baseBuildingStats.height;
                posX = posX == 0 ? 0 : posX + BattleConst.BATTLE_MAP_BORDER/2 * BattleConst.BATTLE_MAP_SCALE;
                posY = posY == 0 ? 0 : posY +  BattleConst.BATTLE_MAP_BORDER/2 * BattleConst.BATTLE_MAP_SCALE;
            }
            else {

            }

            for (int i = 0; i < width * BattleConst.BATTLE_MAP_SCALE; i++) {
                for (int j = 0; j <height * BattleConst.BATTLE_MAP_SCALE; j++) {
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


            tick++;
        }

    }


}
