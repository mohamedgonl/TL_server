package cmd.send.battle;

import bitzero.server.extensions.data.BaseMsg;

public class ResponseEndGame extends BaseMsg {

    public ResponseEndGame(short type) {
        super(type);
    }
}
