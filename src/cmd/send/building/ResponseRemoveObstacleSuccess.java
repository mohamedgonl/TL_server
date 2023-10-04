package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseRemoveObstacleSuccess extends BaseMsg {
    private int buildingId;

    public ResponseRemoveObstacleSuccess(short error) {
        super(CmdDefine.REMOVE_OBSTACLE_SUCCESS, error);
    }

    public ResponseRemoveObstacleSuccess(short error, int buildingId) {
        super(CmdDefine.REMOVE_OBSTACLE_SUCCESS, error);
        this.buildingId = buildingId;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(buildingId);
        return packBuffer(bf);
    }
}
