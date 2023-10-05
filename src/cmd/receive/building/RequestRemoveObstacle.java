package cmd.receive.building;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.common.business.CommonHandle;

import java.nio.ByteBuffer;

public class RequestRemoveObstacle extends BaseCmd {
    private int buildingId;
    private boolean valid = false;

    public RequestRemoveObstacle(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
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
            buildingId = readInt(bf);
            valid = true;
        } catch (Exception e) {
            valid = false;
            CommonHandle.writeErrLog(e);
        }
    }
}
