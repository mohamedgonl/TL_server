package cmd.receive.authen;

import bitzero.server.extensions.data.BaseCmd;
import bitzero.server.extensions.data.DataCmd;
import bitzero.util.common.business.CommonHandle;

import java.nio.ByteBuffer;

public class RequestLogin extends BaseCmd {
    public String sessionKey = "";
    public int userId = 0;
    public RequestLogin(DataCmd dataCmd) {
        super(dataCmd);
    }
    
    @Override
    public void unpackData() {
        ByteBuffer bf = makeBuffer();
        try {
//            sessionKey = readString(bf);
            userId = readInt(bf);
        } catch (Exception e) {
            CommonHandle.writeErrLog(e);
        }
    }
    
}
