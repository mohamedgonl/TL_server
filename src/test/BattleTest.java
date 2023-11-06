package test;

import battle_models.BattleAction;
import battle_models.BattleMatch;
import bitzero.engine.sessions.ISession;
import bitzero.engine.sessions.Session;
import bitzero.engine.sessions.SessionType;
import bitzero.server.BitZeroServer;
import bitzero.server.config.ConfigHandle;

import bitzero.server.config.ServerSettings;
import bitzero.server.core.BZEvent;
import bitzero.server.core.BZEventParam;
import bitzero.server.core.BZEventType;
import bitzero.server.entities.User;
import bitzero.server.entities.managers.BZUserManager;
import bitzero.server.exceptions.BZException;
import bitzero.server.extensions.IClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;
import bitzero.server.util.ByteArray;
import bitzero.util.ExtensionUtility;
import bitzero.util.socialcontroller.bean.UserInfo;

import cmd.CmdDefine;
import cmd.receive.battle.RequestEndGame;
import cmd.receive.battle.RequestGetMatch;
import cmd.receive.battle.RequestSendAction;
import cmd.send.battle.*;
import event.handler.LoginSuccessHandler;
import extension.FresherExtension;
import javafx.beans.binding.ObjectBinding;
import model.PlayerInfo;
import model.TrainingItem;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import service.DemoHandler;
import service.battle.ActionHandler;
import service.battle.BattleHandler;
import service.battle.MatchHandler;
import util.BattleConst;
import util.server.ServerConstant;

import java.awt.*;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
import static test.TestConstant.CHANGE_POS;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BattleTest {


    /**
     * Description:
     * -
     * <p>
     * <p>
     * Requirement:
     * -
     * <p>
     * Actions:
     * -
     */
    @Test
    @Order(1)
    void checkConfigDatabase() {
        assertEquals("127.0.0.1:11211", ConfigHandle.instance().get("dservers"),
                TestConstant.ERROR_MSG_DATABASE);

    }

    @Test
    @Order(0)
    void initFramework() {

        BitZeroServer localBitZeroServer = BitZeroServer.getInstance();
        localBitZeroServer.setClustered(false);
        localBitZeroServer.start();

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

//    @Test
//    @Order(4)
//    void checkConfigListenedServer(){
//        for(Iterator iterator = BitZeroServer.getInstance().getConfigurator().getServerSettings().socketAddresses.iterator(); iterator.hasNext();)
//        {
//            bitzero.server.config.ServerSettings.SocketAddress addr = (ServerSettings.SocketAddress)iterator.next();
//
//            assertEquals(TestConstant.SERVER_ADDR, addr.address, TestConstant.ERROR_MSG_LISTEN_ADDR);
//            assertEquals(TestConstant.SERVER_PORT, addr.port, TestConstant.ERROR_MSG_LISTEN_PORT);
//
//        }
//
//    }

    @Test
    @Order(2)
    void validateUser() {

        try {
            FresherExtension extension = (FresherExtension) BitZeroServer.getInstance().getExtensionManager().getMainExtension();
            UserInfo uInfo = extension.getUserInfo("username_" + TestConstant.USER_ID, TestConstant.USER_ID, "127.0.0.1");
            assertEquals(TestConstant.USER_ID + "", uInfo.getUserId(),
                    "\nChange setting conf/cluster.properties -> custom_login to : 2");
        } catch (Exception e) {
            assertTrue(false,
                    "\nError:\n"
                            + ExceptionUtils.getStackTrace(e));
        }

    }


    @Test
    @Order(3)
    void checkCacheInRAM() {
        try {
            int userId = this.createUser(TestConstant.USER_ID).getId();
            Thread.sleep(500L);

            User uNew = BitZeroServer.getInstance().getUserManager().getUserById(userId);
            Object userInfo = uNew.getProperty(ServerConstant.PLAYER_INFO);
            assertNotEquals(null, userInfo, TestConstant.ERROR_MSG_CACHE_IN_RAM);

        } catch (Exception e) {
            assertTrue(false,
                    "\nError:\n"
                            + ExceptionUtils.getStackTrace(e));
        }
    }

//    @Test
//    @Order(4)
    void createUsersForTest(){
        try {
            for (int i = 1; i <= 10000; i++) {
                this.createUser(i);
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    @Test
    @Order(4)
    void testMatching() {
        try {
            ByteArray byteArray = new ByteArray();
            DataCmd dataCmd = new DataCmd(byteArray.getBytes());
            dataCmd.setId(CmdDefine.BATTLE_MATCHING);

            User user = BitZeroServer.getInstance().getUserManager().getUserById(TestConstant.USER_ID);
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            Map<String, Integer> troopList = new HashMap<>();
            troopList.put("ARM_1", 3);
            troopList.put("ARM_2", 4);

            playerInfo.setListTroops(troopList);


            ResponseMatchingPlayer responseMatchingPlayer = MatchHandler.createMatch(user);

            System.out.println(responseMatchingPlayer.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    @Order(5)
    void testSendAction() {
        try {
            User user = BitZeroServer.getInstance().getUserManager().getUserById(TestConstant.USER_ID);

            ActionHandler.handleReceiveAction(user,
                    this.createSendActionRq(BattleConst.ACTION_THROW_TROOP, 50, "ARM_1", 5, 12));

            ActionHandler.handleReceiveAction(user,
                    this.createSendActionRq(BattleConst.ACTION_END, 120));

            System.out.println();

        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Test
    @Order(6)
    void testEndGame() {
        try {

            ArrayList<TrainingItem> troops = new ArrayList<>();
            troops.add(new TrainingItem("ARM_1", 3));
            troops.add(new TrainingItem("ARM_2", 4));
            RequestEndGame requestEndGame = this.createRequestEndGame(2,20,12,21,144,troops);

            int userId = getRandomNumber(1, 1000);
            User u = BitZeroServer.getInstance().getUserManager().getUserById(TestConstant.USER_ID);

            u.getProperty(ServerConstant.MATCH);

            try {
                ResponseEndGame responseEndGame = MatchHandler.handleEndGame(u, requestEndGame);

            } catch (Exception ef) {
                System.err.println(ef.getMessage());
            }


        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    @Order(9)
    void testGetHistoryAttack () {
        try {
            ByteArray byteArray = new ByteArray();
            DataCmd dataCmd = new DataCmd(byteArray.getBytes());
            dataCmd.setId(CmdDefine.GET_HISTORY_ATTACK);

            User u = BitZeroServer.getInstance().getUserManager().getUserById(TestConstant.USER_ID);

            ResponseGetHistoryAttack responseGetHistoryAttack = MatchHandler.handleGetHistoryAttack(u);
            System.out.println(responseGetHistoryAttack.toString());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


    @Test
    @Order(10)
    void checkGetMatch() {

        try {

            ByteArray byteArray = new ByteArray();
            byteArray.writeInt(1);

            DataCmd dataCmd = new DataCmd(byteArray.getBytes());
            dataCmd.setId(CmdDefine.GET_MATCH);

//            int userId = getRandomNumber(1,10000);

            User u = BitZeroServer.getInstance().getUserManager().getUserById(TestConstant.USER_ID);


            try {
                ResponseGetMatch responseGetMatch = MatchHandler.handleGetMatch(u, new RequestGetMatch(dataCmd));
                System.out.println(responseGetMatch.toString());
            } catch (Exception ef) {
                System.err.println(ef.getMessage());
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }


    }

//    @Test
//    @Order(11)
//    void checkDispatchEvent(){
//        assertTrue(false);
//
//    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private User createUser(int userId) throws Exception {

        ISession dmnSession = new Session();
        dmnSession.setType(SessionType.VOID);

        FresherExtension extension = (FresherExtension) BitZeroServer.getInstance().getExtensionManager().getMainExtension();

        UserInfo uInfo = extension.getUserInfo("username_" + userId, userId, "127.0.0.1");
        User u = ExtensionUtility.instance().canLogin(uInfo, "", dmnSession);
        BitZeroServer.getInstance().getUserManager().addUser(u);

        Map<Object, Object> evtParams = new HashMap<>();
        evtParams.put(BZEventParam.USER, u);
        ExtensionUtility.dispatchEvent(new BZEvent(BZEventType.USER_LOGIN, evtParams));
        Thread.sleep(1);

        u.setProperty("userId", uInfo.getUserId());

        return u;
    }

    private RequestSendAction createSendActionRq(int actionType, int tick) throws BZException {
        ByteArray byteArray = new ByteArray();

        byteArray.writeInt(actionType);
        byteArray.writeInt(tick);


        DataCmd dataCmd = new DataCmd(byteArray.getBytes());

        dataCmd.setId(CmdDefine.SEND_ACTION);

        return new RequestSendAction(dataCmd);
    }

    private RequestSendAction createSendActionRq(int actionType, int tick, String troopType, int posX, int posY) throws BZException {
        ByteArray byteArray = new ByteArray();

        byteArray.writeInt(actionType);
        byteArray.writeInt(tick);
        byteArray.writeUTF(troopType);
        byteArray.writeInt(posX);
        byteArray.writeInt(posY);


        DataCmd dataCmd = new DataCmd(byteArray.getBytes());

        dataCmd.setId(CmdDefine.SEND_ACTION);

        return new RequestSendAction(dataCmd);
    }

    private RequestEndGame createRequestEndGame (int stars, int trophy, int goldGot, int elixirGot, int tick, ArrayList<TrainingItem> troops) throws BZException {
        boolean result = false;

        ByteArray byteArray = new ByteArray();

        byteArray.writeInt(result ? 1 : 0);
        byteArray.writeInt(stars);
        byteArray.writeInt(trophy);
        byteArray.writeInt(goldGot);
        byteArray.writeInt(elixirGot);

        byteArray.writeInt(troops.size());
        if (!troops.isEmpty()) {
            for (int i = 0; i < troops.size(); i++) {
                byteArray.writeUTF(troops.get(i).cfgId);
                byteArray.writeInt(troops.get(i).count);
            }
        }

        byteArray.writeInt(tick);
        byteArray.writeDouble(32);

        DataCmd dataCmd = new DataCmd(byteArray.getBytes());
        dataCmd.setId(CmdDefine.END_GAME);

        return new RequestEndGame(dataCmd);
    }

}
