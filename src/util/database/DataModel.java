package util.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Barrack;
import model.Building;
import model.CollectorBuilding;
import model.Obstacle;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import util.server.ServerUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataModel {
    private static final Map<String, List<Field>> cachedMapField = new HashMap<String, List<Field>>();
    static Gson gson = new Gson();
    static Gson gsonDeserializer = initGsonDeserializerInstance();

    public DataModel() {
        super();
    }

    private static Gson initGsonDeserializerInstance() {
        BuildingDeserializer deserializer = new BuildingDeserializer("type", Building.class);
        deserializer.registerBarnType("RES", CollectorBuilding.class);
        deserializer.registerBarnType("BAR", Barrack.class);
        deserializer.registerBarnType("OBS", Obstacle.class);

        return new GsonBuilder()
                .registerTypeAdapter(Building.class, deserializer)
                .create();
    }

    public static Object getModel(String key, Class c) throws Exception {
        String globalKey = ServerUtil.getModelKeyName(c.getSimpleName(), key);
        return gsonDeserializer.fromJson((String) DataHandler.get(globalKey), c);
        //return DataHandler.get(key);
    }

    public static Object getModel(int uId, Class c) throws Exception {
        String key = ServerUtil.getModelKeyName(c.getSimpleName(), uId);
        return gsonDeserializer.fromJson((String) DataHandler.get(key), c);
        //return DataHandler.get(key);
    }

    public static Object getModel(long uId, Class c) throws Exception {
        String key = ServerUtil.getModelKeyName(c.getSimpleName(), uId);
        return gsonDeserializer.fromJson((String) DataHandler.get(key), c);
        //return DataHandler.get(key);
    }

    public static Object getSocialModel(long uId, Class c) throws Exception {
        String key = ServerUtil.getSocialModelKeyName(c.getSimpleName(), uId);
        return DataHandler.get(key);
    }

    public static CASValue getS(int uId, Class c) throws Exception {
        String key = ServerUtil.getModelKeyName(c.getSimpleName(), uId);
        return DataHandler.getS(key);
    }

    public void saveModel(String key) throws Exception {
        String globalKey = ServerUtil.getModelKeyName(this.getClass().getSimpleName(), key);
        String sobj = gson.toJson(this);
        DataHandler.set(globalKey, sobj);
    }

    public void saveModel(int uId) throws Exception {
        String key = ServerUtil.getModelKeyName(this.getClass().getSimpleName(), uId);
        String sobj = gson.toJson(this);
        DataHandler.set(key, sobj);
        //DataHandler.set(key, this);
    }

    public void saveModel(long uId) throws Exception {
        String key = ServerUtil.getModelKeyName(this.getClass().getSimpleName(), uId);
        String sobj = gson.toJson(this);
        DataHandler.set(key, sobj);
        //DataHandler.set(key, this);
    }

    public void saveSocialModel(long uId) throws Exception {
        String key = ServerUtil.getSocialModelKeyName(this.getClass().getSimpleName(), uId);
        DataHandler.set(key, this);
    }

    public boolean checkAndSet(int uId, long valCAS) throws Exception {
        String key = ServerUtil.getModelKeyName(this.getClass().getSimpleName(), uId);
        CASResponse casRes = DataHandler.checkAndSet(key, valCAS, this);
        return casRes.equals(CASResponse.OK);
    }

}
