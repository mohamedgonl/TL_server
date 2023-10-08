package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseCancleTrain extends BaseMsg {
    private int barrackId;

    private String cfgId;

    private int lastTrainingTime;

    private int additionElixir;


    public ResponseCancleTrain(short error) {
        super(CmdDefine.CANCLE_TRAIN_TROOP, error);
    }

    public ResponseCancleTrain(short error, int barrackId, String cfgId, int lastTrainingTime, int additionElixir) {
        super(CmdDefine.CANCLE_TRAIN_TROOP, error);
        this.barrackId = barrackId;
        this.cfgId = cfgId;
        this.lastTrainingTime = lastTrainingTime;
        this.additionElixir = additionElixir;
    }



    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(barrackId);
        putStr(bf, cfgId);
        bf.putInt(lastTrainingTime);
        bf.putInt(additionElixir);
        return packBuffer(bf);
    }

}
