package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseBuildSuccess extends BaseMsg {
    private int buildingId;
    private int timeLeft = 0;

    public ResponseBuildSuccess(short error) {
        super(CmdDefine.BUILD_SUCCESS, error);
    }

    public ResponseBuildSuccess(short error, int buildingId) {
        super(CmdDefine.BUILD_SUCCESS, error);
        this.buildingId = buildingId;
    }
    public ResponseBuildSuccess(short error, int buildingId, int timeLeft) {
        super(CmdDefine.BUILD_SUCCESS, error);
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
