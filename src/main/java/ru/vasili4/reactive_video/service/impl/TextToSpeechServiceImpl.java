package ru.vasili4.reactive_video.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import ru.vasili4.reactive_video.client.vk.VkVoiceClient;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.repository.reactive.FileReactiveRepository;
import ru.vasili4.reactive_video.exception.ImageRecognitionException;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.service.TextToSpeechService;
import ru.vasili4.reactive_video.utils.ByteArrayUtils;
import ru.vasili4.reactive_video.utils.FileUtils;

@Slf4j
@Service
public class TextToSpeechServiceImpl implements TextToSpeechService {

    public static final String FILENAME_KEY = "filename";
    public static final String FILENAME_TEMPLATE = "%s.%s";
    public static final String MP3_CONTENT_TYPE = "audio/mpeg";
    public static final String GRID_FS_UPLOAD_DATE_COLUMN = "uploadDate";

    private final ReactiveGridFsTemplate gridFsTemplate;
    private final FileReactiveRepository fileReactiveRepository;
    private final FileService fileService;
    private final VkVoiceClient vkVoiceClient;

    public TextToSpeechServiceImpl(
            ReactiveGridFsTemplate gridFsTemplate,
            FileReactiveRepository fileReactiveRepository,
            FileService fileService,
            @Lazy VkVoiceClient vkVoiceClient
    ) {
        this.gridFsTemplate = gridFsTemplate;
        this.fileReactiveRepository = fileReactiveRepository;
        this.fileService = fileService;
        this.vkVoiceClient = vkVoiceClient;
    }

    @Override
    public Flux<DataBuffer> textToSpeechById(String id) {
        return fileReactiveRepository.findById(id)
                .handle((FileDocument fileDocument, SynchronousSink<FileDocument> sink) -> {
                    if (!FileUtils.isTextFile(fileDocument.getFilePath())) {
                        sink.error(new ImageRecognitionException("Файл для распознавания объектов должен быть текстового формата (%s)"
                                .formatted(FileUtils.TEXT_EXTENSIONS)));
                    } else {
                        sink.next(fileDocument);
                    }
                })
                .map(FileDocument::getFileId)
                .flatMap(fileService::syncGetFullFileContentById)
                .map(ByteArrayUtils::objectArrayToPrimitiveArray)
                .map(String::new)
                .map(vkVoiceClient::textToSpeech)
                .flatMapMany((Flux<DataBuffer> dataBuffer) ->
                        saveCache(id, dataBuffer)
                        .thenMany(dataBuffer));
    }

    @Override
    public Flux<DataBuffer> cachedTextToSpeechById(String id) {
        return existsInCache(id)
                .flatMapMany(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return loadCache(id);
                    }
                    Flux<DataBuffer> audio = textToSpeechById(id)
                            .cache();
                    return saveCache(id, audio)
                            .thenMany(audio);
                });
    }

    private Mono<ObjectId> saveCache(String id, Flux<DataBuffer> audio) {
        return gridFsTemplate.store(
                audio,
                FILENAME_TEMPLATE.formatted(id, FileUtils.MP3_EXTENSION),
                MP3_CONTENT_TYPE
        );
    }

    private Flux<DataBuffer> loadCache(String id) {
        return gridFsTemplate.findFirst(
                        Query.query(Criteria.where(FILENAME_KEY)
                                .is(FILENAME_TEMPLATE.formatted(id, FileUtils.MP3_EXTENSION)))
                                .with(Sort.by(Sort.Direction.DESC, GRID_FS_UPLOAD_DATE_COLUMN))
                )
                .flatMap(gridFsTemplate::getResource)
                .flatMapMany(ReactiveGridFsResource::getDownloadStream);
    }

    private Mono<Boolean> existsInCache(String id) {
        return gridFsTemplate.findFirst(
                Query.query(Criteria.where(FILENAME_KEY)
                        .is(FILENAME_TEMPLATE.formatted(id, FileUtils.MP3_EXTENSION)))
        ).hasElement();
    }
}
