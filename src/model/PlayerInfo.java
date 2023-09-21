package model;

import util.database.DataModel;

import java.awt.*;
import java.util.ArrayList;

public class PlayerInfo extends DataModel {
    private int id;
    private String name;
    private String avatar;
    private int level;
    private int rank;
    private int gold;
    private int elixir;
    private int gem;
    private ArrayList<Building> listBuildings;
    private int[][] map;

    public PlayerInfo() {
    }

    public PlayerInfo(int id, String name) {
        this.id = id;
        this.name = name;
        this.avatar = "";
        this.level = 1;
        this.rank = 0;
        this.gold = 0;
        this.elixir = 0;
        this.gem = 0;
    }

    public PlayerInfo(int id, String name, String avatar, int level, int rank, int gold, int elixir, int gem) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.level = level;
        this.rank = rank;
        this.gold = gold;
        this.elixir = elixir;
        this.gem = gem;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getElixir() {
        return elixir;
    }

    public void setElixir(int elixir) {
        this.elixir = elixir;
    }

    public int getGem() {
        return gem;
    }

    public void setGem(int gem) {
        this.gem = gem;
    }

    public ArrayList<Building> getListBuildings() {
        return listBuildings;
    }

    public void setListBuildings(ArrayList<Building> listBuildings) {
        this.listBuildings = listBuildings;
    }

    public int[][] getMap() {
        return map;
    }

    public void setMap(int[][] map) {
        this.map = map;
    }

    public String toString() {
        return String.format("%s|%s|%s|%s|%s", id, name, gold, gem, elixir, level);
    }
}
