package battle_models;

import java.awt.*;

public class ArcherBullet extends TroopBullet{

    public int speedPerSec = 20;

    public ArcherBullet(BattleBuilding target, Point startPoint, int damage) {
        super(target, startPoint, damage,20);
    }
}
