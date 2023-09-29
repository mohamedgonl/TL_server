package cmd.receive.user;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

public class RequestBuyItem extends BaseCmd {
    public RequestBuyItem(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }
}
