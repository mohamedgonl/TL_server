package util.database;

import com.google.gson.*;
import model.Building;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BuildingDeserializer implements JsonDeserializer<Building> {
    private String buildingTypeElementName;
    private Gson gson;
    private Map<String, Class<? extends Building>> buildingTypeRegistry;
    private Class defaultBuildingClass;

    public BuildingDeserializer(String buildingTypeElementName, Class defaultBuildingClass) {
        this.buildingTypeElementName = buildingTypeElementName;
        this.gson = new Gson();
        this.buildingTypeRegistry = new HashMap<>();
        this.defaultBuildingClass = defaultBuildingClass;
    }

    public void registerBarnType(String buildingTypeName, Class<? extends Building> buildingType) {
        buildingTypeRegistry.put(buildingTypeName, buildingType);
    }

    public Building deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject buildingObject = json.getAsJsonObject();
        JsonElement buildingTypeElement = buildingObject.get(buildingTypeElementName);
        String mapType = buildingTypeElement.getAsString().substring(0, 3);
        Class<? extends Building> buildingType = buildingTypeRegistry.getOrDefault(mapType, defaultBuildingClass);
        return gson.fromJson(buildingObject, buildingType);
    }
}
