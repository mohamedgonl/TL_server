package battle_models;

public class BattleAction {
    public int type;
    public int dt;

    public String troopType;
    public int posX;
    public int posY;


    public BattleAction(int type, int dt) {
        this.type = type;
        this.dt = dt;
    }

    public void setData(String troopType, int posX, int posY) {
        this.troopType = troopType;
        this.posX = posX;
        this.posY = posY;
    }



}




