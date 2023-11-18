package service.battle;

import battle_models.BattleMatch;
import bitzero.server.entities.User;
import cmd.ErrorConst;
import cmd.receive.battle.RequestSendAction;
import cmd.send.battle.ResponseSendAction;
import model.ListPlayerData;
import util.BattleConst;
import util.Common;
import util.server.CustomException;
import util.server.ServerConstant;

public class ActionHandler {
    public static ResponseSendAction handleReceiveAction(User user, RequestSendAction requestSendAction) throws Exception {

        BattleMatch match = (BattleMatch) user.getProperty(ServerConstant.MATCH);

        synchronized (match) {
            // Match ended
            if (match.state == BattleConst.MATCH_ENDED) {
                throw new CustomException(ErrorConst.MATCH_ENDED);
            }

            // action start game
            if (requestSendAction.getAction().type == BattleConst.ACTION_START) {

                if (match.state == BattleConst.MATCH_NEW) {

                    match.state = BattleConst.MATCH_HAPPENING;
                    if (Common.currentTimeInSecond() > match.createTime + BattleConst.COUNT_DOWN_TIME) {
                        match.startTime = match.createTime + BattleConst.COUNT_DOWN_TIME;
                    } else {
                        match.startTime = Common.currentTimeInSecond();
                    }

                } else {
                    throw new CustomException(ErrorConst.BATTLE_ACTION_INVALID);
                }
            }


            // action thả lính
            if (requestSendAction.getAction().type == BattleConst.ACTION_THROW_TROOP) {

                if (match.state == BattleConst.MATCH_HAPPENING || match.state == BattleConst.MATCH_NEW) {

                    if (match.state == BattleConst.MATCH_NEW) {
                        match.startTime = Math.min(Common.currentTimeInSecond(), match.createTime + BattleConst.COUNT_DOWN_TIME);
                        match.state = BattleConst.MATCH_HAPPENING;
                    }

                    // trận đấu đã kết thúc, không nhận action thả lính
                    if (Common.currentTimeInSecond() > match.startTime + BattleConst.MAX_TIME_A_MATCH) {
                        match.state = BattleConst.MATCH_ENDED;
                        match.pushAction(requestSendAction.getAction());
                        user.setProperty(ServerConstant.MATCH, match);
                        throw new CustomException(ErrorConst.MATCH_ENDED);

                    }
                    // vị trí ko hợp lệ
                    else if (!match.checkValidThrowTroopPos(requestSendAction.getAction().posX, requestSendAction.getAction().posY)) {
                        throw new CustomException(ErrorConst.INVALID_THROW_TROOP_POSITION);
                    }

                    // nếu đã thả hết lính => không lưu action
                    else if (!match.checkValidTroopCount(requestSendAction.getAction())) {
                        throw new CustomException(ErrorConst.TROOP_EMPTY);
                    }


                } else {
                    throw new CustomException(ErrorConst.BATTLE_ACTION_INVALID);

                }

            }

            // action kết thúc trận
            if (requestSendAction.getAction().type == BattleConst.ACTION_END) {
                if (match.state == BattleConst.MATCH_HAPPENING) {
                    match.state = BattleConst.MATCH_ENDED;

                    // reset enemy state
                    ListPlayerData listUserData = (ListPlayerData) ListPlayerData.getModel(ServerConstant.LIST_USER_DATA_ID, ListPlayerData.class);
                    listUserData.updateUser(match.enemyId, false);
                    listUserData.saveModel(ServerConstant.LIST_USER_DATA_ID);

                } else {
                    throw new CustomException(ErrorConst.BATTLE_ACTION_INVALID);
                }
                match.pushAction(requestSendAction.getAction());
                match.sync();
                MatchHandler.handleGameEndSync(user);
                return new ResponseSendAction(ErrorConst.SUCCESS);
            }

            match.pushAction(requestSendAction.getAction());
        }

        user.setProperty(ServerConstant.MATCH, match);
        return new ResponseSendAction(ErrorConst.SUCCESS);
    }
}
