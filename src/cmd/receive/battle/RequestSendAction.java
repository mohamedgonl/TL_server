package cmd.receive.battle;

import battle_models.BattleAction;
import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.common.business.CommonHandle;

import java.nio.ByteBuffer;

public class RequestSendAction extends BaseCmd {

    private BattleAction action;

    public RequestSendAction(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }



    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            this.action.type = readString(bf);
            this.action.dt = readInt(bf);

            // đọc data của action

        } catch (Exception e) {
            CommonHandle.writeErrLog(e);
        }
    }
}
