package cmd.send.user;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseBuyResourceByGem extends BaseMsg {
    private int gem;

    private int gold;

    private int elixir;

    public ResponseBuyResourceByGem(short error) {super(CmdDefine.BUY_RESOURCE_BY_GEM, error);}

    public ResponseBuyResourceByGem(short error, int gem, int gold, int elixir) {
        super(CmdDefine.BUY_RESOURCE_BY_GEM, error);
        this.gem = gem;
        this.gold = gold;
        this.elixir = elixir;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(this.gem);
        bf.putInt(this.gold);
        bf.putInt(this.elixir);
        return packBuffer(bf);
    }
}





