package cmd.send.battle;

import battle_models.BattleBuilding;
import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.Match;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class ResponseMatchingPlayer extends BaseMsg {
    private Match match;

    public ResponseMatchingPlayer(short error) {
        super(CmdDefine.BATTLE_MATCHING, error);
    }

    public ResponseMatchingPlayer(short error, Match match) {
        super(CmdDefine.BATTLE_MATCHING, error);
        this.match = match;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();

        // list building
        ArrayList<BattleBuilding> battleBuildings = this.match.buildings;
        bf.putInt(battleBuildings.size());
        for (BattleBuilding building : battleBuildings) {
//          bf.putInt(building.getId());
            putStr(bf, building.type);
            bf.putInt(building.level);
            bf.putInt(building.gold);
            bf.putInt(building.elixir);
            bf.putInt(building.posX);
            bf.putInt(building.posY);
        }

        // list troop
        Map<String, Integer> army = this.match.army;

        if (army != null) {
            bf.putInt(army.size());
            for (Map.Entry<String, Integer> set : army.entrySet()) {
                putStr(bf, set.getKey());
                bf.putInt(set.getValue());
            }
        }




        return packBuffer(bf);
    }
}
