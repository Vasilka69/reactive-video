import React from "react";
import {Button, Modal, Space, Spin, Typography} from "antd";

const { Paragraph, Title } = Typography;

export default function TextToSpeechModal({
    open,
    onClose,
    audioUrl,
    textContent,
    textLoading,
    onSpeak,
    onSpeakCached,
    ttsLoading,
}) {
    return (
        <Modal
            open={open}
            onCancel={onClose}
            footer={null}
            title="Просмотр текста"
            width={720}
        >
            <div style={{ marginBottom: 16 }}>
                <Title level={5} style={{ marginBottom: 8 }}>
                    Содержимое файла
                </Title>
                <div
                    style={{
                        minHeight: 160,
                        maxHeight: 320,
                        padding: 12,
                        border: "1px solid #f0f0f0",
                        borderRadius: 8,
                        background: "#fafafa",
                        overflow: "auto",
                    }}
                >
                    {textLoading ? (
                        <Spin />
                    ) : (
                        <Paragraph style={{ whiteSpace: "pre-wrap", margin: 0 }}>
                            {textContent}
                        </Paragraph>
                    )}
                </div>
            </div>

            <Space size="middle" style={{ marginBottom: 16, flexWrap: "wrap" }}>
                <Button
                    type="primary"
                    loading={ttsLoading}
                    onClick={onSpeak}
                    disabled={textLoading}
                >
                    Озвучить текст
                </Button>
                <Button
                    loading={ttsLoading}
                    onClick={onSpeakCached}
                    disabled={textLoading}
                >
                    Использовать кэшированную озвучку (при наличии)
                </Button>
            </Space>

            {audioUrl ? (
                <audio
                    controls
                    autoPlay
                    src={audioUrl}
                    style={{ width: "100%" }}
                />
            ) : (
                <Paragraph type="secondary" style={{ marginTop: 8 }}>
                    Аудио появится после запуска озвучки.
                </Paragraph>
            )}
        </Modal>
    );
}
