package model;

import util.BattleConst;
import util.Common;
import util.database.DataModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

public class ListPlayerData extends DataModel {
    public  Map<Integer,Map<Integer, Boolean> > userIds = new HashMap<>();

    public void updateUser(int newUID, boolean isGettingAttacked) {
        PlayerInfo user = null;
        try {
            user = (PlayerInfo) PlayerInfo.getModel(newUID, PlayerInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int rankPoint = user.getRank();

        int rankLevel = rankPoint / BattleConst.RANK_DIST;
        if(userIds.containsKey(rankLevel)) {
            Map<Integer, Boolean> uIds = userIds.get(rankLevel);
            uIds.putIfAbsent(newUID, isGettingAttacked);
        }
        else {
            Map<Integer, Boolean> uId = new HashMap<>();
            uId.put(newUID, isGettingAttacked);
            this.userIds.put(rankLevel, uId);
        }
    }

    public void updateSegmentRank (int userId, int oldRank, int newRank) {
        int oldRankSegment = oldRank / BattleConst.RANK_DIST;
        int newRankSegment = newRank / BattleConst.RANK_DIST;
        if(oldRankSegment != newRankSegment) {
            this.userIds.get(oldRankSegment).remove(userId);
            Map<Integer, Boolean> uId = new HashMap<>();
            uId.put(userId, false);
            this.userIds.putIfAbsent(newRankSegment, uId);
        }
    }

//    public void updateUserState(int userId, boolean canMatch) {
//        this.userIds.put(userId, canMatch);
//    }


    public PlayerInfo getRandomPlayerInRangeRank(int userId, int min, int max) {
        int userRank = (max + min) /2;
        PlayerInfo playerInfo;
        ArrayList<Map.Entry<Integer, Boolean>> entryList = new ArrayList<>(userIds.get(userRank/BattleConst.RANK_DIST).entrySet());
        int id;
        boolean isOnline = true;
        int count = 0;
        try {
            do {
                count ++;
                Random random = new Random();
                Map.Entry<Integer, Boolean> randomEntry = entryList.get(random.nextInt(entryList.size()));
                id = randomEntry.getKey();
                playerInfo =(PlayerInfo) PlayerInfo.getModel(id, PlayerInfo.class);
                isOnline =  Common.checkUserOnline(playerInfo.getId());
            }
            while (playerInfo.getRank() < min || playerInfo.getRank() > max ||isOnline || id == userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return playerInfo;
    }


}
