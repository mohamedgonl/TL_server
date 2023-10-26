package model;

import util.Common;
import util.database.DataModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ListPlayerData extends DataModel {
    public Map<Integer, Boolean> userIds = new HashMap<>();

    public void updateUser(int newUID, boolean isGettingAttacked) {
        this.userIds.putIfAbsent(newUID, isGettingAttacked);
        System.out.println(this.userIds);
    }

    public ArrayList<PlayerInfo> getAllPlayersOffline() {

        ArrayList<PlayerInfo> listPlayers = new ArrayList<>();

        if(userIds != null) {
            for (Map.Entry<Integer, Boolean> userId : userIds.entrySet()) {
                PlayerInfo playerInfo;
                try {
                    // if user is getting attackd hoặc đang online thì không match
                    if (userId.getValue()
//                            || Common.isUserOnline(userId.getKey())
                    ) continue;

                    playerInfo = (PlayerInfo) PlayerInfo.getModel(userId.getKey(), PlayerInfo.class);
                    if (playerInfo != null) {
                        listPlayers.add(playerInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return listPlayers;
    }

    public void updateUserState (int userId, boolean canMatch) {
        this.userIds.put(userId, canMatch);
    }

//    public void setPlayerState

    public PlayerInfo getRandomPlayerInRangeRank(int min, int max) {
        PlayerInfo playerInfo;
        do {
            ArrayList<Map.Entry<Integer, Boolean>> entryList = new ArrayList<>(userIds.entrySet());
            Random random = new Random();
            Map.Entry<Integer, Boolean> randomEntry = entryList.get(random.nextInt(entryList.size()));
            try {
                playerInfo = (PlayerInfo) PlayerInfo.getModel(randomEntry.getKey(), PlayerInfo.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        while (playerInfo.getRank() < min || playerInfo.getRank() > max);

        return playerInfo;
    }





}
