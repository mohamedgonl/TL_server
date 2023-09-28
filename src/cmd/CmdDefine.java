package cmd;


public class CmdDefine {
    public static final short CUSTOM_LOGIN = 1;
    public static final short GET_USER_INFO = 1001;
    public static final short GET_MAP_INFO = 1002;

    //building
    public static final short BUY_BUILDING = 2001;
    public static final short CANCEL_BUILD = 2002;
    public static final short BUILD_SUCCESS = 2003;

    public static final short UPGRADE_BUILDING = 2004;
    public static final short CANCEL_UPGRADE = 2005;
    public static final short UPGRADE_SUCCESS = 2006;

    //Log cmd
    public static final short MOVE = 3001;
    public static final short GET_NAME = 3002;
    public static final short SET_NAME = 3003;

}
