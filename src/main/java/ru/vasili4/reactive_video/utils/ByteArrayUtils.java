package ru.vasili4.reactive_video.utils;

import java.util.stream.IntStream;

public class ByteArrayUtils {

    public static Byte[] primitiveArrayToObjectArray(byte[] bytes) {
        return IntStream.range(0, bytes.length)
                .mapToObj(i -> bytes[i])
                .toArray(Byte[]::new);
    }

    public static byte[] objectArrayToPrimitiveArray(Byte[] byteObjects) {
        byte[] bytes = new byte[byteObjects.length];
        for (int i = 0; i < byteObjects.length; i++) {
            bytes[i] = byteObjects[i];
        }
        return bytes;
    }

    public static boolean isRangeFinished(Long offset, Long length, Long totalSize) {
        if (offset >= totalSize || length == 0)
            return true;
        return false;
    }
}
