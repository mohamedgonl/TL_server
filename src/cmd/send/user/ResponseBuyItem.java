package cmd.send.user;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.PlayerInfo;

import java.nio.ByteBuffer;

public class ResponseBuyItem extends BaseMsg {
    private int gold;
    private  int elixir;
    private int gem;

    public ResponseBuyItem(short error) {super(CmdDefine.BUY_RESOURCE, error);}

    public ResponseBuyItem(short error, int gold,int elixir,int gem) {
        super(CmdDefine.BUY_RESOURCE, error);
        this.gold = gold;
        this.elixir = elixir;
        this.gem = gem;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(this.gold);
        bf.putInt(this.elixir);
        bf.putInt(this.gem);
        return packBuffer(bf);
    }
}





