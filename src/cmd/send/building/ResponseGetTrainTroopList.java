package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.TrainingItem;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ResponseGetTrainTroopList extends BaseMsg {
    private int barrackId;

    private ArrayList<TrainingItem> trainingItems;

    private ArrayList<TrainingItem> doneList;

    private int lastTrainingTime;

    public ResponseGetTrainTroopList(short error) {
        super(CmdDefine.GET_TRAINING_LIST, error);
    }

    public ResponseGetTrainTroopList(short error, int barrackId, ArrayList<TrainingItem> trainingItems, ArrayList<TrainingItem> doneList, int lastTrainingTime) {
        super(CmdDefine.GET_TRAINING_LIST, error);
        this.barrackId = barrackId;
        this.trainingItems = trainingItems;
        this.lastTrainingTime = lastTrainingTime;
        this.doneList = doneList;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(barrackId);
        bf.putInt(this.lastTrainingTime);

        bf.putInt(this.trainingItems.size());
        for (TrainingItem item : this.trainingItems) {
            putStr(bf, item.cfgId);
            bf.putInt(item.count);
        }

        bf.putInt(this.doneList.size());
        for (TrainingItem trainingItem : this.doneList) {
            putStr(bf, trainingItem.cfgId);
            bf.putInt(trainingItem.count);
        }
        return packBuffer(bf);
    }

    @Override
    public String toString() {
        return "ResponseGetTrainTroopList{" +
                "barrackId=" + barrackId +
                ", trainingItems=" + trainingItems +
                ", doneList=" + doneList +
                ", lastTrainingTime=" + lastTrainingTime +
                '}';
    }
}
