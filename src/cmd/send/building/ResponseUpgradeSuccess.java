package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseUpgradeSuccess extends BaseMsg {
    private int buildingId;
    private int timeLeft = 0;

    public ResponseUpgradeSuccess(short error) {
        super(CmdDefine.UPGRADE_SUCCESS, error);
    }

    public ResponseUpgradeSuccess(short error, int buildingId) {
        super(CmdDefine.UPGRADE_SUCCESS, error);
        this.buildingId = buildingId;
    }

    public ResponseUpgradeSuccess(short error, int buildingId, int timeLeft) {
        super(CmdDefine.UPGRADE_SUCCESS, error);
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
