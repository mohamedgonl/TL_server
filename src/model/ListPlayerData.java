package model;

import util.database.DataModel;

import java.util.ArrayList;

public class ListPlayerData extends DataModel {
    public ArrayList<Integer> userIds = new ArrayList<Integer>();


    public void addNewUserId (int newUID) {
        this.userIds.add(newUID);
    }

    public ArrayList<PlayerInfo> getAllPlayers () {
        ArrayList<PlayerInfo> listPlayers = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            PlayerInfo playerInfo;
            try {
                playerInfo = (PlayerInfo) PlayerInfo.getModel(userIds.get(i), PlayerInfo.class);
                if(playerInfo != null) {
                    listPlayers.add(playerInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return listPlayers;
    }

    public ArrayList<PlayerInfo> getPlayersInRangeRank(int min, int max) {
        ArrayList<PlayerInfo> listPlayers = new ArrayList<>();
        for (PlayerInfo player : this.getAllPlayers()) {
            int rank = player.getRank();
            if (rank >= min && rank <= max) {
                listPlayers.add(player);
            }
        }

        return listPlayers;
    }








}
