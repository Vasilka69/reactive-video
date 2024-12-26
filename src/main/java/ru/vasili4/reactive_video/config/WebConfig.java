package ru.vasili4.reactive_video.config;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.vasili4.reactive_video.web.converter.ByteArrayToByteArrayConverter;

import java.util.List;

public class WebConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new ByteArrayToByteArrayConverter());
    }
}
