package cmd.receive.user;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

public class RequestArmyInfo extends BaseCmd {
    public RequestArmyInfo(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }
}
