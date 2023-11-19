package battle_models;

import util.BattleConst;

public class BattleStorage extends BattleBuilding {
    private int capacity;
    private int resourceLeft;
    private BattleConst.ResourceType resourceType;

    public BattleStorage(int id, String type, int level, int posX, int posY, BattleConst.ResourceType resourceType) {
        super(id, type, level, posX, posY);
        this.resourceType = resourceType;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        this.resourceLeft = capacity;
    }

    public void setResourceLeft(int resourceLeft) {
        this.resourceLeft = resourceLeft;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getResourceLeft() {
        return resourceLeft;
    }

    public BattleConst.ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(BattleConst.ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public void onGainDamage(int damage) {
        super.onGainDamage(damage);

        int resource = (int) Math.ceil((double) (damage * this.capacity) / this.maxHp);
        if (resource <= this.resourceLeft) {
            this.reduceResource(resource);
        }
    }

    public void reduceResource(int resource) {
        this.resourceLeft -= resource;
        this.match.updateResourceGot(resource, this.resourceType);
    }

    public void onDestroy() {
        if (this.resourceLeft > 0) {
            this.reduceResource(this.resourceLeft);
        }
        super.onDestroy();
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
                ", resourceLeft=" + resourceLeft +
                ", capacity=" + capacity +
                '}';
    }
}
