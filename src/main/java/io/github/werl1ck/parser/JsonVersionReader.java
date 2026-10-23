package io.github.werl1ck.parser;

import com.google.gson.Gson;
import io.github.werl1ck.model.VersionInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonVersionReader {
    private static final Gson GSON = new Gson();

    public static VersionInfo read(Path jsonPath) throws IOException {
        String jsonText = Files.readString(jsonPath, StandardCharsets.UTF_8);
        return GSON.fromJson(jsonText, VersionInfo.class);
    }
}
