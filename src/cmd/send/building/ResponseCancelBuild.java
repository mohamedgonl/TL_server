package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseCancelBuild extends BaseMsg {
    private int buildingId;

    public ResponseCancelBuild(short error) {
        super(CmdDefine.CANCEL_BUILD, error);
    }

    public ResponseCancelBuild(short error, int buildingId) {
        super(CmdDefine.CANCEL_BUILD, error);
        this.buildingId = buildingId;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(buildingId);
        return packBuffer(bf);
    }
}
