package service;

import bitzero.server.BitZeroServer;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;

import cmd.CmdDefine;

import cmd.ErrorConst;
import cmd.receive.user.RequestTrainingCreate;
import cmd.receive.user.RequestUserInfo;


import cmd.send.user.ResponseGetMapInfo;
import cmd.send.user.ResponseGetUserInfo;

import model.PlayerInfo;

import org.apache.commons.lang.exception.ExceptionUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.server.ServerConstant;

import java.util.List;

public class TroopHandler extends BaseClientRequestHandler {
    public static short TROOP_MULTI_IDS = 5000;
    private final Logger logger = LoggerFactory.getLogger("UserHandler");

    public TroopHandler() {
        super();
    }

    public void init() {

    }

    public void handleClientRequest(User user, DataCmd dataCmd) {
        System.out.println("requestId: " + dataCmd.getId());
        logger.info("requestId: " + dataCmd.getId());
        try {
            switch (dataCmd.getId()) {
                case CmdDefine.TRAIN_TROOP_CREATE:
                    RequestTrainingCreate reqInfo = new RequestTrainingCreate(dataCmd);
                    trainTroop(user, reqInfo);
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

    private void trainTroop(User user, RequestTrainingCreate reqInfo) {

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


}
