package cmd.send.battle;

import battle_models.BattleAction;
import battle_models.BattleBuilding;
import battle_models.BattleGameObject;
import battle_models.BattleMatch;
import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import util.BattleConst;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class ResponseGetMatch extends BaseMsg {
    private BattleMatch match;

    public ResponseGetMatch(short error) {
        super(CmdDefine.GET_MATCH, error);
    }

    public ResponseGetMatch(short error, BattleMatch match) {
        super(CmdDefine.GET_MATCH, error);
        this.match = match;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        if (this.match != null) {
            bf.putInt(this.match.id);
            bf.putInt(this.match.enemyId);
            putStr(bf, this.match.enemyName);
            bf.putInt(this.match.trophy);
            bf.putInt(this.match.winTrophy);
            bf.putInt(this.match.loseTrophy);
            bf.putInt(this.match.maxGold);
            bf.putInt(this.match.maxElixir);
            bf.putInt(this.match.getGoldGot());
            bf.putInt(this.match.getElixirGot());
            putBoolean(bf, this.match.isWin);
            bf.putInt(this.match.winPercentage);
            bf.putInt(this.match.stars);

            // army
            bf.putInt(this.match.army.size());
            for (Map.Entry<String, Integer> entry : this.match.army.entrySet()) {
                putStr(bf, entry.getKey());
                bf.putInt(entry.getValue());
            }

            // building
            bf.putInt( this.match.listGameObjects.size());
            for (BattleGameObject building :  this.match.listGameObjects) {
                bf.putInt(building.id);
                putStr(bf, building.type);
                bf.putInt(building.level);
                bf.putInt(building.posX);
                bf.putInt(building.posY);
            }

            // action

            bf.putInt( this.match.getActionsList().size());
            for (BattleAction action :  this.match.getActionsList()) {
                int type = action.type;
                bf.putInt(type);
                bf.putInt(action.tick);

                if(type == BattleConst.ACTION_THROW_TROOP) {
                    putStr(bf, action.troopType);
                    bf.putInt(action.posX);
                    bf.putInt(action.posY);
                }

            }
        }
        return packBuffer(bf);
    }

    @Override
    public String toString() {
        return "ResponseGetMatch{" +
                "match=" + match.toString() +
                '}';
    }
}
