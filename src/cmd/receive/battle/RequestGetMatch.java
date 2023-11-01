package cmd.receive.battle;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

import java.nio.ByteBuffer;

public class RequestGetMatch extends BaseCmd {
    private int matchId;


    public RequestGetMatch(DataCmd data) {
        super(data);
        unpackData();
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            this.matchId = readInt(bf);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getMatchId() {
        return matchId;
    }

    @Override
    public String toString() {
        return "RequestGetMatch{" +
                "matchId=" + matchId +
                '}';
    }
}
