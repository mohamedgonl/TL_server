package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseMoveBuilding extends BaseMsg {

    public ResponseMoveBuilding(short error) {
        super(CmdDefine.MOVE_BUILDING, error);
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        return packBuffer(bf);
    }
}
