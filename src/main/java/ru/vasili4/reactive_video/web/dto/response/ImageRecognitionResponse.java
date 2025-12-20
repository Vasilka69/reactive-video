package ru.vasili4.reactive_video.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vasili4.reactive_video.client.vk.dto.DetectResponse;
import ru.vasili4.reactive_video.data.model.reactive.mongo.CachedImageRecognitionDocument;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ImageRecognitionResponse {

    @JsonProperty("labels")
    private List<Label> labels;

    public ImageRecognitionResponse(DetectResponse detectResponse) {
        this.labels = detectResponse.getLabels().stream()
                .map(Label::new)
                .toList();
    }

    public ImageRecognitionResponse(CachedImageRecognitionDocument cachedImageRecognitionDocument) {
        this.labels = cachedImageRecognitionDocument.getLabels().stream()
                .map(Label::new)
                .toList();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Label {
        @JsonProperty("name")
        private String name;

        @JsonProperty("coords")
        private List<Integer> coords;

        public Label(DetectResponse.Label label) {
            this.name = label.getName();
            this.coords = label.getCoords();
        }

        public Label(CachedImageRecognitionDocument.Label label) {
            this.name = label.getName();
            this.coords = label.getCoords();
        }
    }
}
