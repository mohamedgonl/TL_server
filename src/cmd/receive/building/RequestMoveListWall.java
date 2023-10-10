package cmd.receive.building;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.common.business.CommonHandle;

import java.awt.*;
import java.nio.ByteBuffer;

public class RequestMoveListWall extends BaseCmd {
    private Point firstPos;
    private Point nextFirstPos;
    private short dx;
    private short dy;
    private short amount;
    private short newDx;
    private short newDy;
    private boolean valid = false;

    public RequestMoveListWall(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Point getFirstPos() {
        return firstPos;
    }

    public void setFirstPos(Point firstPos) {
        this.firstPos = firstPos;
    }

    public Point getNextFirstPos() {
        return nextFirstPos;
    }

    public void setNextFirstPos(Point nextFirstPos) {
        this.nextFirstPos = nextFirstPos;
    }

    public short getDx() {
        return dx;
    }

    public void setDx(short dx) {
        this.dx = dx;
    }

    public short getDy() {
        return dy;
    }

    public void setDy(short dy) {
        this.dy = dy;
    }

    public short getAmount() {
        return amount;
    }

    public void setAmount(short amount) {
        this.amount = amount;
    }

    public short getNewDx() {
        return newDx;
    }

    public void setNewDx(short newDx) {
        this.newDx = newDx;
    }

    public short getNewDy() {
        return newDy;
    }

    public void setNewDy(short newDy) {
        this.newDy = newDy;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            firstPos = new Point(readShort(bf), readShort(bf));
            dx = readShort(bf);
            dy = readShort(bf);
            amount = readShort(bf);
            nextFirstPos = new Point(readShort(bf), readShort(bf));
            newDx = readShort(bf);
            newDy = readShort(bf);

            valid = true;
        } catch (Exception e) {
            valid = false;
            CommonHandle.writeErrLog(e);
        }
    }
}
