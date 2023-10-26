package cmd.receive.battle;

import battle_models.BattleAction;
import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.common.business.CommonHandle;
import util.BattleConst;

import java.nio.ByteBuffer;

public class RequestSendAction extends BaseCmd {

    private BattleAction action;

    public RequestSendAction(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }

    public BattleAction getAction() {
        return action;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {

            this.action.type = readInt(bf);
            this.action.dt = readInt(bf);

            // đọc data của action
            if(this.action.type == BattleConst.ACTION_THROW_TROOP) {
                 String troopType = readString(bf);
                 int posX = readInt(bf);
                 int posY = readInt(bf);
                this.action.setData(troopType, posX, posY);
            }

        } catch (Exception e) {
            CommonHandle.writeErrLog(e);
        }
    }
}
