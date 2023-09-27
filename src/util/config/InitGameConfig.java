package util.config;

import java.util.Map;

public class InitGameConfig {
    public Map<String, MapElement> map;
    public PlayerData player;
    public Map<Integer, ObsElement> obs;

    public class MapElement {
        public int posX;
        public int posY;
    }

    public class PlayerData {
        public int gold;
        public int coin;
        public int elixir;
        public int darkElixir;
        public int builderNumber;
    }

    public class ObsElement {
        public String type;
        public int posX;
        public int posY;
    }
}
