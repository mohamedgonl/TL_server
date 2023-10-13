package service;

import bitzero.server.core.IBZEvent;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseClientRequestHandler;
import bitzero.server.extensions.data.DataCmd;
import cmd.CmdDefine;
import cmd.ErrorConst;
import cmd.receive.user.RequestBuyItem;
import cmd.receive.user.RequestBuyResourceByGem;
import cmd.send.user.ResponseBuyItem;
import cmd.send.user.ResponseBuyResourceByGem;
import extension.FresherExtension;
import model.PlayerInfo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.GameConfig;
import util.config.ShopResourceItemConfig;
import util.server.ServerConstant;

public class ShopHandler extends BaseClientRequestHandler {
    public static short SHOP_MULTI_IDS = 4000;
    private final Logger logger = LoggerFactory.getLogger("UserHandler");

    public ShopHandler() {
        super();
    }

    public void init() {
//        getExtension().addEventListener(BZEventType.USER_DISCONNECT, this);
//        getExtension().addEventListener(BZEventType.USER_RECONNECTION_SUCCESS, this);

        /**
         *  register new event, so the core will dispatch event type to this class
         */
//        getExtension().addEventListener(DemoEventType.CHANGE_NAME, this);
    }

    private FresherExtension getExtension() {
        return (FresherExtension) getParentExtension();
    }

    public void handleServerEvent(IBZEvent ibzevent) {
//        if (ibzevent.getType() == BZEventType.USER_DISCONNECT)
//            this.userDisconnect((User) ibzevent.getParameter(BZEventParam.USER));
//        else if (ibzevent.getType() == DemoEventType.CHANGE_NAME){
//
//        }
    }

    public void handleClientRequest(User user, DataCmd dataCmd) {
        logger.info("requestId: " + dataCmd.getId());
        try {
            switch (dataCmd.getId()) {
                case CmdDefine.BUY_RESOURCE:
                    RequestBuyItem reqInfo = new RequestBuyItem(dataCmd);
                    this.handleBuyResItem(user, reqInfo);
                    break;
                case CmdDefine.BUY_RESOURCE_BY_GEM:
                    RequestBuyResourceByGem reqBuyResourceByGem = new RequestBuyResourceByGem(dataCmd);
                    this.handleBuyResourceByGem(user, reqBuyResourceByGem);
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

    private void handleBuyResItem(User user, RequestBuyItem requestBuyItem) {
        System.out.println("HANDLE BUY RESOURCE ITEM");
        PlayerInfo userInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);
        if (!requestBuyItem.isValid()) {
            send(new ResponseBuyItem(ErrorConst.PARAM_INVALID), user);
        }
        try {
            String itemCfgId = requestBuyItem.getItemCfgId();
            // check item id có trong config không
            for (int i = 0; i < GameConfig.getInstance().shopResItemConfig.get("category_ngankho").size(); i++) {
                if (itemCfgId.equals(GameConfig.getInstance().shopResItemConfig.get("category_ngankho").get(i).cfgId)) {
                    ShopResourceItemConfig resItem = GameConfig.getInstance().shopResItemConfig.get("category_ngankho").get(i);
                    if (resItem.price > userInfo.getGem()) {
                        send(new ResponseBuyItem(ErrorConst.GEM_NOT_ENOUGH), user);
                        return;
                    } else {
                        // update resource
                        float percent = resItem.nganhko_percent;

                        userInfo.addResources(resItem.value_type.equals("RESOURCE_TYPE.GOLD") ? (int) (userInfo.getGoldCapacity() * percent) : 0,
                                resItem.value_type.equals("RESOURCE_TYPE.ELIXIR") ? (int) (userInfo.getElixirCapacity() * percent) : 0, 0);
                        userInfo.setGem(userInfo.getGem() - resItem.price);

                        userInfo.saveModel(userInfo.getId());

                        System.out.println("DEBUG send buy success");
                        send(new ResponseBuyItem(ErrorConst.SUCCESS, userInfo.getGold(), userInfo.getElixir(), userInfo.getGem()), user);
                        return;
                    }
                }
            }
            send(new ResponseBuyItem(ErrorConst.ITEM_NOT_EXIST), user);
        } catch (Exception e) {
            System.out.println("BUY ITEM ERROR " + e.getMessage());
            send(new ResponseBuyItem(ErrorConst.UNKNOWN), user);
        }
    }

    private void handleBuyResourceByGem(User user, RequestBuyResourceByGem reqData) {
        try {
            if (!reqData.isValid()) {
                send(new ResponseBuyResourceByGem(ErrorConst.PARAM_INVALID), user);
            }

            //get user from cache
            PlayerInfo playerInfo = (PlayerInfo) user.getProperty(ServerConstant.PLAYER_INFO);

            if (playerInfo == null) {
                send(new ResponseBuyResourceByGem(ErrorConst.PLAYER_INFO_NULL), user);
                return;
            }

            synchronized (playerInfo) {
                //check capacity
                if (playerInfo.getGold() + reqData.getGold() > playerInfo.getGoldCapacity()
                        || playerInfo.getElixir() + reqData.getElixir() > playerInfo.getElixirCapacity()) {
                    send(new ResponseBuyResourceByGem(ErrorConst.TOO_MUCH_RESOURCES), user);
                    return;
                }

                int gemCost = getGemCost(reqData.getGold(), reqData.getElixir());
                if (gemCost > playerInfo.getGem()) {
                    send(new ResponseBuyResourceByGem(ErrorConst.NOT_ENOUGH_RESOURCES), user);
                    return;
                }

                //success
                playerInfo.useResources(0, 0, gemCost);
                playerInfo.addResources(reqData.getGold(), reqData.getElixir(), 0);
            }
            playerInfo.saveModel(user.getId());
            send(new ResponseBuyResourceByGem(ErrorConst.SUCCESS, playerInfo.getGem(), playerInfo.getGold(), playerInfo.getElixir()), user);
        } catch (Exception e) {
            System.out.println("BUY ITEM ERROR " + e.getMessage());
            send(new ResponseBuyResourceByGem(ErrorConst.UNKNOWN), user);
        }
    }

    private int getGemCost(int gold, int elixir) {
        int goldPerGem = 400;
        int elixirPerGem = 500;
        return (int) Math.ceil((double) gold / goldPerGem) + (int) Math.ceil((double) elixir / elixirPerGem);
    }
}
