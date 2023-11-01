package battle_models;

import util.BattleConst;
import util.Common;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class BattleMatch {
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

    public void setGoldGot(int goldGot) {
        this.goldGot = goldGot;
    }

    public void setElixirGot(int elixirGot) {
        this.elixirGot = elixirGot;
    }

    private int goldGot = 0; // vàng chiếm dc
    private int elixirGot = 0; // dầu chiếm dc

    public boolean isWin;
    public float winPercentage;

    public int stars;

    private transient int[][] battleMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];
    private transient int[][] troopMap = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];

    private transient ArrayList<BattleTroop> troops = new ArrayList<>(); // Lưu thông tin từng con lính trong trận
    public ArrayList<BattleBuilding> buildings;
    private ArrayList<BattleAction> actionsList = new ArrayList<>();


    public BattleMatch(int enemyId, String enemyName, ArrayList<BattleBuilding> buildings, Map<String, Integer> army, int maxGold, int maxElixir) {
        this.enemyId = enemyId;
        this.enemyName = enemyName;
        this.maxGold = maxGold;
        this.maxElixir = maxElixir;
        this.army = army;
        this.buildings = buildings;
        this.initData();
    }

    public void initData() {
        this.state = BattleConst.MATCH_NEW;
        this.createTime = Common.currentTimeInSecond();

        Random random = new Random();
        this.winTrophy = random.nextInt(BattleConst.MAX_POINT - BattleConst.MIN_POINT + 1) + BattleConst.MIN_POINT;
        this.loseTrophy = random.nextInt(BattleConst.MAX_POINT - BattleConst.MIN_POINT + 1) + BattleConst.MIN_POINT;

        this.initBattleMap();
        this.printGridMap(this.battleMap);

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

    public void printGridMap(int [][] map) {
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

    public boolean checkValidThrowTroopPos(int posX, int posY) {
        int n = this.battleMap.length;

        for (int i = -BattleConst.BATTLE_MAP_BORDER; i <= BattleConst.BATTLE_MAP_BORDER; i++) {
            for (int j = -BattleConst.BATTLE_MAP_BORDER; j <= BattleConst.BATTLE_MAP_BORDER; j++) {
                int newRow = posX + i;
                int newCol = posY + j;

                if (newRow >= 0 && newRow < n && newCol >= 0 && newCol < n && (newRow != posX || newCol != posY)) {
                    if (this.battleMap[newRow][newCol] != 0) {
                        return true;
                    }
                }
            }
        }

        return false; // Không có ô nào có gi
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




                actionIndex ++;
            }


            //TODO: update state


            tick++;
        }

    }


}
