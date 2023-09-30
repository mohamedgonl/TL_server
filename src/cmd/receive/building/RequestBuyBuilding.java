package cmd.receive.building;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.common.business.CommonHandle;

import java.awt.*;
import java.nio.ByteBuffer;

public class RequestBuyBuilding extends BaseCmd {
    private String type;
    private Point position;
    private boolean valid = false;

    public RequestBuyBuilding(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            type = readString(bf);
            short posX = readShort(bf);
            short posY = readShort(bf);
            position = new Point(posX, posY);
            valid = true;
        } catch (Exception e) {
            valid = false;
            CommonHandle.writeErrLog(e);
        }
    }
}
