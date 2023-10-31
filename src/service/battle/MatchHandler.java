package service.battle;

import battle_models.BattleBuilding;
import battle_models.BattleMatch;
import bitzero.server.entities.User;
import cmd.ErrorConst;
import cmd.receive.battle.RequestEndGame;
import cmd.send.battle.ResponseEndGame;
import cmd.send.battle.ResponseMatchingPlayer;
import model.Building;
import model.ListPlayerData;
import model.PlayerInfo;
import util.BattleConst;
import util.Common;
import util.server.CustomException;
import util.server.ServerConstant;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class MatchHandler {
    public static ResponseMatchingPlayer createMatch(User user) throws CustomException {
        //get user from cache
        PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

        // check có đang trong trận không
        if (isInAMatch(user)) {
            throw new CustomException(ErrorConst.IN_A_MATCH);
        }

        // không có lính
        if (userInfo.getCurrentSpace() == 0) {
            System.out.println("troop empty");
            throw new CustomException(ErrorConst.TROOP_LIST_EMPTY);
        }
        // không đủ tiền tạo trận
        if (userInfo.getGold() - BattleConst.MatchingGoldCost < 0) {
            System.out.println("resource not enough");
            throw new CustomException(ErrorConst.NOT_ENOUGH_MATCHING_COST);
        }

        userInfo.setGold(userInfo.getGold() - BattleConst.MatchingGoldCost);

        try {
            userInfo.saveModel(user.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PlayerInfo enemyInfo;

        enemyInfo = executeInMaxTime(() -> {
            return getPlayerSameRank(user, 50);
        }, 5);

        if (enemyInfo == null) {
            enemyInfo = executeInMaxTime(() -> {
                return getPlayerSameRank(user, 100);
            }, 10);
        }

        if (enemyInfo == null) {
            enemyInfo = getPlayerSameRank(user, 200);
        }
        if (enemyInfo == null) {
            // không tìm được trận
            System.out.println("cant found enemy");
            throw new CustomException(ErrorConst.CANT_GET_MATCH);
        }

        ArrayList<BattleBuilding> buildings = convertToBattleBuilding(enemyInfo.getListBuildings());

        Map<String, Integer> army = userInfo.getListTroops();

        // Tạo 1 match với các data trên
        BattleMatch newMatch = new BattleMatch(enemyInfo.getId(), enemyInfo.getName(),
                buildings,
                army,
                (int) (enemyInfo.getGold() * BattleConst.RESOURCE_RATE),
                (int) (enemyInfo.getElixir() * BattleConst.RESOURCE_RATE));

        user.setProperty(ServerConstant.MATCH, newMatch);
        return new ResponseMatchingPlayer(ErrorConst.SUCCESS, newMatch, userInfo);
    }

    public static <T> T executeInMaxTime(Callable<T> function, long time) {
        Future<T> future = Executors.newSingleThreadExecutor().submit(function);
        try {
            return future.get(time, TimeUnit.SECONDS);
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

    public static ArrayList<BattleBuilding> convertToBattleBuilding(ArrayList<Building> buildings) {
        ArrayList<BattleBuilding> battleBuildings = new ArrayList<>();
        for (Building building : buildings) {
            battleBuildings.add(BattleBuilding.convertFromCityBuilding(building));
        }
        return battleBuildings;
    }

    public static PlayerInfo getPlayerSameRank(User user, int range) {
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

    private static boolean isInAMatch(User user) {
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
        System.out.println("check match state ::: " + match.state);
        return match.state == BattleConst.MATCH_HAPPENING;
    }

    public static ResponseEndGame handleEndGame(User user, RequestEndGame requestEndGame) {
        PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
        userInfo.addResources(requestEndGame.getGoldGot(), requestEndGame.getElixirGot(), 0);
        userInfo.setRank(userInfo.getRank() + requestEndGame.getTrophy());
        userInfo.removeTroop(requestEndGame.getArmy());
        try {
            userInfo.saveModel(user.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new  ResponseEndGame(ErrorConst.SUCCESS);
    }
}
