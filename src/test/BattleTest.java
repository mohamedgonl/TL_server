package test;

import battle_models.BattleAction;
import bitzero.engine.sessions.*;
import bitzero.server.BitZeroServer;
import bitzero.server.Main;
import bitzero.server.config.ConfigHandle;
import bitzero.server.config.ServerSettings;
import bitzero.server.core.BZEventType;
import bitzero.server.entities.User;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.ExtensionUtility;
import bitzero.util.socialcontroller.bean.UserInfo;

import cmd.receive.battle.RequestSendAction;
import extension.FresherExtension;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import service.battle.BattleHandler;
import util.BattleConst;
import util.server.ServerConstant;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BattleTest {

    @Test
    public void trueTest() {
        Main.main(new String[0]);



    }

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

            BattleAction action = new BattleAction(BattleConst.ACTION_START, 0);
            BattleHandler.getInstance().handleClientRequest(new DummyUser(new DummySession()), new DataCmd(RequestSendAction.createByteBuffer(action).array()));


//            BZEventType.USER_LOGIN

            UserInfo uInfo = extension.getUserInfo("username_" + TestConstant.USER_ID, TestConstant.USER_ID, "127.0.0.1");
            assertEquals(TestConstant.USER_ID +"", uInfo.getUserId(),
                    "\nChange setting conf/cluster.properties -> custom_login to : 2");
        }catch(Exception e){
            assertTrue(false,
                    "\nError:\n"
                    + ExceptionUtils.getStackTrace(e));
        }

    }

    private class DummyUser extends User {

        public DummyUser(ISession session) {
            super(session);
        }
    }

    private class DummySession implements ISession {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public void setId(int i) {

        }

        @Override
        public String getHashId() {
            return null;
        }

        @Override
        public void setHashId(String s) {

        }

        @Override
        public SessionType getType() {
            return null;
        }

        @Override
        public void setType(SessionType sessionType) {

        }

        @Override
        public String getNodeId() {
            return null;
        }

        @Override
        public void setNodeId(String s) {

        }

        @Override
        public boolean isLocal() {
            return false;
        }

        @Override
        public boolean isLoggedIn() {
            return false;
        }

        @Override
        public void setLoggedIn(boolean b) {

        }

        @Override
        public IPacketQueue getPacketQueue() {
            return null;
        }

        @Override
        public void setPacketQueue(IPacketQueue iPacketQueue) {

        }

        @Override
        public SocketChannel getConnection() {
            return null;
        }

        @Override
        public void setConnection(SocketChannel socketChannel) {

        }

        @Override
        public DatagramChannel getDatagramChannel() {
            return null;
        }

        @Override
        public void setDatagrmChannel(DatagramChannel datagramChannel) {

        }

        @Override
        public long getCreationTime() {
            return 0;
        }

        @Override
        public void setCreationTime(long l) {

        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public void setConnected(boolean b) {

        }

        @Override
        public long getLastActivityTime() {
            return 0;
        }

        @Override
        public void setLastActivityTime(long l) {

        }

        @Override
        public long getLastLoggedInActivityTime() {
            return 0;
        }

        @Override
        public void setLastLoggedInActivityTime(long l) {

        }

        @Override
        public long getLastReadTime() {
            return 0;
        }

        @Override
        public void setLastReadTime(long l) {

        }

        @Override
        public long getLastWriteTime() {
            return 0;
        }

        @Override
        public void setLastWriteTime(long l) {

        }

        @Override
        public long getReadBytes() {
            return 0;
        }

        @Override
        public void addReadBytes(long l) {

        }

        @Override
        public long getWrittenBytes() {
            return 0;
        }

        @Override
        public void addWrittenBytes(long l) {

        }

        @Override
        public int getDroppedMessages() {
            return 0;
        }

        @Override
        public void addDroppedMessages(int i) {

        }

        @Override
        public int getMaxIdleTime() {
            return 0;
        }

        @Override
        public void setMaxIdleTime(int i) {

        }

        @Override
        public int getMaxLoggedInIdleTime() {
            return 0;
        }

        @Override
        public void setMaxLoggedInIdleTime(int i) {

        }

        @Override
        public boolean isMarkedForEviction() {
            return false;
        }

        @Override
        public void setMarkedForEviction() {

        }

        @Override
        public boolean isIdle() {
            return false;
        }

        @Override
        public boolean isFrozen() {
            return false;
        }

        @Override
        public void freeze() {

        }

        @Override
        public void unfreeze() {

        }

        @Override
        public long getFreezeTime() {
            return 0;
        }

        @Override
        public boolean isReconnectionTimeExpired() {
            return false;
        }

        @Override
        public Object getSystemProperty(String s) {
            return null;
        }

        @Override
        public void setSystemProperty(String s, Object o) {

        }

        @Override
        public void removeSystemProperty(String s) {

        }

        @Override
        public Object getProperty(String s) {
            return null;
        }

        @Override
        public void setProperty(String s, Object o) {

        }

        @Override
        public void removeProperty(String s) {

        }

        @Override
        public String getFullIpAddress() {
            return null;
        }

        @Override
        public String getAddress() {
            return null;
        }

        @Override
        public int getClientPort() {
            return 0;
        }

        @Override
        public String getServerAddress() {
            return null;
        }

        @Override
        public int getServerPort() {
            return 0;
        }

        @Override
        public String getFullServerIpAddress() {
            return null;
        }

        @Override
        public ism getSessionManager() {
            return null;
        }

        @Override
        public void setSessionManager(ism ism) {

        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public int getReconnectionSeconds() {
            return 0;
        }

        @Override
        public void setReconnectionSeconds(int i) {

        }

        @Override
        public boolean isMobile() {
            return false;
        }

        @Override
        public void setMobile(boolean b) {

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
