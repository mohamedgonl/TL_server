package service.battle;

import battle_models.BattleBuilding;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;
import cmd.CmdDefine;
import cmd.ErrorConst;
import cmd.receive.battle.RequestSendAction;
import cmd.send.battle.ResponseMatchingPlayer;
import cmd.send.battle.ResponseSendAction;
import model.Building;
import model.ListPlayerData;
import battle_models.BattleMatch;
import model.PlayerInfo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.BattleConst;
import util.Common;
import util.server.ServerConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

public class BattleHandler extends BaseClientRequestHandler {
    public static short BATTLE_MULTI_IDS = 6000;
    private final Logger logger = LoggerFactory.getLogger("UserHandler");

    public BattleHandler() {
        super();
    }

    public void init() {

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
            }

        } catch (Exception e) {
            logger.warn("BATTLE HANDLER EXCEPTION " + e.getMessage());
            logger.warn(ExceptionUtils.getStackTrace(e));
        }


    }


    public void handleMatchingPlayers(User user) {
        System.out.println("HANDLE MATCHING PLAYER START");
        try {
            //get user from cache
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            // check có đang trong trận không
            if(this.isInAMatch(user)) {
                send(new ResponseMatchingPlayer(ErrorConst.IN_A_MATCH), user);
                return;
            }

            // không có lính
            if (userInfo.getCurrentSpace() == 0) {
                send(new ResponseMatchingPlayer(ErrorConst.TROOP_LIST_EMPTY), user);
                return;
            }
            // không đủ tiền tạo trận
            if (userInfo.getGold() - BattleConst.MatchingGoldCost < 0) {
                send(new ResponseMatchingPlayer(ErrorConst.NOT_ENOUGH_MATCHING_COST), user);
                return;
            }

            userInfo.setGold(userInfo.getGold() - BattleConst.MatchingGoldCost);
            userInfo.saveModel(user.getId());

            PlayerInfo enemyInfo;

            enemyInfo = this.executeInMaxTime(() -> {
                return this.getPlayerSameRank(user, 50);
            }, 5000);

            if (enemyInfo == null) {
                enemyInfo = this.executeInMaxTime(() -> {
                    return this.getPlayerSameRank(user, 100);
                }, 10000);
            }

            if (enemyInfo == null) {
                enemyInfo = this.getPlayerSameRank(user, 200);
            }
            if (enemyInfo == null) {
                // không tìm được trận
                send(new ResponseMatchingPlayer(ErrorConst.CANT_GET_MATCH), user);
                return;
            }

            //TODO: convert city map => battle map
            ArrayList<BattleBuilding> buildings = this.convertToBattleBuilding(enemyInfo.getListBuildings());

            //TODO: Lấy list troops
            Map<String, Integer> army = userInfo.getListTroops();

            //TODO: Tạo 1 match với các data trên
            BattleMatch newMatch = new BattleMatch(enemyInfo.getId(), enemyInfo.getName(),
                    buildings,
                    army,
                    (int) (enemyInfo.getGold() * BattleConst.RESOURCE_RATE),
                    (int) (enemyInfo.getElixir() * BattleConst.RESOURCE_RATE));

            user.setProperty(ServerConstant.MATCH, newMatch);
            send(new ResponseMatchingPlayer(ErrorConst.SUCCESS, newMatch, userInfo), user);

        } catch (Exception e) {
            System.out.println("HANDLE MATCHING PLAYER ERROR :: " + e.getMessage());
            send(new ResponseMatchingPlayer(ErrorConst.UNKNOWN), user);
        } finally {
            System.out.println("HANDLE MATCHING PLAYER END");
        }
    }


    public void handleReceiveAction(User user, RequestSendAction requestSendAction) {
        System.out.println("HANDLE SEND ACTION START");
        try {
            BattleMatch match = (BattleMatch) user.getProperty(ServerConstant.MATCH);
            ListPlayerData listPlayerData = (ListPlayerData) ListPlayerData.getModel(ServerConstant.LIST_USER_DATA_ID, ListPlayerData.class);

            // Match ended
            if (match.state == BattleConst.MATCH_ENDED) {
                send(new ResponseSendAction(ErrorConst.MATCH_ENDED), user);
                return;
            }

            // action start game
            if (requestSendAction.getAction().type == BattleConst.ACTION_START && match.state == BattleConst.MATCH_NEW) {
                match.state = BattleConst.MATCH_HAPPENING;
                match.startTime = Common.currentTimeInSecond();

            } else {
                send(new ResponseSendAction(ErrorConst.BATTLE_ACTION_INVALID), user);
                return;
            }


            // action thả lính
            if (requestSendAction.getAction().type == BattleConst.ACTION_THROW_TROOP && match.state == BattleConst.MATCH_HAPPENING) {
                // trận đấu đã kết thúc, không nhận action thả lính
                if (Common.currentTimeInSecond() > match.startTime + BattleConst.MAX_TIME_A_MATCH) {
                    match.state = BattleConst.MATCH_ENDED;
                    send(new ResponseSendAction(ErrorConst.MATCH_ENDED), user);
                    user.setProperty(ServerConstant.MATCH, match);
                }
                // nếu đã thả hết lính => không lưu action
                else if (!match.checkValidActionThrowTroop(requestSendAction.getAction())) {
                    send(new ResponseSendAction(ErrorConst.TROOP_EMPTY), user);
                    return;
                }
            }

            // action kết thúc trận
            if (requestSendAction.getAction().type == BattleConst.ACTION_END && match.state == BattleConst.MATCH_ENDED) {
                send(new ResponseSendAction(ErrorConst.BATTLE_ACTION_INVALID), user);
                return;
            }

            match.pushAction(requestSendAction.getAction());
            user.setProperty(ServerConstant.MATCH, match);
            send(new ResponseSendAction(ErrorConst.SUCCESS), user);
        } catch (Exception e) {
            System.out.println("HANDLE RECEIVE ACTION ERROR :: " + e.getMessage());
            send(new ResponseSendAction(ErrorConst.UNKNOWN), user);
        } finally {
            System.out.println("HANDLE SEND ACTION END");
        }
    }

    // helpers

    public PlayerInfo getPlayerSameRank(User user, int range) {
        try {

            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
            int userRank = userInfo.getRank();
            ListPlayerData listUserData = (ListPlayerData) ListPlayerData.getModel(ServerConstant.LIST_USER_DATA_ID, ListPlayerData.class);
            PlayerInfo playerInfo = listUserData.getRandomPlayerInRangeRank(userRank - range, userRank + range);
            return playerInfo;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public <T> T executeInMaxTime(Callable<T> function, long time) {
        Future<T> future = Executors.newSingleThreadExecutor().submit(function);
        try {
            return future.get(time, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Luồng chính bị hủy.");
        } catch (ExecutionException e) {
            System.err.println("Lỗi xảy ra trong hàm.");
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Công việc bị hủy sau " + time + " ms.");
        }
        return null;
    }


    public ArrayList<BattleBuilding> convertToBattleBuilding(ArrayList<Building> buildings) {
        ArrayList<BattleBuilding> battleBuildings = new ArrayList<>();
        for (Building building : buildings) {
            battleBuildings.add(BattleBuilding.convertFromCityBuilding(building));
        }
        return battleBuildings;
    }

    private boolean isInAMatch(User user) {
        BattleMatch match = (BattleMatch) user.getProperty(ServerConstant.MATCH);

        if (match == null) return false;

        else if (match.state == BattleConst.MATCH_NEW
                && match.createTime + BattleConst.MAX_TIME_A_MATCH > Common.currentTimeInSecond()
                && match.createTime + BattleConst.COUNT_DOWN_TIME <= Common.currentTimeInSecond()) {
            match.state = BattleConst.MATCH_HAPPENING;
        } else if (match.state == BattleConst.MATCH_NEW && match.createTime + BattleConst.MAX_TIME_A_MATCH < Common.currentTimeInSecond()) {
            match.state = BattleConst.MATCH_ENDED;
        }

        user.setProperty(ServerConstant.MATCH, match);

        return match.state == BattleConst.MATCH_HAPPENING;
    }


}
