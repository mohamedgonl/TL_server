package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.CollectorBuilding;

import java.nio.ByteBuffer;

public class ResponseCollectResource extends BaseMsg {
    private CollectorBuilding collector;
    private int gold;
    private int elixir;

    public ResponseCollectResource(short error) {
        super(CmdDefine.COLLECT_RESOURCE, error);
    }

    public ResponseCollectResource(short error, CollectorBuilding collector, int gold, int elixir) {
        super(CmdDefine.COLLECT_RESOURCE, error);
        this.collector = collector;
        this.gold = gold;
        this.elixir = elixir;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        if (collector != null) {
            bf.putInt(collector.getId());
            bf.putInt(collector.getLastCollectTime());
            bf.putInt(gold);
            bf.putInt(elixir);
        }
        return packBuffer(bf);
    }
}
