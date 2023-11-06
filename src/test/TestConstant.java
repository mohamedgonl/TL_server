package test;

import java.awt.*;

public class TestConstant {
    public static String SERVER_ADDR = "127.0.0.1";
    public static int SERVER_PORT = 1101;
    public static int DB_PORT = 11211;
    public static int USER_ID = 7;
    public static Point CHANGE_POS = new Point(2,5);

    public static String ERROR_MSG_DATABASE = "\nSUGGESTION:" +
            "\nEdit conf/cluster.properties \nset dservers=127.0.0.1:11211\n";
    public static String ERROR_MSG_LISTEN_ADDR = "\nSUGGESTION:" +
            "\nSet IP Address in config/server.xml -> serverSettings -> socketAddresses -> socket : \"address\"\n";
    public static String ERROR_MSG_LISTEN_PORT = "\nSUGGESTION:" +
            "\nSet IP Address in config/server.xml -> serverSettings -> socketAddresses -> socket : \"port\"\n";

    public static String ERROR_MSG_REQUEST_CHANGE_POS = "\nSUGGESTION:" +
            "\nGo to ChangePosition.java -> unpackData()" +
            "\nRead x and y by:" +
            "\n  x = readInt(bf);" +
            "\n  y = readInt(bf);\n";

    public static String ERROR_MSG_VALID_POS = "\nSUGGESTION:" +
            "\ncheck valid position in PlayerInfo.java -> isValidPos()!\n";
    public static String ERROR_MSG_CHANGE_POS_RES = "\nSUGGESTION:" +
            "\nGo to ResponseChangePosition.java -> createData()" +
            "\nWrite response to Client by:" +
            "\n  ByteBuffer bf = makeBuffer();" +
            "\n  bf.putInt(this.pos.x);" +
            "\n  bf.putInt(this.pos.y);" +
            "\n  return packBuffer(bf);\n";


    public static String ERROR_MSG_SAVE_DB = "\nSUGGESTION:" +
            "\nGo to DemoHandler.java -> processChangePosition()" +
            "\nafter visit position and before send message to client, save to database" +
            "\nuserInfo.saveModel(user.getId());\n";

    public static String ERROR_MSG_CACHE_IN_RAM = "\nSUGGESTION:" +
            "\nGo to LoginSuccessHandler.java -> onLoginSuccess()" +
            "\ncache data PlayerInfo in RAM by" +
            "\nuser.setProperty(ServerConstant.PLAYER_INFO, pInfo);\n";
}
