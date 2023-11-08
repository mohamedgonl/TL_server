package cmd.receive.user;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

import java.nio.ByteBuffer;

public class RequestCancleTrain extends BaseCmd {

    private int barrackId;
    private String troopCfgId;
    public RequestCancleTrain(DataCmd data) {
        super(data);
        unpackData();
    }

    public int getBarrackId() {
        return barrackId;
    }

    public String getTroopCfgId() {
        return troopCfgId;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            this.barrackId = readInt(bf);
            this.troopCfgId = readString(bf);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "RequestCancleTrain{" +
                "barrackId=" + barrackId +
                ", troopCfgId='" + troopCfgId + '\'' +
                '}';
    }
}
