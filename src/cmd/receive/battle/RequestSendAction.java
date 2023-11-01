package cmd.receive.battle;

import battle_models.BattleAction;
import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.common.business.CommonHandle;
import util.BattleConst;

import java.nio.ByteBuffer;

public class RequestSendAction extends BaseCmd {

    private BattleAction action;

    public RequestSendAction(DataCmd dataCmd) {
        super(dataCmd);
        unpackData();
    }

    public BattleAction getAction() {
        return action;
    }

    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {

            this.action.type = readInt(bf);
            this.action.tick = readInt(bf);

            // đọc data của action
            if(this.action.type == BattleConst.ACTION_THROW_TROOP) {
                 String troopType = readString(bf);
                 int posX = readInt(bf);
                 int posY = readInt(bf);
                this.action.setData(troopType, posX, posY);
            }

        } catch (Exception e) {
            CommonHandle.writeErrLog(e);
        }
    }

    public static ByteBuffer createByteBuffer(BattleAction action){


        int bufferSize = 4 * 2; // Kích thước cho type và tick

        if (action.type == BattleConst.ACTION_THROW_TROOP) {
            bufferSize += 4 + action.troopType.length() + 4 + 4; // Kích thước cho troopType, posX và posY
        }

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putInt(action.type);
        buffer.putInt(action.tick);

        if (action.type == BattleConst.ACTION_THROW_TROOP) {
            byte[] troopTypeBytes = action.troopType.getBytes();
            buffer.putInt(troopTypeBytes.length);
            buffer.put(troopTypeBytes);
            buffer.putInt(action.posX);
            buffer.putInt(action.posY);
        }

        buffer.flip(); // Đảm bảo rằng buffer sẽ đọc từ đầu

        return buffer;

    }

    @Override
    public String toString() {
        return "RequestSendAction{" +
                "action=" + action +
                '}';
    }
}
