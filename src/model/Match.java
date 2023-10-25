package model;

import battle_models.BattleBuilding;
import battle_models.BattleTroop;
import util.Common;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Match {
    public int id;
    public int enemyId;

    public String enemyName;

    private int[][] map;

    public int timeStart; // thời điểm tạo trận

    private ArrayList<BattleTroop> troops; // Lưu thông tin từng con lính trong trận
    public ArrayList<BattleBuilding> buildings;

    public Map<String, Integer> army; // Lượng lính mang đi đánh

    public int maxGold;

    public int maxElixir;

    private int goldGot = 0;
    private int elixirGot = 0;


    public Match (int enemyId, String enemyName, ArrayList<BattleBuilding> buildings, Map<String, Integer> army, int maxGold, int maxElixir) {
        this.enemyId = enemyId;
        this.enemyName = enemyName;
        this.maxGold = maxGold;
        this.maxElixir = maxElixir;
        this.army = army;
        this.buildings = buildings;
        this.initData();
    }

    public void initData () {
        this.timeStart = Common.currentTimeInSecond();
    }


}
