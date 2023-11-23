package event.handler;

import bitzero.server.core.BZEventParam;
import bitzero.server.core.IBZEvent;
import bitzero.server.entities.User;
import bitzero.server.extensions.BaseServerEventHandler;
import bitzero.server.extensions.ExtensionLogLevel;
import bitzero.util.ExtensionUtility;
import bitzero.util.common.business.CommonHandle;
import event.eventType.DemoEventParam;
import model.Building;
import model.ListPlayerData;
import model.Obstacle;
import model.PlayerInfo;
import util.BuildingFactory;
import util.Common;
import util.GameConfig;
import util.config.BaseBuildingConfig;
import util.config.InitGameConfig;
import util.config.StorageConfig;
import util.config.TownHallConfig;
import util.server.ServerConstant;

import java.awt.*;
import java.util.*;
import java.util.List;

public class LoginSuccessHandler extends BaseServerEventHandler {
    public LoginSuccessHandler() {
        super();
    }

    public void handleServerEvent(IBZEvent iBZEvent) {
        this.onLoginSuccess((User) iBZEvent.getParameter(BZEventParam.USER));
    }

    /**
     * @param user description: after login successful to server, core framework will dispatch this event
     */
    public void onLoginSuccess(User user) {

        try {


            trace(ExtensionLogLevel.DEBUG, "On Login Success ", user.getName());
            PlayerInfo pInfo = null;

            pInfo = (PlayerInfo) PlayerInfo.getModel(user.getId(), PlayerInfo.class);


            ListPlayerData listUserData = null;

            listUserData = (ListPlayerData) ListPlayerData.getModel(ServerConstant.LIST_USER_DATA_ID, ListPlayerData.class);

            if (listUserData == null) {
                listUserData = new ListPlayerData();
            }


            if (pInfo == null) {
                pInfo = new PlayerInfo(user.getId(), "username_" + user.getId());

                initNewPlayerInfo(pInfo);
                //save to db
                pInfo.saveModel(user.getId());
                listUserData.updateUser(user.getId(), false);

            }

            // create fake accounts
            if (user.getId() == ServerConstant.CREATE_FAKE_ACCOUNTS) {
                for (int i = ServerConstant.CREATE_FAKE_ACCOUNTS; i > ServerConstant.CREATE_FAKE_ACCOUNTS - 1000; i--) {
                    PlayerInfo pInfoFake = new PlayerInfo(i, "username_" + i);
                    createRandomPlayerInfo(pInfoFake);
                    pInfoFake.saveModel(i);
                    listUserData.updateUser(i, false);
                }
            }

            listUserData.saveModel(ServerConstant.LIST_USER_DATA_ID);

            //init map & capacity from buildings
            initPlayerData(pInfo);

            /**
             * cache playerinfo in RAM
             */
            user.setProperty(ServerConstant.PLAYER_INFO, pInfo);

            /**
             * send login success to client
             * after receive this message, client begin to send game logic packet to server
             */
            ExtensionUtility.instance().sendLoginOK(user);

            /**
             * dispatch event here
             */
            Map evtParams = new HashMap();
            evtParams.put(DemoEventParam.USER, user);
            evtParams.put(DemoEventParam.NAME, user.getName());
//        ExtensionUtility.dispatchEvent(new BZEvent(DemoEventType.LOGIN_SUCCESS, evtParams));
        } catch (Exception e) {
            trace("LOGIN HANDLE ERROR ::: " + e.getMessage());
            CommonHandle.writeErrLog(e);
        }
    }

    private void initNewPlayerInfo(PlayerInfo playerInfo) throws Exception {
        GameConfig gameConfig = GameConfig.getInstance();
        System.out.println("init new player info: " + playerInfo.getId());

        InitGameConfig initGameConfig = gameConfig.initGameConfig;

        playerInfo.setGold(initGameConfig.player.gold);
        playerInfo.setGem(initGameConfig.player.coin);
        playerInfo.setElixir(initGameConfig.player.elixir);

        List buildings = new ArrayList<Building>();
        int id = 1;

        for (Map.Entry<String, InitGameConfig.MapElement> entry : initGameConfig.map.entrySet()) {
            String type = entry.getKey();
            InitGameConfig.MapElement data = entry.getValue();

            Building building = BuildingFactory.getBuilding(id, type, 1, new Point(data.posX - 1, data.posY - 1));

            buildings.add(building);
            id++;
        }

        for (Map.Entry<Integer, InitGameConfig.ObsElement> entry : initGameConfig.obs.entrySet()) {
            InitGameConfig.ObsElement data = entry.getValue();

            Building building = BuildingFactory.getBuilding(id, data.type, 1, new Point(data.posX - 1, data.posY - 1));
            buildings.add(building);
            id++;
        }

        playerInfo.setListBuildings((ArrayList<Building>) buildings);
    }

    private void initPlayerData(PlayerInfo playerInfo) {
        int[][] map = new int[GameConfig.MAP_HEIGHT][GameConfig.MAP_WIDTH];
        Map<String, Integer> buildingAmount = new HashMap<>();

        int totalBuilders = 0;
        int availableBuilders = 0;
        int goldCapacity = 0;
        int elixirCapacity = 0;

        List<Building> listBuildingToRemove = new ArrayList<>();

        for (Building building : playerInfo.getListBuildings()) {
            //update building status
            if (building.getStatus() == Building.Status.ON_BUILD || building.getStatus() == Building.Status.ON_UPGRADE) {
                if (building.getEndTime() <= Common.currentTimeInSecond()) {
                    if (building.getStatus() == Building.Status.ON_BUILD)
                        building.buildSuccess();
                    else building.upgradeSuccess();
                } else {
                    availableBuilders--;
                }
            }

            BaseBuildingConfig buildingDetail = GameConfig.getInstance().getBuildingConfig(building.getType(), building.getLevel());

            if (building instanceof Obstacle) {
                if (((Obstacle) building).isRemove()) {
                    listBuildingToRemove.add(building);
                    continue;
                }
            } else {
                //update buildingAmount
                buildingAmount.put(building.getType(), buildingAmount.getOrDefault(building.getType(), 0) + 1);
            }

            //update map
            if (buildingDetail.height <= 0 || buildingDetail.height <= 0)
                return;

            for (int i = 0; i < buildingDetail.width; i++)
                for (int j = 0; j < buildingDetail.height; j++) {
                    int posX = (int) (building.getPosition().getX() + i);
                    int posY = (int) (building.getPosition().getY() + j);
                    if (posX < GameConfig.MAP_WIDTH && posY < GameConfig.MAP_HEIGHT)
                        map[posY][posX] = building.getId();
                }

            //update resources capacity
            if (BuildingFactory.isTownHallBuilding(building.getType())) {
                TownHallConfig townHall = (TownHallConfig) buildingDetail;
                playerInfo.setTownHallType(building.getType());
                playerInfo.setTownHallLv(building.getLevel());
                goldCapacity += townHall.capacityGold;
                elixirCapacity += townHall.capacityElixir;
            } else if (BuildingFactory.isBuilderHutBuilding(building.getType())) {
                totalBuilders++;
                availableBuilders++;
            } else if (BuildingFactory.isStorageBuilding(building.getType())) {
                StorageConfig storage = (StorageConfig) buildingDetail;
                switch (storage.type) {
                    case "gold":
                        goldCapacity += storage.capacity;
                        break;
                    case "elixir":
                        elixirCapacity += storage.capacity;
                        break;
                }
            }
        }

        for (Building building : listBuildingToRemove) {
            playerInfo.getListBuildings().remove(building);
        }

        playerInfo.setMap(map);
        playerInfo.setBuildingAmount(buildingAmount);
        playerInfo.setAvaiableBuilders(availableBuilders);
        playerInfo.setTotalBuilders(totalBuilders);
        playerInfo.setGoldCapacity(goldCapacity);
        playerInfo.setElixirCapacity(elixirCapacity);
    }


    public static void createRandomPlayerInfo(PlayerInfo playerInfo) throws Exception {
        GameConfig gameConfig = GameConfig.getInstance();
        System.out.println("init new player info: " + playerInfo.getId());


        playerInfo.setGold(new Random().nextInt(50000)+1);
        playerInfo.setGem(new Random().nextInt(50000)+1);
        playerInfo.setElixir(new Random().nextInt(50000)+1);
        playerInfo.setRank(new Random().nextInt(5000)+1);
        Random random = new Random();

        ArrayList<Building> buildings = new ArrayList<>();
        int[][] map = new int[40][40];

        String[] buildingType = {"AMC_1", "BAR_1", "DEF_1", "DEF_2", "DEF_3", "STO_1", "STO_2", "RES_1", "RES_2", "BDH_1"};
        int typeCount = buildingType.length;

        int wallStartX = random.nextInt(5)+5;
        int wallStartY = random.nextInt(5)+5;
        int wallEndX = random.nextInt(20) + 20;
        int wallEndY = random.nextInt(20) + 20;
        int margin = random.nextInt(2)+3;

        Building building = BuildingFactory.getBuilding(1, BuildingFactory.BuildingType.TOWN_HALL, random.nextInt(ServerConstant.MAX_FAKE_BUILDING_LEVEL) + 1,
                new Point(random.nextInt(wallEndX-wallStartX-3)+wallStartX, random.nextInt(wallEndY-wallStartY-3)+wallStartY ));
        buildings.add(building);
        BaseBuildingConfig tow = gameConfig.getBuildingConfig(BuildingFactory.BuildingType.TOWN_HALL, building.getLevel());
        fillArrayByNewValue(map, building.getPosition().x, building.getPosition().y, tow.width, tow.height,1);

        int id = 2;

        for (int i = wallStartX+margin; i <= wallEndX - margin; i++) {
            for (int j = wallStartY + margin; j <= wallEndY -margin; j++) {
                if(random.nextBoolean())
               {
                    String type = buildingType[random.nextInt(typeCount)];
                    Building building1 = BuildingFactory.getBuilding(id, type, type.equals(BuildingFactory.BuildingType.BUILDER_HUT) ? 1 :random.nextInt(ServerConstant.MAX_FAKE_BUILDING_LEVEL) + 1,
                            new Point(i, j));
                    BaseBuildingConfig buildingConfig = gameConfig.getBuildingConfig(building1.getType(), building1.getLevel());
                    if(checkFill(map,building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height)
                            && i + buildingConfig.height < 40 && j+ buildingConfig.width < 40){
                        fillArrayByNewValue(map, building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height, building1.getId());
                        buildings.add(building1);
                        id++;
                    }
                }
            }
        }

        for (int i = wallStartX - random.nextInt(3) ; i <= wallEndX+ random.nextInt(3); i++) {
            Building building1 = BuildingFactory.getBuilding(id, BuildingFactory.BuildingType.WALL, random.nextInt(ServerConstant.MAX_FAKE_BUILDING_LEVEL) + 1,
                    new Point(i, wallStartY - 2));
            BaseBuildingConfig buildingConfig = gameConfig.getBuildingConfig(building1.getType(), building1.getLevel());
            if(checkFill(map,building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height)
                    && i + buildingConfig.height < 40 && wallStartY+ buildingConfig.width < 40){
                fillArrayByNewValue(map, building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height, building1.getId());
                buildings.add(building1);
                id++;
            }
        }

        // init wall
        for (int i = wallStartX; i <= wallEndX; i++) {
            Building building1 = BuildingFactory.getBuilding(id, BuildingFactory.BuildingType.WALL, random.nextInt(ServerConstant.MAX_FAKE_BUILDING_LEVEL) + 1,
                    new Point(i, wallStartY));
            BaseBuildingConfig buildingConfig = gameConfig.getBuildingConfig(building1.getType(), building1.getLevel());
            if(checkFill(map,building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height)
                    && i + buildingConfig.height < 40 && wallStartY+ buildingConfig.width < 40){
                fillArrayByNewValue(map, building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height, building1.getId());
                buildings.add(building1);
                id++;
            }
        }
        for (int i = wallStartX; i <= wallEndX; i++) {
            Building building1 = BuildingFactory.getBuilding(id, BuildingFactory.BuildingType.WALL, 4,
                    new Point(i, wallEndY));
            BaseBuildingConfig buildingConfig = gameConfig.getBuildingConfig(building1.getType(), building1.getLevel());
            if(checkFill(map,building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height)
                    && i + buildingConfig.height < 40 && wallEndY+ buildingConfig.width < 40){
                fillArrayByNewValue(map, building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height, building1.getId());
                buildings.add(building1);
                id++;
            }
        }
        for (int i = wallStartY; i <= wallEndY; i++) {
            Building building1 = BuildingFactory.getBuilding(id, BuildingFactory.BuildingType.WALL,  1,
                    new Point(wallStartX, i));
            BaseBuildingConfig buildingConfig = gameConfig.getBuildingConfig(building1.getType(), building1.getLevel());
            if(checkFill(map,building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height)
                    && wallStartX + buildingConfig.height < 40 && i+ buildingConfig.width < 40){
                fillArrayByNewValue(map, building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height, building1.getId());
                buildings.add(building1);
                id++;
            }
        }

        for (int i = wallStartY; i <= wallEndY; i++) {
            Building building1 = BuildingFactory.getBuilding(id, BuildingFactory.BuildingType.WALL, random.nextInt(ServerConstant.MAX_FAKE_BUILDING_LEVEL) + 1,
                    new Point(wallEndX, i));
            BaseBuildingConfig buildingConfig = gameConfig.getBuildingConfig(building1.getType(), building1.getLevel());
            if(checkFill(map,building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height)
                    && wallEndX + buildingConfig.height < 40 && i+ buildingConfig.width < 40){
                fillArrayByNewValue(map, building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height, building1.getId());
                buildings.add(building1);
                id++;
            }
        }

        for (int i = 0; i < 40; i+=4) {
            for (int j = 0; j < 40; j+=4) {
                if(random.nextBoolean() && map[i][j] == 0) {
                    Building building1 = BuildingFactory.getBuilding(id, BuildingFactory.GameObjectPrefix.OBSTACLE + "_" + (random.nextInt(10)+1),  1,
                            new Point(i, j));
                    BaseBuildingConfig buildingConfig = gameConfig.getBuildingConfig(building1.getType(), building1.getLevel());
                    if(checkFill(map,building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height)
                            && i + buildingConfig.height < 40 && j+ buildingConfig.width < 40){
                        fillArrayByNewValue(map, building1.getPosition().x, building1.getPosition().y, buildingConfig.width, buildingConfig.height, building1.getId());
                        buildings.add(building1);
                        id++;
                    }
                }
            }
        }



        playerInfo.setListBuildings( buildings);
    }
    private static void fillArrayByNewValue(int[][] array, int xStart, int yStart, int width, int height, int newValue) {
        for (int i = xStart; i < xStart + height && i < array.length; i++) {
            for (int j = yStart; j < yStart + width && j < array[i].length; j++) {
                array[i][j] = newValue;
            }
        }
    }

    private static boolean checkFill(int[][] array, int xStart, int yStart, int width, int height) {
        try {
        for (int i = xStart; i < xStart + height && i < array.length; i++) {
            for (int j = yStart; j < yStart + width && j < array[i].length; j++) {
                if(array[i][j] != 0) return false;
            }
        }
        return true;
        }
        catch (Exception e) {
            return  false;
        }
    }


}
