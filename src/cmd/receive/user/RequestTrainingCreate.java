package cmd.receive.user;


import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

import java.nio.ByteBuffer;

public class RequestTrainingCreate extends BaseCmd {
    private String troopCfgId;
    private int troopCount;
    private int barrackId;
    private boolean valid = false;


    public RequestTrainingCreate(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }


    public String getTroopCfgId() {
        return troopCfgId;
    }

    public int getBarrackIdId() {
        return barrackId;
    }

    public int getTroopCount() {
        return troopCount;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            this.troopCfgId = readString(bf);
            this.troopCount = readInt(bf);
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
        return "RequestTrainingCreate{" +
                "troopCfgId='" + troopCfgId + '\'' +
                ", troopCount=" + troopCount +
                ", barrackId=" + barrackId +
                ", valid=" + valid +
                '}';
    }
}
