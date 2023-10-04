package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseRemoveObstacle extends BaseMsg {
    private int buildingId;

    public ResponseRemoveObstacle(short error) {
        super(CmdDefine.REMOVE_OBSTACLE, error);
    }

    public ResponseRemoveObstacle(short error, int buildingId) {
        super(CmdDefine.REMOVE_OBSTACLE, error);
        this.buildingId = buildingId;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(buildingId);
        return packBuffer(bf);
    }
}
