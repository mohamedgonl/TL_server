package battle_models;

import battle_models.BattleBullet;
import battle_models.BattleTroop;

import java.awt.*;

public class BattleCannonBullet extends BattleBullet {

    public BattleCannonBullet(Point startPoint, BattleTroop target, int damagePerShot, double attackRadius) {
        super("DEF_1", startPoint, target, damagePerShot, attackRadius);
    }

    public void onReachDestination() {
        BattleTroop target = getTarget();
        if (target != null && target.isAlive()) {
            target.onGainDamage(getDamagePerShot());
        }
        destroyBullet();
    }
}
