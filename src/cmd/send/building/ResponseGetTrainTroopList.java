package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.TrainingItem;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ResponseGetTrainTroopList extends BaseMsg {
    private int barrackId;

    private ArrayList<TrainingItem> trainingItems;

    private int lastTrainingTime;

    public ResponseGetTrainTroopList(short error) {
        super(CmdDefine.GET_TRAINING_LIST, error);
    }

    public ResponseGetTrainTroopList(short error, int barrackId, ArrayList<TrainingItem> trainingItems, int lastTrainingTime){
        super(CmdDefine.GET_TRAINING_LIST, error);
        this.barrackId = barrackId;
        this.trainingItems = trainingItems;
        this.lastTrainingTime = lastTrainingTime;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(barrackId);
        bf.putInt(this.lastTrainingTime);

        bf.putInt(this.trainingItems.size());
        for (int i = 0; i < this.trainingItems.size(); i++) {
            putStr(bf, this.trainingItems.get(i).cfgId);
            bf.putInt(this.trainingItems.get(i).count);
        }
        return packBuffer(bf);
    }

}
