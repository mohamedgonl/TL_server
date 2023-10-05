package cmd.receive.building;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.common.business.CommonHandle;

import java.nio.ByteBuffer;

public class RequestUpgradeListWall extends BaseCmd {
    private int[] buildingIds;
    private boolean valid = false;

    public RequestUpgradeListWall(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }

    public int[] getBuildingIds() {
        return buildingIds;
    }

    public void setBuildingIds(int[] buildingIds) {
        this.buildingIds = buildingIds;
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
            int length = readShort(bf);
            this.buildingIds = new int[length];
            for (int i = 0; i < length; i++) {
                buildingIds[i] = readInt(bf);
            }
            valid = true;
        } catch (Exception e) {
            valid = false;
            CommonHandle.writeErrLog(e);
        }
    }
}
