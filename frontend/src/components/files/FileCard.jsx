import React, {useEffect, useState} from "react";
import axios from "axios";
import {Button, Card, message, Space, Spin} from "antd";
import {useFileBlob} from "../../hooks/useFileBlob";
import {cachedRecognizeImage, recognizeImage,} from "../../api/imageRecognition";
import {cachedTextToSpeech, textToSpeech,} from "../../api/textToSpeech";
import ImageRecognitionModal from "./ImageRecognitionModal";
import FilePreviewModal from "./FilePreviewModal";
import TextToSpeechModal from "./TextToSpeechModal";

export default function FileCard({ file, onDelete }) {
    const { blobUrl, loading } = useFileBlob(file.fileId);
    const isImage = file.type === "image";
    const isGif = file.type === "gif";
    const isVideo = file.type === "video";
    const isText = file.type === "text";
    const canPreview = isImage || isGif || isVideo;
    const [modalVisible, setModalVisible] = useState(false);
    const [previewVisible, setPreviewVisible] = useState(false);
    const [recognitionResult, setRecognitionResult] = useState(null);
    const [textModalVisible, setTextModalVisible] = useState(false);
    const [textContent, setTextContent] = useState("");
    const [textLoading, setTextLoading] = useState(false);
    const [ttsAudioUrl, setTtsAudioUrl] = useState(null);
    const [ttsLoading, setTtsLoading] = useState(false);
    const [recognitionLoading, setRecognitionLoading] = useState(false);
    const messageKeys = { recog: "recog", tts: "tts" };

    useEffect(() => {
        return () => {
            if (ttsAudioUrl) {
                URL.revokeObjectURL(ttsAudioUrl);
            }
        };
    }, [ttsAudioUrl]);

    const handleMarkImage = async () => {
        try {
            setRecognitionLoading(true);
            message.loading({ content: "Распознавание...", key: messageKeys.recog });
            const result = await recognizeImage(file.fileId);
            message.success({
                content: "Распознавание завершено",
                key: messageKeys.recog,
                duration: 2,
            });
            setRecognitionResult(result);
            setModalVisible(true);
        } catch (e) {
            console.error(e);
            message.error({
                content:
                    "Ошибка распознавания: " +
                    (e?.response?.data?.message || "Неизвестная ошибка"),
                key: messageKeys.recog,
            });
        } finally {
            setRecognitionLoading(false);
        }
    };

    const handleUseCached = async () => {
        try {
            setRecognitionLoading(true);
            message.loading({
                content: "Получение кэшированной разметки...",
                key: messageKeys.recog,
            });
            const result = await cachedRecognizeImage(file.fileId);
            message.success({
                content: "Кэшированная разметка получена",
                key: messageKeys.recog,
                duration: 2,
            });
            setRecognitionResult(result);
            setModalVisible(true);
        } catch (e) {
            console.error(e);
            message.error({
                content:
                    "Ошибка загрузки кэшированной разметки: " +
                    (e?.response?.data?.message || "Неизвестная ошибка"),
                key: messageKeys.recog,
            });
        } finally {
            setRecognitionLoading(false);
        }
    };

    const openTextModal = async () => {
        setTextModalVisible(true);
        setTextLoading(true);
        setTextContent("");
        try {
            const token = localStorage.getItem("token");
            const res = await axios.get(
                `/api/v1/reactive/file/sync/${file.fileId}`,
                {
                    headers: { Authorization: token },
                    responseType: "text",
                }
            );
            setTextContent(res.data);
        } catch (e) {
            console.error(e);
            message.error(
                "Не удалось загрузить текст: " +
                    (e?.response?.data?.message || "произошла ошибка")
            );
        } finally {
            setTextLoading(false);
        }
    };

    const handleTextToSpeech = async (useCache = false) => {
        try {
            setTextModalVisible(true);
            setTtsLoading(true);
            message.loading({
                content: useCache
                    ? "Получаем озвучку из кеша..."
                    : "Готовим озвучку текста...",
                key: messageKeys.tts,
            });
            const audioBlob = useCache
                ? await cachedTextToSpeech(file.fileId)
                : await textToSpeech(file.fileId);
            if (ttsAudioUrl) {
                URL.revokeObjectURL(ttsAudioUrl);
            }
            const url = URL.createObjectURL(audioBlob);
            setTtsAudioUrl(url);
            message.success({
                content: useCache
                    ? "Озвучка получена из кеша"
                    : "Озвучка готова",
                key: messageKeys.tts,
                duration: 2,
            });
        } catch (e) {
            console.error(e);
            message.error({
                content:
                    "Не удалось озвучить текст: " +
                    (e?.response?.data?.message || "произошла ошибка"),
                key: messageKeys.tts,
            });
        } finally {
            setTtsLoading(false);
        }
    };

    const handleCloseTextModal = () => {
        if (ttsAudioUrl) {
            URL.revokeObjectURL(ttsAudioUrl);
            setTtsAudioUrl(null);
        }
        setTextContent("");
        setTextModalVisible(false);
    };

    const renderPreview = () => {
        if (loading) return <Spin />;
        switch (file.type) {
            case "image":
            case "gif":
                return (
                    <img
                        src={blobUrl}
                        alt={file.filePath}
                        style={{ width: 120, borderRadius: 8 }}
                    />
                );
            case "video":
                return (
                    <video
                        src={blobUrl}
                        height="80"
                        style={{ borderRadius: 8 }}
                        muted
                    />
                );
            case "text":
                return (
                    <div
                        style={{
                            width: 120,
                            height: 80,
                            borderRadius: 8,
                            background: "#e6f4ff",
                            color: "#1677ff",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            fontWeight: 600,
                        }}
                    >
                        TEXT
                    </div>
                );
            default:
                return (
                    <div
                        style={{
                            width: 120,
                            height: 80,
                            borderRadius: 8,
                            background: "#eee",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                        }}
                    >
                        {file.type.toUpperCase()}
                    </div>
                );
        }
    };

    return (
        <>
            <Card
                style={{ marginBottom: 16, cursor: "pointer" }}
                onClick={() => setPreviewVisible(true)}
            >
                <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
                    {renderPreview()}
                    <div>
                        <div>{file.filePath}</div>
                        <div style={{ color: "#888" }}>{file.bucket}</div>
                    </div>
                </div>

                <Space style={{ marginTop: 16 }}>
                    {isImage && (
                        <>
                            <Button
                                type="primary"
                                loading={recognitionLoading}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleMarkImage();
                                }}
                            >
                                Разметить изображение
                            </Button>

                            <Button
                                loading={recognitionLoading}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleUseCached();
                                }}
                            >
                                Использовать кэшированную разметку (при наличии)
                            </Button>
                        </>
                    )}

                    {isText && (
                        <Button
                            type="primary"
                            loading={textLoading}
                            onClick={(e) => {
                                e.stopPropagation();
                                openTextModal();
                            }}
                        >
                            Просмотр и озвучка текста
                        </Button>
                    )}

                    <Button
                        onClick={(e) => {
                            e.stopPropagation();
                            const link = document.createElement("a");
                            link.href = blobUrl;
                            link.download = file.filePath.split("/").pop();
                            link.click();
                        }}
                    >
                        Скачать
                    </Button>

                    <Button
                        danger
                        onClick={(e) => {
                            e.stopPropagation();
                            onDelete();
                        }}
                    >
                        Удалить
                    </Button>
                </Space>
            </Card>

            <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
                {canPreview && previewVisible && (
                    <FilePreviewModal
                        open={previewVisible}
                        onClose={() => setPreviewVisible(false)}
                        file={file}
                        blobUrl={blobUrl}
                    />
                )}
            </div>

            <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
                {modalVisible && (
                    <ImageRecognitionModal
                        visible={modalVisible}
                        onClose={() => setModalVisible(false)}
                        result={recognitionResult}
                        imageUrl={blobUrl}
                    />
                )}
                {textModalVisible && (
                    <TextToSpeechModal
                        open={textModalVisible}
                        onClose={handleCloseTextModal}
                        audioUrl={ttsAudioUrl}
                        textContent={textContent}
                        textLoading={textLoading}
                        onSpeak={() => handleTextToSpeech(false)}
                        onSpeakCached={() => handleTextToSpeech(true)}
                        ttsLoading={ttsLoading}
                    />
                )}
            </div>
        </>
    );
}
