package cmd.receive.user;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

import java.nio.ByteBuffer;

public class RequestGetTrainTroopList extends BaseCmd {
    private int barrackId;

    public RequestGetTrainTroopList(DataCmd dataCmd){
        super(dataCmd);
        this.unpackData();
    }

    public int getBarrackId() {
        return barrackId;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            this.barrackId = readInt(bf);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
