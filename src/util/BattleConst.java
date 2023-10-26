package util;

public class BattleConst {
    public static int MatchingGoldCost = 250;

    public enum BattleBuildingState {
        AVAILABLE, NOT_AVAILABLE, DESTROYED
    }

    public enum BattleMatchState {
        STARTED, ENDED, HAPPENING
    }

    public static int MAX_POINT = 30;
    public static int MIN_POINT = 15;

    public static int[] rankRange = {50,100,200};
    public static int BATTLE_MAP_SIZE = 126;

    public static int MAX_TIME_A_MATCH = 165;

    public static int COUNT_DOWN_TIME = 30;

    public static float RESOURCE_RATE = 0.25F;

    public static String THROW_TROOP = "throw_troop";
    public static String END_GAME = "end_game";




}
