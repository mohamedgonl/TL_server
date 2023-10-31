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

    private transient int[][] map = new int[BattleConst.BATTLE_MAP_SIZE][BattleConst.BATTLE_MAP_SIZE];

    public int createTime; // thời điểm tạo trận
    public int startTime;

    private transient ArrayList<BattleTroop> troops; // Lưu thông tin từng con lính trong trận
    public ArrayList<BattleBuilding> buildings;

    public Map<String, Integer> army; // Lượng lính mang đi đánh

    public int maxGold;

    public int maxElixir;

    private int goldGot = 0; // vàng chiếm dc
    private int elixirGot = 0; // dầu chiếm dc

    private ArrayList<BattleAction> actionsList;
    
    public boolean isWin;
    public float winPercentage;

    public int stars;


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
        
        this.initGridMap();
//        this.printGridMap();

    }
    
    public void initGridMap () {
        for (BattleBuilding building: this.buildings) {
            for (int i = 0; i < building.baseBuildingStats.width*BattleConst.BATTLE_MAP_SCALE; i++) {
                for (int j = 0; j < building.baseBuildingStats.height*BattleConst.BATTLE_MAP_SCALE; j++) {
                    this.map[i + building.posX][j+building.posY] = building.id;
                }
            }
        }
    }

    public void printGridMap () {
        for (int row = 0; row < BattleConst.BATTLE_MAP_SIZE; row++) {
            for (int col = 0; col < BattleConst.BATTLE_MAP_SIZE; col++) {
                String cellValue = String.format("%3d", this.map[row][col]);
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

            if(this.actionsList.get(actionIndex).tick == tick) {
                //TODO: do action

            }


            //TODO: update state





            tick++;
        }

    }

    public void updateResourceGot(int addition, BattleConst.ResourceType type) {

        if(type == BattleConst.ResourceType.GOLD) {
            if(this.goldGot + addition <= this.maxGold ) {
                this.goldGot += addition;
            }
        }

        if(type == BattleConst.ResourceType.ELIXIR) {
            if(this.elixirGot + addition <= this.maxElixir ) {
                this.elixirGot += addition;
            }
        }


    }


}
