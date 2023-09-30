package service;

import bitzero.server.core.BZEventParam;
import bitzero.server.core.BZEventType;
import bitzero.server.core.IBZEvent;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;

import cmd.CmdDefine;

import cmd.ErrorConst;
import cmd.receive.user.RequestBuyItem;

import cmd.send.user.ResponseBuyItem;
import cmd.send.user.ResponseGetUserInfo;
import event.eventType.DemoEventType;
import extension.FresherExtension;

import model.PlayerInfo;

import org.apache.commons.lang.exception.ExceptionUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.config.GameConfig;
import util.config.ShopResourceItemConfig;
import util.server.ServerConstant;

public class ShopHandler extends BaseClientRequestHandler {
    public static short USER_MULTI_IDS = 3001;
    private final Logger logger = LoggerFactory.getLogger("UserHandler");

    public ShopHandler() {
        super();
    }

    public void init() {
        getExtension().addEventListener(BZEventType.USER_DISCONNECT, this);
        getExtension().addEventListener(BZEventType.USER_RECONNECTION_SUCCESS, this);

        /**
         *  register new event, so the core will dispatch event type to this class
         */
        getExtension().addEventListener(DemoEventType.CHANGE_NAME, this);
    }

    private FresherExtension getExtension() {
        return (FresherExtension) getParentExtension();
    }

    public void handleServerEvent(IBZEvent ibzevent) {

        if (ibzevent.getType() == BZEventType.USER_DISCONNECT)
            this.userDisconnect((User) ibzevent.getParameter(BZEventParam.USER));
        else if (ibzevent.getType() == DemoEventType.CHANGE_NAME){

        }
    }

    public void handleClientRequest(User user, DataCmd dataCmd) {
        logger.info("requestId: " + dataCmd.getId());
        try {
            switch (dataCmd.getId()) {
                case CmdDefine.BUY_ITEM:
                    RequestBuyItem reqInfo = new RequestBuyItem(dataCmd);
                    this.handleBuyResItem(user, reqInfo);
                    break;
            }
        } catch (Exception e) {
            logger.warn("USERHANDLER EXCEPTION " + e.getMessage());
            logger.warn(ExceptionUtils.getStackTrace(e));
        }

    }





    private void userDisconnect(User user) {
        // log user disconnect
    }

    private void handleBuyResItem(User user, RequestBuyItem requestBuyItem){
        PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
//        if (!requestBuyItem.isValid()){
//            send(new ResponseBuyItem(ErrorConst.PARAM_INVALID), user);
//        }

        try {
            String itemCfgId = requestBuyItem.getItemCfgId();

            // check item id có trong config không
            for (int i = 0; i < GameConfig.getInstance().shopResItemConfig.size(); i++) {
                if(itemCfgId ==
                        GameConfig.getInstance().shopResItemConfig.get("category_ngankho").get(i).cfgId){
                    ShopResourceItemConfig resItem = GameConfig.getInstance().shopResItemConfig.get("category_ngankho").get(i);
                    if(resItem.price > userInfo.getGem()){
                        send(new ResponseBuyItem(ErrorConst.GEM_NOT_ENOUGH),user);
                        return;
                    }
                    else {
                        float percent = resItem.nganhko_percent;
                        switch (resItem.value_type){
                            case "RESOURCE_TYPE.ELIXIR": {

                                break;
                            }
                            case "RESOURCE_TYPE.GOLD": {

                                break;
                            }
                            default: {

                                break;
                            }
                        }





                    }


                }
            }

            send(new ResponseBuyItem(ErrorConst.ITEM_NOT_EXIST),user);


        }
        catch (Exception e){

        }
    }


}