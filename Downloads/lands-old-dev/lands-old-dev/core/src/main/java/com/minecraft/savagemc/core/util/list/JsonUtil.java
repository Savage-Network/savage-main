package com.minecraft.savagemc.core.util.list;

import com.minecraft.savagemc.core.Savage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static JsonObject jsonTree(Object src) {
        return Savage.GSON.toJsonTree(src).getAsJsonObject();
    }

    public static String elementToString(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            }
        }
        return Savage.GSON.toJson(element);
    }

    public static <T> T mapToObject(Map<String, String> map, Class<T> tClass) {
        JsonObject object = new JsonObject();

        map.forEach((key, value) -> {
            try {
                object.add(key, Savage.PARSER.parse(value));
            } catch (Exception e) {
                object.addProperty(key, value);
            }
        });

        return Savage.GSON.fromJson(object, tClass);
    }

    public static Map<String, String> objectToMap(Object src) {
        Map<String, String> map = new HashMap<>();

        try {
            JsonObject object = (JsonObject) Savage.GSON.toJsonTree(src);
            object.entrySet().forEach(entry -> map.put(entry.getKey(), Savage.GSON.toJson(entry.getValue())));
        } catch (Exception ignored) {
        }

        return map;
    }
}
