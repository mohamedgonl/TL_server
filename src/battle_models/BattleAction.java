package battle_models;

public class BattleAction {
    public int type;
    public int tick;

    public String troopType;
    public int posX;
    public int posY;


    public BattleAction(int type, int tick) {
        this.type = type;
        this.tick = tick;
    }

    public void setData(String troopType, int posX, int posY) {
        this.troopType = troopType;
        this.posX = posX;
        this.posY = posY;
    }


    @Override
    public String toString() {
        return "BattleAction{" +
                "type=" + type +
                ", tick=" + tick +
                ", troopType='" + troopType + '\'' +
                ", posX=" + posX +
                ", posY=" + posY +
                '}';
    }
}




