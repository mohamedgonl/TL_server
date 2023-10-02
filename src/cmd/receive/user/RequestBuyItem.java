package cmd.receive.user;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

import java.nio.ByteBuffer;

public class RequestBuyItem extends BaseCmd {
    private String itemCfgId;
    private boolean valid = false;


    public RequestBuyItem(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }


    public String getItemCfgId() {
        return itemCfgId;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            this.itemCfgId = readString(bf);
            valid = true;
        }
        catch (Exception e){
            valid = false;
            e.printStackTrace();
        }
    }
}
