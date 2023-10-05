package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.Building;

import java.nio.ByteBuffer;

public class ResponseRemoveObstacle extends BaseMsg {
    private Building building;

    public ResponseRemoveObstacle(short error) {
        super(CmdDefine.REMOVE_OBSTACLE, error);
    }

    public ResponseRemoveObstacle(short error, Building building) {
        super(CmdDefine.REMOVE_OBSTACLE, error);
        this.building = building;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        if (building != null) {
            bf.putInt(building.getId());
            bf.putShort(building.getStatus().getValue());
            bf.putInt(building.getStartTime());
            bf.putInt(building.getEndTime());
        }
        return packBuffer(bf);
    }
}
