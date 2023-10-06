package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.CollectorBuilding;

import java.nio.ByteBuffer;

public class ResponseTrainingSuccess extends BaseMsg {
    private int barrackId;
    private int isDoneNow;
    private int lastTrainingTime;

    private String cfgId;

    public ResponseTrainingSuccess(short error) {
        super(CmdDefine.TRAIN_TROOP_SUCCESS, error);
    }

    public ResponseTrainingSuccess(short error, int barrackId, int isDoneNow, String cfgId, int lastTrainingTime) {
        super(CmdDefine.TRAIN_TROOP_SUCCESS, error);
        this.barrackId = barrackId;
        this.isDoneNow = isDoneNow;
        this.cfgId = cfgId;
        this.lastTrainingTime = lastTrainingTime;

    }


    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(isDoneNow);
        bf.putInt(barrackId);
        putStr(bf, cfgId);
        bf.putInt(lastTrainingTime);
        
        return packBuffer(bf);
    }
}
