package cmd.send.battle;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;


public class ResponseSendAction  extends BaseMsg {


    public ResponseSendAction(short error) {
        super(CmdDefine.SEND_ACTION, error);
    }


}
