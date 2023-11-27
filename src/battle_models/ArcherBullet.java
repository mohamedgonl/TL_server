package battle_models;

import java.awt.*;

public class ArcherBullet extends TroopBullet{

    public int speedPerSec = 20;

    public ArcherBullet(BattleBuilding target, Point startPoint, int damage, BattleTroop troop) {
        super(target, startPoint, damage,20, troop);
    }
}
