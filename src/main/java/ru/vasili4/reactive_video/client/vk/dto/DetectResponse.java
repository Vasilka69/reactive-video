package ru.vasili4.reactive_video.client.vk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DetectResponse {

    @JsonProperty("labels")
    private List<Label> labels;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Label {
        @JsonProperty("rus")
        private String name;

        @JsonProperty("coord")
        private List<Integer> coords;
    }
}
