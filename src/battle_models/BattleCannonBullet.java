package battle_models;

import java.awt.*;

public class BattleCannonBullet extends BattleBullet {

    public BattleCannonBullet(Point startPoint, BattleTroop target, int damagePerShot, String type) {
        super(startPoint, target, damagePerShot, "DEF_1", 40);
    }

    public void onReachDestination() {
        BattleTroop target = getTarget();
        if (target != null && target.isAlive()) {
            target.onGainDamage(getDamagePerShot());
        }
        destroyBullet();
    }
}
