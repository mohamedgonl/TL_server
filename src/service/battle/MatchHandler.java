package service.battle;

import battle_models.BattleAction;
import battle_models.BattleBuilding;
import battle_models.BattleMatch;
import bitzero.server.entities.User;
import cmd.ErrorConst;
import cmd.receive.battle.RequestEndGame;
import cmd.receive.battle.RequestGetMatch;
import cmd.send.battle.ResponseEndGame;
import cmd.send.battle.ResponseGetMatch;
import cmd.send.battle.ResponseMatchingPlayer;
import model.Building;
import model.ListPlayerData;
import model.PlayerInfo;
import util.BattleConst;
import util.Common;
import util.database.DataModel;
import util.server.CustomException;
import util.server.ServerConstant;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class MatchHandler  {
    public static ResponseMatchingPlayer createMatch(User user) throws Exception {
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
            enemyInfo = executeInMaxTime(() -> {
                return getPlayerSameRank(user, 100);
            }, 50);
        }
        if (enemyInfo == null) {
            // không tìm được trận
            System.out.println("cant found enemy");
            throw new CustomException(ErrorConst.CANT_GET_MATCH);
        }

        ArrayList<BattleBuilding> buildings = convertToBattleBuilding(enemyInfo.getListBuildings());
        boolean hasElixirSto = buildings.stream().anyMatch(building -> "STO_2".equals(building.type) || "RES_2".equals(building.type));

        Map<String, Integer> army = userInfo.getListTroops();

        // Tạo 1 match với các data trên
        BattleMatch newMatch = new BattleMatch(userInfo.getBattleMatches().size(), enemyInfo.getId(), enemyInfo.getName(),
                buildings,
                army,
                (int) (enemyInfo.getGold() * BattleConst.RESOURCE_RATE),
                hasElixirSto?(int) (enemyInfo.getElixir() * BattleConst.RESOURCE_RATE):0);

        // update enemy state
        ListPlayerData listUserData = (ListPlayerData) ListPlayerData.getModel(ServerConstant.LIST_USER_DATA_ID, ListPlayerData.class);
        listUserData.updateUser(newMatch.enemyId, true);
        listUserData.saveModel(ServerConstant.LIST_USER_DATA_ID);


        user.setProperty(ServerConstant.MATCH, newMatch);
        newMatch.saveModel(user.getId());
        userInfo.saveModel(user.getId());
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
            PlayerInfo playerInfo = listUserData.getRandomPlayerInRangeRank(user.getId(), userRank - range, userRank + range);
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

        try {
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
            userInfo.addResources(requestEndGame.getGoldGot(), requestEndGame.getElixirGot(), 0);
            userInfo.setRank(userInfo.getRank() + requestEndGame.getTrophy());
            userInfo.removeTroop(requestEndGame.getArmy());

            BattleMatch match = (BattleMatch) user.getProperty(ServerConstant.MATCH);
            if (match != null) {
                match.isWin = requestEndGame.getResult();
                match.trophy = requestEndGame.getTrophy();
                match.stars = requestEndGame.getStars();
                match.setGoldGot(requestEndGame.getGoldGot());
                match.setElixirGot(requestEndGame.getElixirGot());
                match.pushAction(new BattleAction(BattleConst.ACTION_END, requestEndGame.getTick()));

                ListPlayerData listUserData = (ListPlayerData) ListPlayerData.getModel(ServerConstant.LIST_USER_DATA_ID, ListPlayerData.class);
                listUserData.updateUser(match.enemyId, false);
                listUserData.saveModel(ServerConstant.LIST_USER_DATA_ID);
            }
            userInfo.pushNewMatch(match);
            userInfo.saveModel(user.getId());
            match.saveModel(match.id);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResponseEndGame(ErrorConst.SUCCESS);
    }

    public static ResponseGetMatch handleGetMatch(User user, RequestGetMatch requestGetMatch) throws Exception {
        PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
        ArrayList<BattleMatch> matches = userInfo.getBattleMatches();

        for (BattleMatch match : matches) {
            if (match.id == requestGetMatch.getMatchId()) {
                return new ResponseGetMatch(ErrorConst.SUCCESS, match);
            }
        }
        throw  new CustomException(ErrorConst.NO_MATCH_FOUND);
    }

}
