package cmd.receive.user;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;

import java.nio.ByteBuffer;

public class RequestCheatResource extends BaseCmd {
    private int gold;
    private int elixir;
    private int gem;
    private boolean valid = false;

    public RequestCheatResource(DataCmd dataCmd) {
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

    public int getGem() {
        return gem;
    }

    public void setGem(int gem) {
        this.gem = gem;
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
            this.gold = bf.getInt();
            this.elixir = bf.getInt();
            this.gem = bf.getInt();
            valid = true;
        }
        catch (Exception e){
            valid = false;
            e.printStackTrace();
        }
    }
}
