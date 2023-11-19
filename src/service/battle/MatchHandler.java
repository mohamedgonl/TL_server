package service.battle;

import battle_models.BattleAction;
import battle_models.BattleGameObject;
import battle_models.BattleMatch;
import bitzero.server.entities.User;
import cmd.ErrorConst;
import cmd.receive.battle.RequestEndGame;
import cmd.receive.battle.RequestGetMatch;
import cmd.send.battle.ResponseEndGame;
import cmd.send.battle.ResponseGetHistoryAttack;
import cmd.send.battle.ResponseGetMatch;
import cmd.send.battle.ResponseMatchingPlayer;
import model.Building;
import model.ListPlayerData;
import model.PlayerInfo;
import util.BattleConst;
import util.Common;
import util.server.CustomException;
import util.server.ServerConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class MatchHandler {
    public static ResponseMatchingPlayer createMatch(User user) throws Exception {
        //get user from cache
        PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

        // check có đang trong trận không
        if (isInAMatch(user)) {
            throw new CustomException(ErrorConst.IN_A_MATCH);
        }

        // không có lính
        if (userInfo.getCurrentTroopSpace() == 0) {
            System.out.println("troop empty");
            throw new CustomException(ErrorConst.TROOP_LIST_EMPTY);
        }
        // không đủ tiền tạo trận
        if (userInfo.getGold() - BattleConst.MatchingGoldCost < 0) {
            System.out.println("resource not enough");
            throw new CustomException(ErrorConst.NOT_ENOUGH_MATCHING_COST);
        }

        userInfo.setGold(userInfo.getGold() - BattleConst.MatchingGoldCost);
        userInfo.saveModel(user.getId());

        PlayerInfo enemyInfo = findPlayer(user);

        ArrayList<BattleGameObject> buildings = convertToBattleBuilding(enemyInfo.getListBuildings());
        boolean hasElixirSto = buildings.stream().anyMatch(building -> "STO_2".equals(building.type) || "RES_2".equals(building.type));

        Map<String, Integer> army = new HashMap<>(userInfo.getListTroops());

        // Tạo 1 match với các data trên
        BattleMatch newMatch = new BattleMatch(userInfo.getBattleMatches().size(), enemyInfo.getId(), enemyInfo.getName(),
                buildings,
                army,
                (int) (enemyInfo.getGold() * BattleConst.RESOURCE_RATE),
                hasElixirSto ? (int) (enemyInfo.getElixir() * BattleConst.RESOURCE_RATE) : 0,
                enemyInfo.getRank(),
                userInfo.getRank());

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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(function);
        try {
            return future.get(time, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Luồng chính bị hủy.");
        } catch (ExecutionException e) {
            System.out.println(e.getMessage());
            System.err.println("Lỗi xảy ra trong hàm.");
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Công việc bị hủy sau " + time + " s.");
        } finally {
            executor.shutdown();
        }
        return null;
    }

    public static ArrayList<BattleGameObject> convertToBattleBuilding(ArrayList<Building> buildings) {
        ArrayList<BattleGameObject> battleBuildings = new ArrayList<>();
        for (Building building : buildings) {
            battleBuildings.add(BattleGameObject.convertFromCityBuilding(building));
        }
        return battleBuildings;
    }

    public static PlayerInfo findPlayer(User user) throws CustomException {
        PlayerInfo enemyInfo = null;

        for (int i = 0; i < BattleConst.TIME_GET_MATCH.length; i++) {
            int finalI = i;
            enemyInfo = executeInMaxTime(() -> getPlayerSameRank(user,
                    BattleConst.TIME_GET_MATCH[finalI][1]), BattleConst.TIME_GET_MATCH[i][0]);
            if (enemyInfo != null) {
                break;
            }
        }

        if (enemyInfo == null) {
            // không tìm thấy trận
            System.out.println("cant found enemy");
            throw new CustomException(ErrorConst.CANT_GET_MATCH);
        }

        return enemyInfo;
    }

    public static PlayerInfo getPlayerSameRank(User user, int range) {
        try {

            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
            int userRank = userInfo.getRank();
            ListPlayerData listUserData = (ListPlayerData) ListPlayerData.getModel(ServerConstant.LIST_USER_DATA_ID, ListPlayerData.class);
            PlayerInfo playerInfo = listUserData.getRandomPlayerInRangeRank(user.getId()
                    , userRank - range, userRank + range);
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
        return match.state == BattleConst.MATCH_HAPPENING;
    }

    public static ResponseEndGame handleEndGame(User user, RequestEndGame requestEndGame) {

        try {
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
            userInfo.addResources(requestEndGame.getGoldGot(), requestEndGame.getElixirGot(), 0);
            userInfo.setRank(Math.max(userInfo.getRank() + (requestEndGame.getResult() ? 1 : -1) * requestEndGame.getTrophy(), 0));

            BattleMatch match = (BattleMatch) user.getProperty(ServerConstant.MATCH);
            if (match != null) {
                match.isWin = requestEndGame.getResult();
                match.trophy = requestEndGame.getTrophy();
                match.stars = requestEndGame.getStars();
                match.state = BattleConst.MATCH_ENDED;
                match.usedArmy = requestEndGame.getArmy();
                match.winPercentage = requestEndGame.percentage;
                match.setGoldGot(requestEndGame.getGoldGot());
                match.setElixirGot(requestEndGame.getElixirGot());
                match.pushAction(new BattleAction(BattleConst.ACTION_END, requestEndGame.getTick()));
                userInfo.removeTroop(match.usedArmy);

                ListPlayerData listUserData = (ListPlayerData) ListPlayerData.getModel(ServerConstant.LIST_USER_DATA_ID, ListPlayerData.class);
                listUserData.updateUser(match.enemyId, false);

                userInfo.pushNewMatch(match);
                userInfo.saveModel(user.getId());
                match.saveModel(match.id);

                // update enemy rank and resource
                PlayerInfo enemyInfo = (PlayerInfo) PlayerInfo.getModel(match.enemyId, PlayerInfo.class);
                int oldEnemyRank = enemyInfo.getRank();
                int newEnemyRank = enemyInfo.getRank() + (match.isWin ? -match.trophy : match.trophy);
                enemyInfo.setRank(newEnemyRank);
                enemyInfo.useResources(match.getGoldGot(), match.getElixirGot(), 0);

                listUserData.updateSegmentRank(match.enemyId, oldEnemyRank, newEnemyRank);
                listUserData.saveModel(ServerConstant.LIST_USER_DATA_ID);

                enemyInfo.saveModel(enemyInfo.getId());
            } else {
                throw new CustomException(ErrorConst.NO_MATCH_FOUND);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResponseEndGame(ErrorConst.SUCCESS);
    }

    public static void handleGameEndSync(User user) {
        try {
            PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
            BattleMatch match = (BattleMatch) user.getProperty(ServerConstant.MATCH);

            if (match != null) {
                BattleAction lastAction = match.getActionsList().get(match.getActionsList().size() - 1);
                if (lastAction.type != BattleConst.ACTION_END) {
                    match.getActionsList().add(new BattleAction(BattleConst.ACTION_END, lastAction.tick + 1));
                }
                userInfo.addResources(match.getGoldGot(), match.getElixirGot(), 0);
                match.state = BattleConst.MATCH_ENDED;
                userInfo.removeTroop(match.usedArmy);

                ListPlayerData listUserData = (ListPlayerData) ListPlayerData.getModel(ServerConstant.LIST_USER_DATA_ID, ListPlayerData.class);
                listUserData.updateUser(match.enemyId, false);

                // update enemy rank and resource
                PlayerInfo enemyInfo = (PlayerInfo) PlayerInfo.getModel(match.enemyId, PlayerInfo.class);
                int oldEnemyRank = enemyInfo.getRank();
                int newEnemyRank = enemyInfo.getRank() + match.trophy;
                enemyInfo.setRank(newEnemyRank);
                enemyInfo.useResources(match.getGoldGot(), match.getElixirGot(), 0);

                listUserData.updateSegmentRank(userInfo.getId(), userInfo.getRank(), userInfo.getRank() + match.trophy);
                userInfo.setRank(Math.max(userInfo.getRank() + match.trophy, 0));
                listUserData.updateSegmentRank(match.enemyId, oldEnemyRank, newEnemyRank);
                listUserData.saveModel(ServerConstant.LIST_USER_DATA_ID);

                userInfo.pushNewMatch(match);
                enemyInfo.saveModel(enemyInfo.getId());
                userInfo.saveModel(user.getId());
            } else {
                throw new CustomException(ErrorConst.NO_MATCH_FOUND);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResponseGetMatch handleGetMatch(User user, RequestGetMatch requestGetMatch) throws Exception {
        PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
        ArrayList<BattleMatch> matches = userInfo.getBattleMatches();

        for (BattleMatch match : matches) {
            if (match.id == requestGetMatch.getMatchId()) {
                return new ResponseGetMatch(ErrorConst.SUCCESS, match);
            }
        }
        throw new CustomException(ErrorConst.NO_MATCH_FOUND);
    }

    public static ResponseGetHistoryAttack handleGetHistoryAttack(User user) {
        PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
        ArrayList<BattleMatch> matches = userInfo.getBattleMatches();

        ArrayList<BattleMatch> copiedMatches = new ArrayList<>(matches);
        Collections.reverse(copiedMatches);
        return new ResponseGetHistoryAttack(ErrorConst.SUCCESS, copiedMatches);
    }

    public static void handleDisconnect(User user) {

    }

}
