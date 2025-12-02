package ru.vasili4.reactive_video.data.model.reactive.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.vasili4.reactive_video.client.vk.dto.DetectResponse;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("cachedImageRecognition")
public class CachedImageRecognitionDocument {

    @Id
    private String fileId;
    private List<Label> labels;

    public CachedImageRecognitionDocument(String fileId, DetectResponse detectResponse) {
        this.fileId = fileId;
        this.labels = detectResponse.getLabels().stream()
                .map(Label::new)
                .toList();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Label {
        private String name;
        private List<Integer> coords;

        public Label(DetectResponse.Label label) {
            this.name = label.getName();
            this.coords = label.getCoords();
        }
    }
}
