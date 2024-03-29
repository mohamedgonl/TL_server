package model;

import battle_models.BattleMatch;
import util.BuildingFactory;
import util.GameConfig;
import util.config.ArmyCampConfig;
import util.database.DataModel;

import java.util.*;

public class PlayerInfo extends DataModel {
    public static int fakeIdGenerate = -1;
    private int id;
    private String name;
    private String avatar;
    private int level;
    private int rank;
    private int gold;
    private int elixir;
    private int gem;
    private int goldCapacity;
    private int elixirCapacity;
    private Map<String, Integer> listTroops = new HashMap<>();

    private ArrayList<Building> listBuildings;

    //not be saved in db
    private transient String townHallType;
    private transient int townHallLv;
    private transient int avaiableBuilders;
    private transient int totalBuilders;
    private transient int[][] map;
    private transient Map<String, Integer> buildingAmount;

    public ArrayList<BattleMatch> getBattleMatches() {
        return battleMatches;
    }

    private ArrayList<BattleMatch> battleMatches = new ArrayList<>();

    public PlayerInfo() {
    }

    public PlayerInfo(int id, String name) {
        this.id = id;
        this.name = name;
        this.avatar = "";
        this.level = 1;
        this.rank = 0;
        this.gold = 0;
        this.elixir = 0;
        this.gem = 0;
        this.goldCapacity = 0;
        this.elixirCapacity = 0;
        this.townHallLv = 1;
        this.avaiableBuilders = 0;
        this.totalBuilders = 0;
    }

    public PlayerInfo(int id, String name, String avatar, int level, int rank, int gold, int elixir, int gem, int goldCapacity, int elixirCapacity) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.level = level;
        this.rank = rank;
        this.gold = gold;
        this.elixir = elixir;
        this.gem = gem;
        this.goldCapacity = goldCapacity;
        this.elixirCapacity = elixirCapacity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = Math.max(rank, 0);
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getElixir() {
        return elixir;
    }

    public void setElixir(int elixir) {
        this.elixir = elixir;
    }

    public int getGem() {
        return gem;
    }

    public void setGem(int gem) {
        this.gem = gem;
    }

    public int getGoldCapacity() {
        return goldCapacity;
    }

    public void setGoldCapacity(int goldCapacity) {
        this.goldCapacity = goldCapacity;
    }

    public int getElixirCapacity() {
        return elixirCapacity;
    }

    public void setElixirCapacity(int elixirCapacity) {
        this.elixirCapacity = elixirCapacity;
    }

    public int getTownHallLv() {
        return townHallLv;
    }

    public void setTownHallLv(int townHallLv) {
        this.townHallLv = townHallLv;
    }

    public String getTownHallType() {
        return townHallType;
    }

    public void setTownHallType(String townHallType) {
        this.townHallType = townHallType;
    }

    public int getAvaiableBuilders() {
        return avaiableBuilders;
    }

    public void setAvaiableBuilders(int avaiableBuilders) {
        this.avaiableBuilders = avaiableBuilders;
    }

    public int getTotalBuilders() {
        return totalBuilders;
    }

    public void setTotalBuilders(int totalBuilders) {
        this.totalBuilders = totalBuilders;
    }

    public Map<String, Integer> getBuildingAmount() {
        return buildingAmount;
    }

    public void setBuildingAmount(Map<String, Integer> buildingAmount) {
        this.buildingAmount = buildingAmount;
    }

    public ArrayList<Building> getListBuildings() {
        return listBuildings;
    }

    public void setListBuildings(ArrayList<Building> listBuildings) {
        this.listBuildings = listBuildings;
    }
    public void setListTroops(Map<String, Integer> listTroops) {
        this.listTroops = listTroops;
    }

    public int[][] getMap() {
        return map;
    }

    public void setMap(int[][] map) {
        this.map = map;
    }

    public Map<String, Integer> getListTroops() {
        return listTroops;
    }

    public void useResources(int gold, int elixir, int gem) {
        if (gold < 0 || elixir < 0 || gem < 0)
            return;

        this.gold -= gold;
        this.elixir -= elixir;
        this.gem -= gem;

        if (this.gold < 0)
            this.gold = 0;
        if (this.elixir < 0)
            this.elixir = 0;
        if (this.gem < 0)
            this.gem = 0;
    }

    public void addResources(int gold, int elixir, int gem) {
        if (gold < 0 || elixir < 0 || gem < 0)
            return;

        this.gold += gold;
        this.elixir += elixir;
        this.gem += gem;

        if (this.gold > goldCapacity)
            this.gold = goldCapacity;
        if (this.elixir > elixirCapacity)
            this.elixir = elixirCapacity;
    }

    public void useBuilder(int builderAmount) {
        if (this.avaiableBuilders - builderAmount >= 0)
            this.avaiableBuilders -= builderAmount;
        else
            this.avaiableBuilders = 0;
    }

    public void freeBuilder(int builderAmount) {
        if (this.avaiableBuilders + builderAmount <= totalBuilders)
            this.avaiableBuilders += builderAmount;
        else this.avaiableBuilders = totalBuilders;
    }

    public void pushToListTroop(List<TrainingItem> listTroops){
        for (int i = 0; i < listTroops.size(); i++) {
            String cfg  = listTroops.get(i).cfgId;
            int count = listTroops.get(i).count;

            int amount = this.listTroops.getOrDefault(cfg, 0) + count;
            this.listTroops.put(cfg, Math.max(0,amount));
        }
    }

    public void removeTroop(Map<String, Integer> troops) {
        for (Map.Entry<String, Integer> entry : troops.entrySet()) {
            String key = entry.getKey();
            Integer sub = entry.getValue();

            if (this.listTroops.containsKey(key)) {
                Integer count = listTroops.get(key);
                if(count - sub < 0) {
                    listTroops.put(key, 0);
                }
                    listTroops.put(key, count - sub);
            }
        }
    }



    public int getMaxArmySpace(){
        int max  = 0;
        for (int i = 0; i < this.listBuildings.size(); i++) {
            if(this.listBuildings.get(i).getType().startsWith(BuildingFactory.GameObjectPrefix.ARMY_CAMP)) {
                int level = this.listBuildings.get(i).getLevel();
                max += ((ArmyCampConfig)GameConfig.getInstance().getBuildingConfig(BuildingFactory.BuildingType.ARMY_CAMP, level)).capacity;
            }
        }
        return max;
    }

    public int getCurrentTroopSpace(){
        int total =  0;
        for (Map.Entry<String, Integer> entry : listTroops.entrySet()) {
            total += entry.getValue() * GameConfig.getInstance().troopBaseConfig.get(entry.getKey()).housingSpace;
        }
        return total;
    }

    public void pushNewMatch(BattleMatch match) {
        this.battleMatches.add(match);
    }


    @Override
    public String toString() {
        return "PlayerInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", level=" + level +
                ", rank=" + rank +
                ", gold=" + gold +
                ", elixir=" + elixir +
                ", gem=" + gem +
                ", goldCapacity=" + goldCapacity +
                ", elixirCapacity=" + elixirCapacity +
                ", listTroops=" + listTroops +
                ", listBuildings=" + listBuildings +
                ", townHallType='" + townHallType + '\'' +
                ", townHallLv=" + townHallLv +
                ", avaiableBuilders=" + avaiableBuilders +
                ", totalBuilders=" + totalBuilders +
                ", map=" + Arrays.toString(map) +
                ", buildingAmount=" + buildingAmount +
                ", battleMatches=" + battleMatches +
                '}';
    }
}
