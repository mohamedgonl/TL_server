package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseCancelUpgrade extends BaseMsg {
    private int buildingId;

    public ResponseCancelUpgrade(short error) {
        super(CmdDefine.CANCEL_UPGRADE, error);
    }

    public ResponseCancelUpgrade(short error, int buildingId) {
        super(CmdDefine.CANCEL_UPGRADE, error);
        this.buildingId = buildingId;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(buildingId);
        return packBuffer(bf);
    }
}
