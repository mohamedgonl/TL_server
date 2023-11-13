package util;

public class BattleConst {
    public static int MatchingGoldCost = 250;

    public enum BattleBuildingState {
        AVAILABLE, NOT_AVAILABLE, DESTROYED
    }

    public enum ResourceType {
        GOLD, ELIXIR
    }

    public enum TROOP_STATE {
        FIND_PATH,
        MOVE,
        ATTACK,
        IDLE,
        DEAD,
    }

    public static int GRID_BATTLE_RATIO = 3;

    public static int MATCH_NEW = 0;
    public static int MATCH_HAPPENING = 1;
    public static int MATCH_ENDED = 2;

    public static int ACTION_START = 0;
    public static int ACTION_THROW_TROOP = 1;
    public static int ACTION_END = 2;

    public static int[] rankRange = {50,100,200};
    public static int BATTLE_MAP_SIZE = 132;

    public static int MAX_TIME_A_MATCH = 150;

    public static int COUNT_DOWN_TIME = 30;

    public static float RESOURCE_RATE = 0.25F;

    public static String THROW_TROOP = "throw_troop";
    public static String END_GAME = "end_game";

    public static int BATTLE_MAP_BORDER = 2;
    public static int BATTLE_MAP_SCALE = 3;

    public static int TICK_PER_SECOND = 60;

    public static int MAX_TICK_PER_GAME = (COUNT_DOWN_TIME + MAX_TIME_A_MATCH) * TICK_PER_SECOND;

    public static int RANK_DIST = 500;

    public static int[][] TIME_GET_MATCH = {{2, 50}, {5, 100}, {10, 200}};

}