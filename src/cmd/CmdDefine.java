package cmd;


public class CmdDefine {
    public static final short CUSTOM_LOGIN = 1;
    public static final short GET_USER_INFO = 1001;
    public static final short GET_MAP_INFO = 1002;
    public static final short GET_TIME_SERVER = 1003;
    public static final short GET_ARMY_INFO = 1004;

    public static final short CHEAT_RESOURCE = 1900;

    //building
    public static final short BUY_BUILDING = 2001;
    public static final short CANCEL_BUILD = 2002;
    public static final short BUILD_SUCCESS = 2003;

    public static final short UPGRADE_BUILDING = 2004;
    public static final short CANCEL_UPGRADE = 2005;
    public static final short UPGRADE_SUCCESS = 2006;
    public static final short COLLECT_RESOURCE = 2007;
    public static final short MOVE_BUILDING = 2008;

    public static final short REMOVE_OBSTACLE = 2009;
    public static final short REMOVE_OBSTACLE_SUCCESS = 2010;

    public static final short MOVE_LIST_WALL = 2011;
    public static final short UPGRADE_LIST_WALL = 2012;

    public static final short FINISH_WORK_BY_GEM = 2013;


    //Log cmd
    public static final short MOVE = 3001;
    public static final short GET_NAME = 3002;
    public static final short SET_NAME = 3003;

    public static final short BUY_RESOURCE = 4001;
    public static final short TRAIN_TROOP_CREATE = 5001;
    public static final short TRAIN_TROOP_SUCCESS = 5002;

    public static final short GET_TRAINING_LIST = 5003;

    public static final short CANCLE_TRAIN_TROOP = 5004;

}
