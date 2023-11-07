package cmd.send.battle;

import battle_models.BattleMatch;
import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class ResponseGetHistoryAttack extends BaseMsg {
    ArrayList<BattleMatch> matches = new ArrayList<>();

    public ResponseGetHistoryAttack(short type) {
        super(CmdDefine.GET_HISTORY_ATTACK, type);
    }

    public ResponseGetHistoryAttack(short type, ArrayList<BattleMatch> matches) {
        super(CmdDefine.GET_HISTORY_ATTACK, type);
        this.matches = matches;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        bf.putInt(matches.size());
        for (BattleMatch match : this.matches) {
            bf.putInt(match.id);
            bf.putInt(match.enemyId);
            putStr(bf, match.enemyName);
            bf.putInt(match.trophy);
            bf.putInt(match.getGoldGot());
            bf.putInt(match.getElixirGot());
            bf.putInt(match.isWin?1:0);
            bf.putInt(match.winPercentage);
            bf.putInt(match.stars);
            bf.putInt(match.createTime);

            // army
            bf.putInt(match.usedArmy.size());
            for (Map.Entry<String, Integer> entry : match.usedArmy.entrySet()) {
                putStr(bf, entry.getKey());
                bf.putInt(entry.getValue());
            }
        }
        return packBuffer(bf);
    }

    @Override
    public String toString() {
        StringBuilder matchesString = new StringBuilder();
        for (BattleMatch match :
                this.matches) {
            matchesString.append(match.toString()).append("\n");
        }
        return "ResponseGetHistoryAttack{" +
                "matches=" + matchesString +
                '}';
    }
}
