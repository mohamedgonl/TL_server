package cmd.send.user;


import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.TrainingItem;

import java.nio.ByteBuffer;


public class ResponseTrainingCreate extends BaseMsg {
    private int lastTrainingTime;
    private TrainingItem trainingItem;
    private int barrackId;

    public ResponseTrainingCreate(short error) {
        super(CmdDefine.TRAIN_TROOP_CREATE, error);
    }

    public ResponseTrainingCreate(short error, TrainingItem trainingItem, int barrackId, int lastTrainingTime) {
        super(CmdDefine.TRAIN_TROOP_CREATE, error);
        this.trainingItem = trainingItem;
        this.barrackId = barrackId;
        this.lastTrainingTime = lastTrainingTime;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(this.barrackId);
        putStr(bf, this.trainingItem.cfgId);
        bf.putInt(this.trainingItem.count);
        bf.putInt(this.lastTrainingTime);
        return packBuffer(bf);
    }
}
