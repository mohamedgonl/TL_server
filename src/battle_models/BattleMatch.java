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

    public int winPoint;
    public int losePoint;

    private transient int[][] map;

    public int createTime; // thời điểm tạo trận
    public int startTime;

    private transient ArrayList<BattleTroop> troops; // Lưu thông tin từng con lính trong trận
    public ArrayList<BattleBuilding> buildings;

    public Map<String, Integer> army; // Lượng lính mang đi đánh

    public int maxGold;

    public int maxElixir;

    private int goldGot = 0;
    private int elixirGot = 0;

    private ArrayList<BattleAction> actionsList;


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
        this.winPoint = random.nextInt(BattleConst.MAX_POINT - BattleConst.MIN_POINT + 1) + BattleConst.MIN_POINT;
        this.losePoint = random.nextInt(BattleConst.MAX_POINT - BattleConst.MIN_POINT + 1) + BattleConst.MIN_POINT;
    }

    public void pushAction(BattleAction action) {
        this.actionsList.add(action);
    }

    public ArrayList<BattleAction> getActionsList() {
        return this.actionsList;
    }

    public boolean checkValidActionThrowTroop(BattleAction action) {
        if (action.type == BattleConst.ACTION_THROW_TROOP) {
            if (this.army.get(action.type) <= this.getTroopCount(action.troopType)) {
                return false;
            }
            return true;
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


}
