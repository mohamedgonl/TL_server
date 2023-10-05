package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.CollectorBuilding;

import java.nio.ByteBuffer;

public class ResponseTrainingSuccess extends BaseMsg {
    private int barrackId;
    private int isDoneNow;

    public ResponseTrainingSuccess(short error) {
        super(CmdDefine.TRAIN_TROOP_SUCCESS, error);
    }

    public ResponseTrainingSuccess(short error, int barrackId) {
        super(CmdDefine.COLLECT_RESOURCE, error);
        this.barrackId = barrackId;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(isDoneNow);
        bf.putInt(barrackId);
        return packBuffer(bf);
    }
}
