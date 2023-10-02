package cmd.send.user;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseGetTimeServer extends BaseMsg {
    private int time;

    public ResponseGetTimeServer(short error) {
        super(CmdDefine.GET_TIME_SERVER, error);
    }

    public ResponseGetTimeServer(short error, int time) {
        super(CmdDefine.GET_TIME_SERVER, error);
        this.time = time;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(this.time);
        return packBuffer(bf);
    }
}





