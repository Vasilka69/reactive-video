package ru.vasili4.reactive_video.utils;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;

public class CustomDataBufferUtils extends DataBufferUtils {

    public static byte[] readAllBytesArray(DataBuffer dataBuffer) {
        byte[] byteContent = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(byteContent);
        release(dataBuffer);

        return byteContent;
    }
}
