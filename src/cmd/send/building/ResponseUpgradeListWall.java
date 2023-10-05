package cmd.send.building;

import bitzero.server.extensions.data.BaseMsg;
import cmd.CmdDefine;

import java.nio.ByteBuffer;

public class ResponseUpgradeListWall extends BaseMsg {
    private int[] buildingIds;

    public ResponseUpgradeListWall(short error) {
        super(CmdDefine.UPGRADE_LIST_WALL, error);
    }

    public ResponseUpgradeListWall(short error, int[] buildingIds) {
        super(CmdDefine.UPGRADE_LIST_WALL, error);
        this.buildingIds = buildingIds;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        if (buildingIds != null) {
            bf.putShort((short) buildingIds.length);
            for (int i = 0; i < buildingIds.length; i++) {
                bf.putInt(buildingIds[i]);
            }
        }
        return packBuffer(bf);
    }
}
