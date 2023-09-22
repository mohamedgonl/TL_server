package cmd.receive.user;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

public class RequestMapInfo extends BaseCmd {
    public RequestMapInfo(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }
}
