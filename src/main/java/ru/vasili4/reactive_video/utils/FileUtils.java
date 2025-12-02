package ru.vasili4.reactive_video.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.FileNameUtils;

import java.nio.file.Paths;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
    public static final String MP4_EXTENSION = "mp4";
    public static final List<String> VIDEO_EXTENSIONS = List.of(MP4_EXTENSION);

    public static final String PNG_EXTENSION = "png";
    public static final String JPG_EXTENSION = "jpg";
    public static final String JPEG_EXTENSION = "jpeg";
    public static final List<String> IMAGE_EXTENSIONS = List.of(PNG_EXTENSION, JPG_EXTENSION, JPEG_EXTENSION);

    public static String getFilenameByPath(String path) {
        return Paths.get(path).getFileName().toString();
    }

    public static String getExtensionByPath(String path) {
        return FileNameUtils.getExtension(path);
    }

    public static boolean isVideoFile(String path) {
        return VIDEO_EXTENSIONS.contains(getExtensionByPath(path));
    }

    public static boolean isImageFile(String path) {
        return IMAGE_EXTENSIONS.contains(getExtensionByPath(path));
    }
}
