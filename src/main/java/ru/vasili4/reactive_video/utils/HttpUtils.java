package ru.vasili4.reactive_video.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtils {

    public static Map<String, String> getContentDispositionHeaderByPath(String path) {
        return getContentDispositionHeaderByFilename(FileUtils.getFilenameByPath(path));
    }

    public static Map<String, String> getContentDispositionHeaderByFilename(String filename) {
        return Map.of(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", filename));
    }
}
