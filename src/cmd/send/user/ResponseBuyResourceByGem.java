package cmd.send.user;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseBuyResourceByGem extends BaseMsg {
    private int gem;

    public ResponseBuyResourceByGem(short error) {super(CmdDefine.BUY_RESOURCE_BY_GEM, error);}

    public ResponseBuyResourceByGem(short error, int gem) {
        super(CmdDefine.BUY_RESOURCE_BY_GEM, error);
        this.gem = gem;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(this.gem);
        return packBuffer(bf);
    }
}





