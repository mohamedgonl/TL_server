package cmd.send.battle;

import battle_models.BattleBuilding;
import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import battle_models.BattleMatch;
import model.PlayerInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class ResponseMatchingPlayer extends BaseMsg {
    private BattleMatch match;
    private PlayerInfo playerInfo;

    public ResponseMatchingPlayer(short error) {
        super(CmdDefine.BATTLE_MATCHING, error);
    }

    public ResponseMatchingPlayer(short error, BattleMatch match, PlayerInfo playerInfo) {
        super(CmdDefine.BATTLE_MATCHING, error);
        this.match = match;
        this.playerInfo = playerInfo;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(this.match.id);
        // thông tin đối thủ
        bf.putInt(this.match.enemyId);
        putStr(bf,this.match.enemyName);
        bf.putInt(this.match.maxGold);
        bf.putInt(this.match.maxElixir);



        // list building
        ArrayList<BattleBuilding> battleBuildings = this.match.buildings;
        bf.putInt(battleBuildings.size());
        for (BattleBuilding building : battleBuildings) {
            bf.putInt(building.id);
            putStr(bf, building.type);
            bf.putInt(building.level);
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

        // điểm danh vọng
        bf.putInt(this.match.winPoint);
        bf.putInt(this.match.losePoint);

        // tài nguyên người chơi

        bf.putInt(this.playerInfo.getGold());
        bf.putInt(this.playerInfo.getGoldCapacity());

        bf.putInt(this.playerInfo.getElixir());
        bf.putInt(this.playerInfo.getElixirCapacity());

        bf.putInt(this.playerInfo.getGem());

        return packBuffer(bf);
    }
}
