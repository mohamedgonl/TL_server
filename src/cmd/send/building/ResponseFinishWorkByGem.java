package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseFinishWorkByGem extends BaseMsg {
    private int buildingId;
    private int gem;

    public ResponseFinishWorkByGem(short error) {
        super(CmdDefine.FINISH_WORK_BY_GEM, error);
    }

    public ResponseFinishWorkByGem(short error, int buildingId, int gem) {
        super(CmdDefine.FINISH_WORK_BY_GEM, error);
        this.buildingId = buildingId;
        this.gem = gem;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(buildingId);
        bf.putInt(gem);
        return packBuffer(bf);
    }
}
