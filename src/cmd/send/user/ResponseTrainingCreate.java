package cmd.send.user;


import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.TrainingItem;

import java.nio.ByteBuffer;


public class ResponseTrainingCreate extends BaseMsg {
    private TrainingItem trainingItem;

    public ResponseTrainingCreate(short error) {
        super(CmdDefine.TRAIN_TROOP_CREATE, error);
    }

    public ResponseTrainingCreate(short error, TrainingItem trainingItem) {
        super(CmdDefine.TRAIN_TROOP_CREATE, error);
        this.trainingItem = trainingItem;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();

        return packBuffer(bf);
    }
}
