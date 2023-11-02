package cmd.send.battle;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

public class ResponseEndGame extends BaseMsg {

    public ResponseEndGame(short type) {
        super(CmdDefine.END_GAME, type);
    }
}
