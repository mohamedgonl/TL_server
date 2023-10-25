package model;

import util.Common;
import util.database.DataModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ListPlayerData extends DataModel {
    public Map<Integer, Boolean> userIds = new HashMap<>();

    public void addNewUserId (int newUID) {
        this.userIds.putIfAbsent(newUID, true);
        System.out.println(this.userIds);
    }

    public ArrayList<PlayerInfo> getAllPlayersOffline() {

        ArrayList<PlayerInfo> listPlayers = new ArrayList<>();

        if(userIds != null) {
            for (Map.Entry<Integer, Boolean> userId : userIds.entrySet()) {
                PlayerInfo playerInfo;
                try {
                    if (!userId.getValue() || Common.isUserOnline(userId.getKey())) continue;

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

    public ArrayList<PlayerInfo> getPlayersInRangeRank(int min, int max) {

        ArrayList<PlayerInfo> listPlayers = new ArrayList<>();
        for (PlayerInfo player : this.getAllPlayersOffline()) {
            int rank = player.getRank();
            if (rank >= min && rank <= max) {
                listPlayers.add(player);
            }
        }

        return listPlayers;
    }





}
