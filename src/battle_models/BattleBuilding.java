package battle_models;

import util.BuildingFactory;
import util.GameConfig;
import util.config.BaseBuildingConfig;
import util.log.LogUtils;
import util.server.RandomUtils;

import java.awt.*;
import java.util.ArrayList;

public class BattleBuilding extends BattleGameObject {
    public transient int hp; // hp hien tai
    public int maxHp;


    public BattleBuilding(int id, String type, int level, int posX, int posY) {
        super(id, type, level, posX, posY);
        BaseBuildingConfig baseBuildingStats = GameConfig.getInstance().getBuildingConfig(type, level);

        this.hp = baseBuildingStats.hitpoints;
        this.maxHp = baseBuildingStats.hitpoints;
    }

    public boolean isDestroy() {
        return this.hp <= 0;
    }

    public void onGainDamage(int damage) {
        if (damage <= 0 || this.hp <= 0) {
            return;
        }
        this.hp = Math.max(this.hp - damage, 0);
        if (this.hp <= 0) {
            this.onDestroy();
        }

        LogUtils.writeLog("building " + this.id + " gain " + damage + " ~ " + this.hp);
    }

    public void onDestroy() {
        this.match.onDestroyBuilding(this.id);

        //if WAL
        if (this.type.startsWith(BuildingFactory.GameObjectPrefix.WALL)) {
            //get list troop from battle manager

            ArrayList<BattleTroop> listTroop = this.match.getTroops();

            //for in list troop, if troop attack type wall, remove from list troop attack
            for (BattleTroop battleTroop : listTroop) {
                if (!battleTroop.isAlive()) continue;
                if (battleTroop.target != null && battleTroop.target.type.startsWith(BuildingFactory.GameObjectPrefix.WALL)) {
                    battleTroop.refindTarget();
                }
            }
        }

        LogUtils.writeLog("building " + this.id + " destroyed");
    }

    public ArrayList<Point> getCorners() {
        ArrayList<Point> corners = new ArrayList<>();
        corners.add(new Point(posX, posY));
        corners.add(new Point(posX + width, posY));
        corners.add(new Point(posX, posY + height));
        corners.add(new Point(posX + width, posY + height));
        return corners;
    }

    public Point getNearestPoint(int x, int y, Integer troopId, boolean random) {
        int xStart = this.posX;
        int yStart = this.posY;
        int xEnd = xStart + this.width;
        int yEnd = yStart + this.height;
        int xMid = xStart + this.width / 2;
        int yMid = yStart + this.height / 2;
        if (!random) {
            if (x <= xStart) {
                if (y <= yStart)
                    return new Point(xStart, yStart);
                else if (y >= yEnd)
                    return new Point(xStart, yEnd);
                else
                    return new Point(xStart,y);
            }
            else if (x >= xEnd) {
                if (y <= yStart)
                    return new Point(xEnd, yStart);
                 else if (y >= yEnd)
                    return new Point(xEnd, yEnd);
                 else
                    return new Point(xEnd, y);
            }
            else {
                if (y <= yStart)
                    return new Point(x, yStart);
                 else if (y >= yEnd)
                    return new Point(x, yEnd);
                else
                    return new Point(x, y);
            }
        }

        String seed = String.valueOf(troopId);
        int distanceOffset = (int) Math.ceil(this.width / 4.0);
        int choose = (int) RandomUtils.generateRandomBySeed(0, 1, seed, true);
        //if nearest point is one of 4 corners, else return random near point on edge
        if (x <= xStart) {
            if (y <= yStart) {
                if (choose == 0) {
                    return new Point(
                            xStart,
                            (int) RandomUtils.generateRandomBySeed(yStart, yMid,seed, true)
                    );
                } else {
                    return new Point(
                            (int) RandomUtils.generateRandomBySeed(xStart, xMid,seed, true),
                            yStart
                    );
                }
            } else if (y >= yEnd) {
                if (choose == 0) {
                    return new Point(
                            xStart,
                            (int) RandomUtils.generateRandomBySeed(yMid, yEnd,seed, true)
                    );
                } else {
                    return new Point(
                            (int) RandomUtils.generateRandomBySeed(xStart, xMid,seed, true),
                            yEnd
                    );
                }
            } else {
                return new Point(
                        xStart,
                        (int) RandomUtils.generateRandomBySeed(yMid -distanceOffset, yMid + distanceOffset,seed, true)
                );
            }
        } else if (x >= xEnd) {
            if (y <= yStart) {
                if (choose == 0) {
                    return new Point(
                            xEnd,
                            (int) RandomUtils.generateRandomBySeed(yStart, yMid,seed, true)
                    );
                } else {
                    return new Point(
                            (int) RandomUtils.generateRandomBySeed(xMid, xEnd,seed, true),
                            yStart
                    );
                }
            } else if (y >= yEnd) {
                if (choose == 0) {
                    return new Point(
                            xEnd,
                            (int) RandomUtils.generateRandomBySeed(yMid, yEnd,seed, true)
                    );
                } else {
                    return new Point(
                            (int) RandomUtils.generateRandomBySeed(xMid, xEnd,seed, true),
                            yEnd
                    );
                }
            } else {
                return new Point(
                        xEnd,
                        (int) RandomUtils.generateRandomBySeed(yMid - distanceOffset, yMid + distanceOffset,seed, true)
                );
            }
        } else {

            if (y <= yStart) {
                return new Point(
                        (int) RandomUtils.generateRandomBySeed(xMid - distanceOffset, xMid + distanceOffset, seed, true),
                        yStart);
            } else if (y >= yEnd) {
                return new Point(
                        (int) RandomUtils.generateRandomBySeed(xMid - distanceOffset, xMid + distanceOffset, seed, true),
                        yEnd);
            } else {
                return new Point(x, y);
            }
        }
    }

    public Point getGridPosition() {
        return new Point(posX, posY);
    }

    @Override
    public String toString() {
        return "BattleBuilding{" +
                "id=" + id +
                ", posX=" + posX +
                ", posY=" + posY +
                ", hp=" + hp +
                ", type='" + type + '\'' +
                ", level=" + level +
                ", width=" + width +
                ", height=" + height +
                ", maxHp=" + maxHp +
                '}';
    }
}
