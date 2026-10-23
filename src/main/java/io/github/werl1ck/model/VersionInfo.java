package io.github.werl1ck.model;

import com.google.gson.JsonElement;
import java.util.List;

public record VersionInfo(
        String mainClass,
        String minecraftArguments,
        String type,
        AssetIndexInfo assetIndex,
        ArgumentsInfo arguments
) {
    public record AssetIndexInfo(String id) {}

    public record ArgumentsInfo(List<JsonElement> game, List<JsonElement> jvm) {}
}
