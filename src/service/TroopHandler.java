package service;

import bitzero.server.BitZeroServer;
import bitzero.server.core.BZEventParam;
import bitzero.server.core.BZEventType;
import bitzero.server.core.IBZEvent;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;

import cmd.CmdDefine;

import cmd.ErrorConst;
import cmd.receive.user.RequestUserInfo;

import cmd.send.demo.user.ResponseGetMapInfo;
import cmd.send.demo.user.ResponseGetUserInfo;

import event.eventType.DemoEventParam;
import event.eventType.DemoEventType;
import extension.FresherExtension;

import model.PlayerInfo;

import org.apache.commons.lang.exception.ExceptionUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.server.ServerConstant;

import java.util.List;

public class TroopHandler extends BaseClientRequestHandler {
    public static short TROOP_MULTI_IDS = 3000;
    private final Logger logger = LoggerFactory.getLogger("UserHandler");

    public TroopHandler() {
        super();
    }

    public void init() {

    }

    public void handleClientRequest(User user, DataCmd dataCmd) {
        logger.info("requestId: " + dataCmd.getId());
        try {
            switch (dataCmd.getId()) {
                case CmdDefine.TRAIN_TROOP:
                    RequestUserInfo reqInfo = new RequestUserInfo(dataCmd);
                    trainTroop(user);
                    break;
//                case CmdDefine.GET_MAP_INFO:
//                    getMapInfo(user);
//                    break;
            }
        } catch (Exception e) {
            logger.warn("USERHANDLER EXCEPTION " + e.getMessage());
            logger.warn(ExceptionUtils.getStackTrace(e));
        }

    }

    private void trainTroop(User user) {
        try {
            //get user from cache
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (userInfo == null) {
                send(new ResponseGetUserInfo(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            send(new ResponseGetUserInfo(ErrorConst.SUCCESS, userInfo), user);
        } catch (Exception e) {
            send(new ResponseGetUserInfo(ErrorConst.UNKNOWN), user);
        }

    }

    private void getMapInfo(User user) {
        try {
            //get user from cache
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (userInfo == null) {
                send(new ResponseGetMapInfo(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            send(new ResponseGetMapInfo(ErrorConst.SUCCESS, userInfo), user);
        } catch (Exception e) {
            send(new ResponseGetMapInfo(ErrorConst.UNKNOWN), user);
        }
    }

    private void userDisconnect(User user) {
        // log user disconnect
    }

    private void userChangeName(User user, String name) {
        List<User> allUser = BitZeroServer.getInstance().getUserManager().getAllUsers();
        for (User aUser : allUser) {
            // notify user's change
        }
    }
}
