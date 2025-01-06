package ru.vasili4.reactive_video.utils;

import org.springframework.http.HttpHeaders;

import java.nio.file.Paths;
import java.util.Map;

public class HttpUtils {

    public static Map<String, String> getFilenameHeaderFromFullPath(String path) {
        return getFilenameHeader(Paths.get(path).getFileName().toString());
    }

    public static Map<String, String> getFilenameHeader(String filename) {
        return Map.of(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", filename));
    }
}
