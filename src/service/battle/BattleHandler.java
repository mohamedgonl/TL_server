package service.battle;

import battle_models.BattleBuilding;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;
import cmd.CmdDefine;
import cmd.ErrorConst;
import cmd.receive.battle.RequestEndGame;
import cmd.receive.battle.RequestGetMatch;
import cmd.receive.battle.RequestSendAction;
import cmd.send.battle.ResponseEndGame;

import cmd.send.battle.ResponseGetMatch;
import cmd.send.battle.ResponseMatchingPlayer;
import cmd.send.battle.ResponseSendAction;
import cmd.send.user.ResponseGetMapInfo;
import model.Building;
import model.ListPlayerData;
import battle_models.BattleMatch;
import model.PlayerInfo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.BattleConst;
import util.Common;
import util.server.CustomException;
import util.server.ServerConstant;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

public class BattleHandler extends BaseClientRequestHandler {
    public static short BATTLE_MULTI_IDS = 6000;
    public static BattleHandler instance;
    private static final String logPrefix = "-----------------------";
    private final Logger logger = LoggerFactory.getLogger("UserHandler");

    public BattleHandler() {
        super();
    }

    public void init() {
        instance = this;
    }

    public void handleClientRequest(User user, DataCmd dataCmd) {
        logger.info("requestId: " + dataCmd.getId());
        try {
            switch (dataCmd.getId()) {
                case CmdDefine.BATTLE_MATCHING: {
                    handleMatchingPlayers(user);
                    break;
                }
                case CmdDefine.SEND_ACTION: {
                    RequestSendAction action = new RequestSendAction(dataCmd);
                    handleReceiveAction(user, action);
                    break;
                }
                case CmdDefine.END_GAME: {
                    RequestEndGame requestEndGame = new RequestEndGame(dataCmd);
                    handleEndGame(user, requestEndGame);
                    break;
                }
                case CmdDefine.GET_MATCH: {
                    RequestGetMatch requestGetMatch = new RequestGetMatch(dataCmd);
                    handleGetMatch(user,requestGetMatch);
                    break;
                }

            }

        } catch (Exception e) {
            logger.warn("BATTLE HANDLER EXCEPTION " + e.getMessage());
            logger.warn(ExceptionUtils.getStackTrace(e));
        }

    }


    public void handleMatchingPlayers(User user) {
        System.out.println(logPrefix + "HANDLE MATCHING PLAYER START" + logPrefix);
        try {
            ResponseMatchingPlayer responseMatchingPlayer = MatchHandler.createMatch(user);
            send(responseMatchingPlayer, user);
        }
        catch (CustomException e) {
            System.out.println("HANDLE MATCHING PLAYER FAIL WITH ERROR CODE : " + e.getErrorCode());
            send(new ResponseMatchingPlayer(e.getErrorCode()), user);
        }
        catch (Exception e) {
            System.out.println("HANDLE MATCHING PLAYER ERROR :: " + e.getMessage());
            send(new ResponseMatchingPlayer(ErrorConst.UNKNOWN), user);
        } finally {
            System.out.println(logPrefix + "HANDLE MATCHING PLAYER END" + logPrefix);
        }
    }


    public void handleReceiveAction(User user, RequestSendAction requestSendAction) {
        System.out.println("HANDLE SEND ACTION START");
        System.out.println(requestSendAction.toString());
        try {
            ResponseSendAction responseSendAction = ActionHandler.handleReceiveAction(user, requestSendAction);
            send(responseSendAction, user);
        }
        catch (CustomException e) {
            System.out.println("HANDLE RECEIVE ACTION FAIL with error code : " + e.getErrorCode());
            send(new ResponseSendAction(e.getErrorCode()), user);
        }
        catch (Exception e) {
            System.out.println("HANDLE RECEIVE ACTION ERROR :: " + e.getMessage());
            send(new ResponseSendAction(ErrorConst.UNKNOWN), user);
        } finally {
            System.out.println(logPrefix + "HANDLE SEND ACTION END" + logPrefix);
        }
    }

    public void handleGetMatch(User user, RequestGetMatch requestGetMatch) {
        System.out.println(logPrefix + "HANDLE GET MATCH START"+ logPrefix);
        System.out.println(requestGetMatch.toString());
        try {
            ResponseGetMatch response = MatchHandler.handleGetMatch(user, requestGetMatch);
            send(response, user);
        }
        catch (CustomException e) {
            System.out.println("HANDLE GET MATCH FAIL with error code : " + e.getErrorCode());
            send(new ResponseSendAction(e.getErrorCode()), user);
        }
        catch (Exception e) {
            System.out.println("HANDLE GET MATCH ERROR :: " + e.getMessage());
            send(new ResponseSendAction(ErrorConst.UNKNOWN), user);
        } finally {
            System.out.println(logPrefix + "HANDLE GET MATCH END" + logPrefix);
        }
    }

    public void handleEndGame(User user, RequestEndGame requestEndGame) {
        System.out.println(logPrefix + "HANDLE END GAME START" + logPrefix);
        System.out.println(requestEndGame.toString());
        try {
            ResponseEndGame response = MatchHandler.handleEndGame(user, requestEndGame);
            send(response, user);
        }
//        catch (CustomException e) {
//            System.out.println("HANDLE END GAME FAIL with error code : " + e.getErrorCode());
//            send(new ResponseSendAction(e.getErrorCode()), user);
//        }
        catch (Exception e) {
            System.out.println("HANDLE END GAME ERROR :: " + e.getMessage());
            send(new ResponseSendAction(ErrorConst.UNKNOWN), user);
        } finally {
            System.out.println(logPrefix + "HANDLE END GAME END" + logPrefix);
        }
    }










    public static BattleHandler getInstance() {
        return instance;
    }

}
