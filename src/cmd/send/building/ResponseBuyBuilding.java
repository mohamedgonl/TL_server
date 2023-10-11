package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.Building;

import java.nio.ByteBuffer;

public class ResponseBuyBuilding extends BaseMsg {
    private Building building;

    public ResponseBuyBuilding(short error) {
        super(CmdDefine.BUY_BUILDING, error);
    }

    public ResponseBuyBuilding(short error, Building building) {
        super(CmdDefine.BUY_BUILDING, error);
        this.building = building;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        if (building != null) {
            bf.putInt(building.getId());
            putStr(bf, building.getType());
            bf.putShort((short) building.getPosition().x);
            bf.putShort((short) building.getPosition().y);
            bf.putShort(building.getStatus().getValue());
            bf.putInt(building.getStartTime());
            bf.putInt(building.getEndTime());
        }
        return packBuffer(bf);
    }
}
