package cmd.receive.battle;

import battle_models.BattleMatch;
import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class RequestGetHistoryAttack extends BaseCmd {
    public RequestGetHistoryAttack(DataCmd data) {
        super(data);
    }

}
