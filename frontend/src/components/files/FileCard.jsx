import React, {useState} from "react";
import {Button, Card, message, Space, Spin} from "antd";
import {useFileBlob} from "../../hooks/useFileBlob";
import {cachedRecognizeImage, recognizeImage,} from "../../api/imageRecognition";
import ImageRecognitionModal from "./ImageRecognitionModal";
import FilePreviewModal from "./FilePreviewModal";

export default function FileCard({ file, onDelete }) {
    const { blobUrl, loading } = useFileBlob(file.fileId);
    const [modalVisible, setModalVisible] = useState(false);
    const [previewVisible, setPreviewVisible] = useState(false);
    const [recognitionResult, setRecognitionResult] = useState(null);

    const handleMarkImage = async () => {
        try {
            message.loading({ content: "Распознавание...", key: "recog" });
            const result = await recognizeImage(file.fileId);
            message.success({
                content: "Распознавание завершено",
                key: "recog",
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
                key: "recog",
            });
        }
    };

    const handleUseCached = async () => {
        try {
            message.loading({
                content: "Получение кэшированной разметки...",
                key: "recog",
            });
            const result = await cachedRecognizeImage(file.fileId);
            message.success({
                content: "Кэшированная разметка получена",
                key: "recog",
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
                key: "recog",
            });
        }
    };

    const renderPreview = () => {
        if (loading) return <Spin />;
        switch (file.type) {
            case "image":
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
                    {file.type === "image" && (
                        <>
                            <Button
                                type="primary"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleMarkImage();
                                }}
                            >
                                Разметка изображения
                            </Button>

                            <Button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleUseCached();
                                }}
                            >
                                Использовать кэшированную разметку (при наличии)
                            </Button>
                        </>
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
                {(file.type === "image" || file.type === "video") &&
                    previewVisible && (
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
            </div>
        </>
    );
}
