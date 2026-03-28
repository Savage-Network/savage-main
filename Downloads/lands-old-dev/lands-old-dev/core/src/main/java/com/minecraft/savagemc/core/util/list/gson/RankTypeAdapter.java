package com.minecraft.savagemc.core.util.list.gson;

import com.google.gson.*;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;

import java.lang.reflect.Type;

public class RankTypeAdapter implements JsonDeserializer<RankType>, JsonSerializer<RankType> {

    @Override
    public RankType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull())
            return RankType.MEMBER;

        // Formato simples: "MEMBER"
        if (json.isJsonPrimitive())
            return parseByName(json.getAsString());

        // Formato objeto: {"name": "Membro", ...} ou {"type": "MEMBER"}
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();

            // Tenta pelo campo "name" (nome do enum, ex: "MEMBER")
            if (obj.has("name")) {
                RankType type = parseByName(obj.get("name").getAsString());
                if (type != null) return type;
            }
        }

        return RankType.MEMBER;
    }

    @Override
    public JsonElement serialize(RankType src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? new JsonPrimitive(RankType.MEMBER.name()) : new JsonPrimitive(src.name());
    }

    private RankType parseByName(String name) {
        try {
            return RankType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Tenta pelo nome display (ex: "Membro" -> MEMBER)
            return RankType.of(name);
        }
    }
}
