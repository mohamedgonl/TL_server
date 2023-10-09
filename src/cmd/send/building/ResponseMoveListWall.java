package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseMoveListWall extends BaseMsg {

    public ResponseMoveListWall(short error) {
        super(CmdDefine.MOVE_LIST_WALL, error);
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        return packBuffer(bf);
    }
}
