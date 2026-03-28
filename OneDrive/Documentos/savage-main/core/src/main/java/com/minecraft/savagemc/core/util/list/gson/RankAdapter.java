package com.minecraft.savagemc.core.util.list.gson;

import com.google.gson.*;
import com.minecraft.savagemc.core.account.context.objects.rank.Rank;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;

import java.lang.reflect.Type;

public class RankAdapter implements JsonDeserializer<Rank> {

    @Override
    public Rank deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull())
            return Rank.builder().build();

        JsonObject obj = json.getAsJsonObject();

        // Desserializa o type com fallback para MEMBER
        RankType type = RankType.MEMBER;
        if (obj.has("type")) {
            JsonElement typeEl = obj.get("type");
            try {
                type = context.deserialize(typeEl, RankType.class);
            } catch (Exception ignored) {}
            if (type == null) type = RankType.MEMBER;
        }

        Rank.RankBuilder builder = Rank.builder().type(type);

        if (obj.has("expiresAt"))
            builder.expiresAt(obj.get("expiresAt").getAsLong());

        return builder.build();
    }
}
