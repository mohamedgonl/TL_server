package cmd.receive.battle;

import battle_models.BattleAction;
import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.common.business.CommonHandle;
import util.BattleConst;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestEndGame extends BaseCmd {
    public int getGoldGot() {
        return goldGot;
    }

    public int getElixirGot() {
        return elixirGot;
    }

    public int getTrophy() {
        return trophy;
    }

    public int getStars() {
        return stars;
    }

    public boolean getResult() {
        return result;
    }

    public Map<String, Integer> getArmy() {
        return army;
    }

    private int goldGot;
    private int elixirGot;
    private int trophy;

    private int stars;

    private boolean result;

    private int tick;

    private Map<String, Integer> army = new HashMap<>();


    public RequestEndGame(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }



    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
            this.result = readInt(bf) != 0;
            this.stars = readInt(bf);
            this.trophy = readInt(bf);
            this.goldGot = readInt(bf);
            this.elixirGot = readInt(bf);

            int armySize = readInt(bf);
            if(armySize != 0) {
                for (int i = 0; i < armySize; i++) {
                    this.army.put(readString(bf), readInt(bf));
                }
            }

            this.tick = readInt(bf);

        } catch (Exception e) {
            CommonHandle.writeErrLog(e);
        }
    }

    public int getTick() {
        return tick;
    }

    @Override
    public String toString() {
        return "RequestEndGame{" +
                "goldGot=" + goldGot +
                ", elixirGot=" + elixirGot +
                ", trophy=" + trophy +
                ", stars=" + stars +
                ", result=" + result +
                ", tick=" + tick +
                ", army=" + army +
                '}';
    }
}
