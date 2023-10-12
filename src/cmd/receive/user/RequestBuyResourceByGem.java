package cmd.receive.user;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

import java.nio.ByteBuffer;

public class RequestBuyResourceByGem extends BaseCmd {
    private int gold;
    private int elixir;
    private boolean valid = false;

    public RequestBuyResourceByGem(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getElixir() {
        return elixir;
    }

    public void setElixir(int elixir) {
        this.elixir = elixir;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            this.gold = readInt(bf);
            this.elixir = readInt(bf);
            valid = true;
        } catch (Exception e) {
            valid = false;
            e.printStackTrace();
        }
    }
}
