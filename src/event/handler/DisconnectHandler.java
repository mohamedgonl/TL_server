package event.handler;

import battle_models.BattleMatch;
import bitzero.server.core.BZEventParam;
import bitzero.server.core.IBZEvent;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseServerEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.server.ServerConstant;

public class DisconnectHandler extends BaseServerEventHandler {

    private final Logger logger = LoggerFactory.getLogger("LogoutHandler");

    public DisconnectHandler() {
        super();
    }

    public void handleServerEvent(IBZEvent iBZEvent) {
        this.onDisconnect((User) iBZEvent.getParameter(BZEventParam.USER));
    }

    private void onDisconnect(User user) {
//        BattleMatch match = (BattleMatch) user.getProperty(ServerConstant.MATCH);

    }


}
