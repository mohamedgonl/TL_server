package cmd.send.user;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseCheatResource extends BaseMsg {
    private int gold;
    private int elixir;
    private int gem;

    public ResponseCheatResource(short error) {super(CmdDefine.CHEAT_RESOURCE, error);}

    public ResponseCheatResource(short error, int gold, int elixir, int gem) {
        super(CmdDefine.CHEAT_RESOURCE, error);
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





