package extension;


import bitzero.engine.sessions.ISession;

import bitzero.server.config.ConfigHandle;
import bitzero.server.core.BZEventType;
import bitzero.server.entities.User;
import bitzero.server.entities.managers.ConnectionStats;
import bitzero.server.extensions.BZExtension;
import bitzero.server.extensions.data.DataCmd;

import bitzero.util.ExtensionUtility;
import bitzero.util.common.business.Debug;
import bitzero.util.datacontroller.business.DataController;
import bitzero.util.socialcontroller.bean.UserInfo;

import cmd.receive.authen.RequestLogin;

import event.handler.DisconnectHandler;
import event.handler.LoginSuccessHandler;
import event.handler.LogoutHandler;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.json.JSONObject;

import service.*;

import service.battle.BattleHandler;
import service.city.BuildingHandler;
import service.city.ShopHandler;
import service.city.TroopHandler;
import util.GuestLogin;

import util.GameConfig;
import util.metric.LogObject;
import util.metric.MetricLog;

import util.server.ServerConstant;
import util.server.ServerLoop;


public class FresherExtension extends BZExtension {
    private static String SERVERS_INFO =
        ConfigHandle.instance().get("servers_key") == null ? "servers" : ConfigHandle.instance().get("servers_key");

    private ServerLoop svrLoop;

    public FresherExtension() {
        super();
        setName("Fresher");
        svrLoop = new ServerLoop();
    }

    public void init() {
        /**
         * register new handler to catch client's packet
         */
        trace("  Register Handler ");
        addRequestHandler(UserHandler.USER_MULTI_IDS, UserHandler.class);
//        addRequestHandler(DemoHandler.DEMO_MULTI_IDS, DemoHandler.class);
        addRequestHandler(BuildingHandler.BUILDING_MULTI_IDS, BuildingHandler.class);
        addRequestHandler(ShopHandler.SHOP_MULTI_IDS, ShopHandler.class);
        addRequestHandler(TroopHandler.TROOP_MULTI_IDS, TroopHandler.class);
        addRequestHandler(BattleHandler.BATTLE_MULTI_IDS, BattleHandler.class);
        /**
         * register new event
         */
        trace(" Event Handler ");
        addEventHandler(BZEventType.USER_LOGIN, LoginSuccessHandler.class);
        addEventHandler(BZEventType.USER_LOGOUT, LogoutHandler.class);
        addEventHandler(BZEventType.USER_DISCONNECT, DisconnectHandler.class);
        //init data
        GameConfig.getInstance().init();

        //init
    }

    public ServerLoop getServerLoop() {
        return svrLoop;
    }

    @Override
    public void monitor() {
        try {
            ConnectionStats connStats = bz.getStatsManager().getUserStats();
            JSONObject data = new JSONObject();

            data.put("totalInPacket", bz.getStatsManager().getTotalInPackets());
            data.put("totalOutPacket", bz.getStatsManager().getTotalOutPackets());
            data.put("totalInBytes", bz.getStatsManager().getTotalInBytes());
            data.put("totalOutBytes", bz.getStatsManager().getTotalOutBytes());

            data.put("connectionCount", connStats.getSocketCount());
            data.put("totalUserCount", bz.getUserManager().getUserCount());

            DataController.getController().setCache(SERVERS_INFO, 60 * 5, data.toString());
        } catch (Exception e) {
            trace("Ex monitor");
        }
    }

    @Override
    public void destroy() {
        List<User> allUser = ExtensionUtility.globalUserManager.getAllUsers();
        if (allUser.size() == 0)
            return;

        User obj = null;

        for (int i = 0; i < allUser.size(); i++) {
            obj = allUser.get(i);
            // do sth with user
            LogObject logObject = new LogObject(LogObject.ACTION_LOGOUT);
            logObject.zingId = obj.getId();
            logObject.zingName = obj.getName();
            //System.out.println("Log logout = " + logObject.getLogMessage());
            MetricLog.writeActionLog(logObject);
        }
    }

    /**
     *
     * @param cmdId
     * @param session
     * @param objData
     *
     * the first packet send from client after handshake success will dispatch to doLogin() function
     */
    public void doLogin(short cmdId, ISession session, DataCmd objData) {
        System.out.println("doLogin 1");
        RequestLogin reqGet = new RequestLogin(objData);
        reqGet.unpackData();
        try {
            System.out.println("doLogin 2 req uId=" + reqGet.userId);

            UserInfo uInfo = getUserInfo(reqGet.sessionKey, reqGet.userId, session.getAddress());
            System.out.println("doLogin 3");
            User u = ExtensionUtility.instance().canLogin(uInfo, "", session);
            System.out.println("doLogin 4 u=" + (u==null?"null":u.getId()));

            if (u!=null)
                u.setProperty("userId", uInfo.getUserId());
        } catch (Exception e) {
            Debug.warn("DO LOGIN EXCEPTION " + e.getMessage());
            Debug.warn(ExceptionUtils.getStackTrace(e));
        }

    }

    public UserInfo getUserInfo(String username, int userId, String ipAddress) throws Exception {
        int customLogin = ServerConstant.CUSTOM_LOGIN;
        switch(customLogin){
            case 1: // login zingme
                return ExtensionUtility.getUserInfoFormPortal(username);
            case 2: // set direct userid
                return GuestLogin.setInfo(userId, "User_" + userId);
            default: // auto increment
                return GuestLogin.newGuest();
        }        
    }

}
