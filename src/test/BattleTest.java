package test;

import bitzero.engine.sessions.ISession;
import bitzero.engine.sessions.Session;
import bitzero.engine.sessions.SessionType;
import bitzero.server.BitZeroServer;
import bitzero.server.config.ConfigHandle;
import bitzero.server.config.ServerSettings;
import bitzero.server.entities.User;
import bitzero.util.ExtensionUtility;
import bitzero.util.socialcontroller.bean.UserInfo;

import extension.FresherExtension;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import util.server.ServerConstant;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BattleTest {



    /**
     * Description:
     * -
     *
     *
     * Requirement:
     * -
     *
     * Actions:
     * -
     */
    @Test
    @Order(5)
    void validateUser() {

        try{
            FresherExtension extension = (FresherExtension)BitZeroServer.getInstance().getExtensionManager().getMainExtension();
            UserInfo uInfo = extension.getUserInfo("username_" + TestConstant.USER_ID, TestConstant.USER_ID, "127.0.0.1");
            assertEquals(TestConstant.USER_ID +"", uInfo.getUserId(),
                    "\nChange setting conf/cluster.properties -> custom_login to : 2");
        }catch(Exception e){
            assertTrue(false,
                    "\nError:\n"
                    + ExceptionUtils.getStackTrace(e));
        }

    }


    @Test
    @Order(6)
    void checkCacheInRAM(){

        try{

            FresherExtension extension = (FresherExtension)BitZeroServer.getInstance().getExtensionManager().getMainExtension();
            int userId = TestConstant.USER_ID + 1;
            ISession dmnSession = new Session();
            dmnSession.setType(SessionType.VOID);

            UserInfo uInfo = extension.getUserInfo("username_" + userId, userId, "127.0.0.1");

            BitZeroServer.getInstance().getSessionManager().addSession(dmnSession);


            User u = ExtensionUtility.instance().canLogin(uInfo, "", dmnSession);
            //BitZeroServer.getInstance().getUserManager().addUser(u);

            u.setProperty("userId", uInfo.getUserId());
            Thread.sleep(500L);

            User uNew = BitZeroServer.getInstance().getUserManager().getUserById(userId);
            Object userInfo = uNew.getProperty(ServerConstant.PLAYER_INFO);
            assertNotEquals(null, userInfo, TestConstant.ERROR_MSG_CACHE_IN_RAM);

        }catch(Exception e){
            assertTrue(false,
                    "\nError:\n"
                            + ExceptionUtils.getStackTrace(e));
        }
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

    @Test
    @Order(1)
    void checkConfigDatabase(){
        assertEquals("127.0.0.1:11211", ConfigHandle.instance().get("dservers"),
                TestConstant.ERROR_MSG_DATABASE);

    }

    @Test
    @Order(4)
    void checkConfigListenedServer(){
        for(Iterator iterator = BitZeroServer.getInstance().getConfigurator().getServerSettings().socketAddresses.iterator(); iterator.hasNext();)
        {
            ServerSettings.SocketAddress addr = (ServerSettings.SocketAddress)iterator.next();

            assertEquals(TestConstant.SERVER_ADDR, addr.address, TestConstant.ERROR_MSG_LISTEN_ADDR);
            assertEquals(TestConstant.SERVER_PORT, addr.port, TestConstant.ERROR_MSG_LISTEN_PORT);

        }

    }

    @Test
    @Order(6)
    void testAddingUserToList() {

    }


}
