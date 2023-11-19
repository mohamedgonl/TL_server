package cmd.receive.user;


import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

import java.nio.ByteBuffer;


public class RequestTrainingSuccess extends BaseCmd {
    private int isDoneNow;
    private int barrackId;

    private boolean valid = false;


    public RequestTrainingSuccess(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }

    public int getBarrackId() {
        return barrackId;
    }

    public boolean checkIsDoneNow() {
        return isDoneNow != 0;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            this.isDoneNow = readInt(bf);
            this.barrackId = readInt(bf);
            valid = true;
        }
        catch (Exception e){
            valid = false;
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "RequestTrainingSuccess{" +
                "isDoneNow=" + isDoneNow +
                ", barrackId=" + barrackId +
                ", valid=" + valid +
                '}';
    }
}
