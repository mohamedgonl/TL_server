package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseRemoveObstacleSuccess extends BaseMsg {
    private int buildingId;
    private int timeLeft = 0;

    public ResponseRemoveObstacleSuccess(short error) {
        super(CmdDefine.REMOVE_OBSTACLE_SUCCESS, error);
    }

    public ResponseRemoveObstacleSuccess(short error, int buildingId) {
        super(CmdDefine.REMOVE_OBSTACLE_SUCCESS, error);
        this.buildingId = buildingId;
    }

    public ResponseRemoveObstacleSuccess(short error, int buildingId, int timeLeft) {
        super(CmdDefine.REMOVE_OBSTACLE_SUCCESS, error);
        this.buildingId = buildingId;
        this.timeLeft = timeLeft;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(buildingId);
        bf.putInt(timeLeft);
        return packBuffer(bf);
    }
}
