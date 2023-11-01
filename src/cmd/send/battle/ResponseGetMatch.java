package cmd.send.battle;

import battle_models.BattleMatch;
import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseGetMatch extends BaseMsg {
    private BattleMatch match;

    public ResponseGetMatch(short error) {
        super(CmdDefine.GET_MATCH, error);
    }

    public ResponseGetMatch(short error, BattleMatch match) {
        super(CmdDefine.BUILD_SUCCESS, error);
        this.match = match;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        return packBuffer(bf);
    }
}
