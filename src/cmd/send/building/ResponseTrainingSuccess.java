package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseTrainingSuccess extends BaseMsg {
    private int barrackId;
    private int isDoneNow;

    private String cfgId;

    private int lastTrainingTime;

    private int gem;

    public ResponseTrainingSuccess(short error, int barrackId) {
        super(CmdDefine.TRAIN_TROOP_SUCCESS, error);
        this.cfgId = "";
        this.barrackId = barrackId;
    }

    public ResponseTrainingSuccess(short error, int barrackId, int isDoneNow, String cfgId, int lastTrainingTime, int gem) {
        super(CmdDefine.TRAIN_TROOP_SUCCESS, error);
        this.barrackId = barrackId;
        this.isDoneNow = isDoneNow;
        this.cfgId = cfgId;
        this.lastTrainingTime = lastTrainingTime;
        this.gem = gem;
    }



    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(isDoneNow);
        bf.putInt(barrackId);
        putStr(bf, cfgId);
        bf.putInt(lastTrainingTime);
        bf.putInt(gem);
        return packBuffer(bf);
    }

}
