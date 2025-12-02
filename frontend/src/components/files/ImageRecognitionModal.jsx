import React, {useState} from "react";
import {Button, Modal} from "antd";
import ImageWithAnnotations from "./ImageWithAnnotations";

export default function ImageRecognitionModal({
    visible,
    onClose,
    result,
    imageUrl,
}) {
    const [showAnnotations, setShowAnnotations] = useState(true);
    const [imageSize, setImageSize] = useState(null);

    const convertToPercent = (labels) => {
        if (!labels || !imageSize) return [];
        const { width, height } = imageSize;
        return labels.map((label) => {
            const [x1, y1, x2, y2] = label.coords;
            return {
                label: label.name,
                x: (x1 / width) * 100,
                y: (y1 / height) * 100,
                width: ((x2 - x1) / width) * 100,
                height: ((y2 - y1) / height) * 100,
            };
        });
    };

    return (
        <Modal
            open={visible}
            onCancel={onClose}
            footer={null}
            width={850}
            style={{
                top: 20,
                textAlign: "center",
            }}
            title={
                <div
                    style={{ display: "flex", justifyContent: "space-between" }}
                >
                    <span>Разметка изображения</span>
                </div>
            }
        >
            <div style={{ paddingTop: 10 }}>
                <ImageWithAnnotations
                    src={imageUrl}
                    annotations={
                        showAnnotations ? convertToPercent(result?.labels) : []
                    }
                    onSize={(w, h) => setImageSize({ width: w, height: h })}
                />
            </div>
            <div style={{ textAlign: "right", marginTop: 20 }}>
                <Button
                    style={{
                        alignItems: "right",
                    }}
                    onClick={() => setShowAnnotations((v) => !v)}
                >
                    {showAnnotations ? "Скрыть разметку" : "Показать разметку"}
                </Button>
            </div>
        </Modal>
    );
}
