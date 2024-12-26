package ru.vasili4.reactive_video.web.converter;

import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConversionException;

import java.io.IOException;

public class ByteArrayToByteArrayConverter extends AbstractHttpMessageConverter<Byte[]> {

    public ByteArrayToByteArrayConverter() {
        super(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Byte[].class.isAssignableFrom(clazz);
    }

    @Override
    protected Byte[] readInternal(Class<? extends Byte[]> clazz, org.springframework.http.HttpInputMessage inputMessage) throws HttpMessageConversionException {
        // Чтение из потока не требуется, так как мы всегда работаем с уже подготовленным объектом
        return null;
    }

    @Override
    protected void writeInternal(Byte[] bytes, org.springframework.http.HttpOutputMessage outputMessage) throws IOException, HttpMessageConversionException {
        byte[] byteArray = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            byteArray[i] = bytes[i];
        }
        outputMessage.getBody().write(byteArray);
    }
}
