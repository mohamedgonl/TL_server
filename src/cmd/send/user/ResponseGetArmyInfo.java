package cmd.send.user;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;
import java.util.Map;

public class ResponseGetArmyInfo extends BaseMsg {
    private Map<String, Integer> listTroops;

    public ResponseGetArmyInfo(short error) {
        super(CmdDefine.GET_ARMY_INFO, error);
    }

    public ResponseGetArmyInfo(short error, Map<String, Integer> listTroops) {
        super(CmdDefine.GET_ARMY_INFO, error);
        this.listTroops = listTroops;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        if (listTroops != null) {
            bf.putInt(listTroops.size());
            for (Map.Entry<String, Integer> set : listTroops.entrySet()) {
                putStr(bf, set.getKey());
                bf.putInt(set.getValue());
            }
        }
        return packBuffer(bf);
    }
}
