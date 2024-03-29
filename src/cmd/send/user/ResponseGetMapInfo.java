package cmd.send.user;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;
import model.Building;
import model.CollectorBuilding;
import model.PlayerInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ResponseGetMapInfo extends BaseMsg {
    public PlayerInfo info;

    public ResponseGetMapInfo(short error) {
        super(CmdDefine.GET_MAP_INFO, error);
    }

    public ResponseGetMapInfo(short error, PlayerInfo _info) {
        super(CmdDefine.GET_MAP_INFO, error);
        info = _info;
    }

    @Override
    public byte[] createData() {
        ArrayList<Building> buildings = info.getListBuildings();
        ByteBuffer bf = makeBuffer();

        bf.putInt(buildings.size());
        for (Building building : buildings) {
            bf.putInt(building.getId());
            bf.putInt(building.getLevel());
            putStr(bf, building.getType());
            bf.putInt((int)building.getPosition().getX());
            bf.putInt((int)building.getPosition().getY());
            bf.putShort(building.getStatus().getValue());
            bf.putInt(building.getStartTime());
            bf.putInt(building.getEndTime());
            if (building instanceof CollectorBuilding){
                bf.putInt(((CollectorBuilding) building).getLastCollectTime());
            }
        }
        return packBuffer(bf);
    }
}
