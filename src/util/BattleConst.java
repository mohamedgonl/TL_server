package util;

public class BattleConst {
    public static int MatchingGoldCost = 1;

    public enum BattleBuildingState {
        AVAILABLE, NOT_AVAILABLE, DESTROYED
    }

    public static int MATCH_NEW = 0;
    public static int MATCH_HAPPENING = 1;
    public static int MATCH_ENDED = 2;

    public static int ACTION_START = 0;
    public static int ACTION_THROW_TROOP = 1;
    public static int ACTION_END = 2;

    public static int MAX_POINT = 30;
    public static int MIN_POINT = 15;

    public static int[] rankRange = {50,100,200};
    public static int BATTLE_MAP_SIZE = 126;

    public static int MAX_TIME_A_MATCH = 150;

    public static int COUNT_DOWN_TIME = 30;

    public static float RESOURCE_RATE = 0.25F;

    public static String THROW_TROOP = "throw_troop";
    public static String END_GAME = "end_game";

    public static int BATTLE_MAP_BORDER = 2;
    public static int BATTLE_MAP_SCALE = 3;




}
