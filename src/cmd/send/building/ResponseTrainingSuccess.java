package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.CollectorBuilding;

import java.nio.ByteBuffer;

public class ResponseTrainingSuccess extends BaseMsg {
    private int barrackId;
    private int isDoneNow;

    private String cfgId;

    public ResponseTrainingSuccess(short error) {
        super(CmdDefine.TRAIN_TROOP_SUCCESS, error);
    }

    public ResponseTrainingSuccess(short error, int barrackId, int isDoneNow, String cfgId) {
        super(CmdDefine.COLLECT_RESOURCE, error);
        this.barrackId = barrackId;
        this.isDoneNow = isDoneNow;
        this.cfgId = cfgId;
    }


    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(isDoneNow);
        bf.putInt(barrackId);
        putStr(bf, cfgId);
        
        return packBuffer(bf);
    }
}
